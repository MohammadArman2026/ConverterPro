#include <jni.h>

#include <unistd.h>

#include <algorithm>
#include <cerrno>
#include <cmath>
#include <cstring>
#include <string>
#include <vector>

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavcodec/codec.h>
#include <libavformat/avformat.h>
#include <libavutil/error.h>
#include <libavutil/audio_fifo.h>
#include <libavutil/opt.h>
#include <libswresample/swresample.h>
}

namespace {

jobjectArray toJavaStringArray(JNIEnv *env, const std::vector<std::string> &items) {
    jclass stringClass = env->FindClass("java/lang/String");
    if (stringClass == nullptr) {
        return nullptr;
    }

    jobjectArray result = env->NewObjectArray(
            static_cast<jsize>(items.size()), stringClass, nullptr);
    if (result == nullptr) {
        return nullptr;
    }

    for (jsize index = 0; index < static_cast<jsize>(items.size()); ++index) {
        jstring value = env->NewStringUTF(items[index].c_str());
        if (value == nullptr) {
            return nullptr;
        }
        env->SetObjectArrayElement(result, index, value);
        env->DeleteLocalRef(value);
    }

    return result;
}

template <typename Predicate>
jobjectArray listCodecs(JNIEnv *env, Predicate predicate) {
    std::vector<std::string> codecs;
    void *iterator = nullptr;

    while (const AVCodec *codec = av_codec_iterate(&iterator)) {
        if (predicate(codec)) {
            codecs.emplace_back(codec->name);
        }
    }

    return toJavaStringArray(env, codecs);
}

std::string ffmpegError(const char *operation, int errorCode) {
    char message[AV_ERROR_MAX_STRING_SIZE] = {};
    av_strerror(errorCode, message, sizeof(message));
    return std::string(operation) + ": " + message;
}

constexpr int kOutputBufferSize = 32 * 1024;

int lastErrnoAsAvError() {
    return AVERROR(errno != 0 ? errno : EIO);
}

int writeToFileDescriptor(void *opaque, const uint8_t *buffer, int size) {
    const int fileDescriptor = *static_cast<int *>(opaque);
    int written = 0;
    while (written < size) {
        const ssize_t count = write(
                fileDescriptor,
                buffer + written,
                static_cast<size_t>(size - written));
        if (count < 0) {
            if (errno == EINTR) continue;
            return lastErrnoAsAvError();
        }
        if (count == 0) return AVERROR(EIO);
        written += static_cast<int>(count);
    }
    return written;
}

int readFromFileDescriptor(void *opaque, uint8_t *buffer, int size) {
    const int fileDescriptor = *static_cast<int *>(opaque);
    while (true) {
        const ssize_t count = read(fileDescriptor, buffer, static_cast<size_t>(size));
        if (count < 0) {
            if (errno == EINTR) continue;
            return lastErrnoAsAvError();
        }
        return count == 0 ? AVERROR_EOF : static_cast<int>(count);
    }
}

int64_t seekFileDescriptor(void *opaque, int64_t offset, int whence) {
    const int fileDescriptor = *static_cast<int *>(opaque);
    if ((whence & ~AVSEEK_FORCE) == AVSEEK_SIZE) {
        const off_t current = lseek(fileDescriptor, 0, SEEK_CUR);
        if (current < 0) return lastErrnoAsAvError();
        const off_t size = lseek(fileDescriptor, 0, SEEK_END);
        if (size < 0) return lastErrnoAsAvError();
        if (lseek(fileDescriptor, current, SEEK_SET) < 0) return lastErrnoAsAvError();
        return size;
    }
    const off_t position = lseek(
            fileDescriptor, static_cast<off_t>(offset), whence & ~AVSEEK_FORCE);
    return position < 0 ? lastErrnoAsAvError() : position;
}

/**
 * Wraps the caller-supplied descriptor in a seekable [AVIOContext].
 *
 * Muxers such as `ipod`/`mp4` rewrite header atoms once the stream length is known, so they
 * reject the non-seekable `pipe:` protocol outright. Container formats that only append data
 * also benefit, because size fields no longer have to be left as placeholders.
 */
int openSeekableOutput(AVFormatContext *outputContext, int *fileDescriptor) {
    auto *buffer = static_cast<unsigned char *>(av_malloc(kOutputBufferSize));
    if (buffer == nullptr) return AVERROR(ENOMEM);

    AVIOContext *io = avio_alloc_context(
            buffer,
            kOutputBufferSize,
            1,
            fileDescriptor,
            readFromFileDescriptor,
            writeToFileDescriptor,
            seekFileDescriptor);
    if (io == nullptr) {
        av_freep(&buffer);
        return AVERROR(ENOMEM);
    }

    outputContext->pb = io;
    return 0;
}

void closeSeekableOutput(AVIOContext **io) {
    if (*io == nullptr) return;
    // libavformat may have swapped the buffer we handed over, so free the current one.
    unsigned char *buffer = (*io)->buffer;
    avio_context_free(io);
    av_freep(&buffer);
}

/**
 * Applies either constant bitrate or variable quality, never both.
 *
 * A negative [qualityScale] means the caller did not ask for variable bitrate. Otherwise FFmpeg
 * needs [AV_CODEC_FLAG_QSCALE] plus `global_quality`; feeding a quality index into `bit_rate`
 * instead asks for an absurdly low bitrate that most encoders refuse to open with.
 */
void configureRateControl(AVCodecContext *encoderContext, int bitrate, float qualityScale) {
    if (qualityScale >= 0.0f) {
        encoderContext->flags |= AV_CODEC_FLAG_QSCALE;
        encoderContext->global_quality =
                static_cast<int>(std::lround(qualityScale * FF_QP2LAMBDA));
    } else if (bitrate > 0) {
        encoderContext->bit_rate = bitrate;
    }
}

int selectSampleRate(const AVCodec *codec, int requestedRate) {
    const void *configurations = nullptr;
    int configurationCount = 0;
    if (requestedRate <= 0 ||
        avcodec_get_supported_config(
                nullptr,
                codec,
                AV_CODEC_CONFIG_SAMPLE_RATE,
                0,
                &configurations,
                &configurationCount) < 0 ||
        configurations == nullptr) {
        return requestedRate;
    }

    const auto *sampleRates = static_cast<const int *>(configurations);
    for (int index = 0; index < configurationCount; ++index) {
        if (sampleRates[index] == requestedRate) {
            return requestedRate;
        }
    }
    return sampleRates[0];
}

enum AVSampleFormat selectSampleFormat(const AVCodec *codec) {
    const void *configurations = nullptr;
    if (avcodec_get_supported_config(
                nullptr,
                codec,
                AV_CODEC_CONFIG_SAMPLE_FORMAT,
                0,
                &configurations,
                nullptr) < 0 ||
        configurations == nullptr) {
        return AV_SAMPLE_FMT_FLTP;
    }
    return static_cast<const AVSampleFormat *>(configurations)[0];
}

int configureChannelLayout(
        AVCodecContext *encoderContext,
        int requestedChannelCount,
        const AVChannelLayout *inputLayout) {
    const int targetChannels = requestedChannelCount > 0
            ? requestedChannelCount
            : std::max(inputLayout->nb_channels, 1);

    const void *configurations = nullptr;
    int configurationCount = 0;
    if (avcodec_get_supported_config(
                nullptr,
                encoderContext->codec,
                AV_CODEC_CONFIG_CHANNEL_LAYOUT,
                0,
                &configurations,
                &configurationCount) >= 0 &&
        configurations != nullptr) {
        const auto *layouts = static_cast<const AVChannelLayout *>(configurations);
        for (int index = 0; index < configurationCount; ++index) {
            const AVChannelLayout *layout = &layouts[index];
            if (layout->nb_channels == targetChannels) {
                return av_channel_layout_copy(&encoderContext->ch_layout, layout);
            }
        }
        return av_channel_layout_copy(&encoderContext->ch_layout, &layouts[0]);
    }

    av_channel_layout_default(&encoderContext->ch_layout, targetChannels);
    return 0;
}

int writeEncodedPackets(
        AVCodecContext *encoderContext,
        AVFormatContext *outputContext,
        AVStream *outputStream) {
    AVPacket *packet = av_packet_alloc();
    if (packet == nullptr) {
        return AVERROR(ENOMEM);
    }

    int result = 0;
    while ((result = avcodec_receive_packet(encoderContext, packet)) >= 0) {
        av_packet_rescale_ts(packet, encoderContext->time_base, outputStream->time_base);
        packet->stream_index = outputStream->index;
        result = av_interleaved_write_frame(outputContext, packet);
        av_packet_unref(packet);
        if (result < 0) {
            break;
        }
    }
    av_packet_free(&packet);
    return result == AVERROR(EAGAIN) || result == AVERROR_EOF ? 0 : result;
}

int encodeAvailableSamples(
        AVAudioFifo *audioFifo,
        AVCodecContext *encoderContext,
        AVFormatContext *outputContext,
        AVStream *outputStream,
        int64_t *nextPts,
        bool flush) {
    const int frameSize = encoderContext->frame_size;
    while (av_audio_fifo_size(audioFifo) > 0 &&
           (flush || frameSize == 0 || av_audio_fifo_size(audioFifo) >= frameSize)) {
        const int samplesToEncode = frameSize > 0
                ? frameSize
                : av_audio_fifo_size(audioFifo);
        AVFrame *outputFrame = av_frame_alloc();
        if (outputFrame == nullptr) {
            return AVERROR(ENOMEM);
        }
        outputFrame->nb_samples = samplesToEncode;
        outputFrame->format = encoderContext->sample_fmt;
        outputFrame->sample_rate = encoderContext->sample_rate;
        int result = av_channel_layout_copy(&outputFrame->ch_layout, &encoderContext->ch_layout);
        if (result >= 0) result = av_frame_get_buffer(outputFrame, 0);
        if (result >= 0) {
            av_samples_set_silence(
                    outputFrame->data,
                    0,
                    samplesToEncode,
                    encoderContext->ch_layout.nb_channels,
                    encoderContext->sample_fmt);
            const int samplesRead = av_audio_fifo_read(
                    audioFifo,
                    reinterpret_cast<void **>(outputFrame->data),
                    std::min(samplesToEncode, av_audio_fifo_size(audioFifo)));
            if (samplesRead < 0) result = samplesRead;
        }
        if (result >= 0) {
            outputFrame->pts = *nextPts;
            *nextPts += samplesToEncode;
            result = avcodec_send_frame(encoderContext, outputFrame);
        }
        if (result >= 0) {
            result = writeEncodedPackets(encoderContext, outputContext, outputStream);
        }
        av_frame_free(&outputFrame);
        if (result < 0) return result;
    }
    return 0;
}

int resampleAndQueue(
        SwrContext *resampler,
        AVAudioFifo *audioFifo,
        AVFrame *inputFrame,
        AVCodecContext *encoderContext,
        AVFormatContext *outputContext,
        AVStream *outputStream,
        int64_t *nextPts) {
    const int outputSamples = av_rescale_rnd(
            swr_get_delay(resampler, inputFrame->sample_rate) + inputFrame->nb_samples,
            encoderContext->sample_rate,
            inputFrame->sample_rate,
            AV_ROUND_UP);
    AVFrame *outputFrame = av_frame_alloc();
    if (outputFrame == nullptr) {
        return AVERROR(ENOMEM);
    }

    outputFrame->nb_samples = outputSamples;
    outputFrame->format = encoderContext->sample_fmt;
    outputFrame->sample_rate = encoderContext->sample_rate;
    int result = av_channel_layout_copy(&outputFrame->ch_layout, &encoderContext->ch_layout);
    if (result >= 0) {
        result = av_frame_get_buffer(outputFrame, 0);
    }
    if (result >= 0) {
        result = swr_convert(
                resampler,
                outputFrame->data,
                outputSamples,
                const_cast<const uint8_t **>(inputFrame->extended_data),
                inputFrame->nb_samples);
    }
    if (result >= 0) {
        outputFrame->nb_samples = result;
        result = av_audio_fifo_realloc(
                audioFifo,
                av_audio_fifo_size(audioFifo) + result);
    }
    if (result >= 0) {
        const int samplesWritten = av_audio_fifo_write(
                audioFifo,
                reinterpret_cast<void **>(outputFrame->data),
                outputFrame->nb_samples);
        if (samplesWritten < outputFrame->nb_samples) result = AVERROR(ENOMEM);
    }
    av_frame_free(&outputFrame);
    return result >= 0
            ? encodeAvailableSamples(audioFifo, encoderContext, outputContext, outputStream, nextPts, false)
            : result;
}

std::string transcodeAudio(
        const char *inputPath,
        int outputFileDescriptor,
        const char *containerFormat,
        const char *encoderName,
        int bitrate,
        float qualityScale,
        int requestedSampleRate,
        int requestedChannelCount) {
    AVFormatContext *inputContext = nullptr;
    AVFormatContext *outputContext = nullptr;
    AVIOContext *outputIo = nullptr;
    AVCodecContext *decoderContext = nullptr;
    AVCodecContext *encoderContext = nullptr;
    SwrContext *resampler = nullptr;
    AVAudioFifo *audioFifo = nullptr;
    AVPacket *inputPacket = nullptr;
    AVFrame *decodedFrame = nullptr;
    const AVStream *inputStream = nullptr;
    const AVCodec *decoder = nullptr;
    const AVCodec *encoder = nullptr;
    AVStream *outputStream = nullptr;
    int audioStreamIndex = -1;
    int64_t nextPts = 0;
    int result = 0;
    std::string error;

    result = avformat_open_input(&inputContext, inputPath, nullptr, nullptr);
    if (result < 0) {
        error = ffmpegError("Unable to open input", result);
        goto cleanup;
    }

    result = avformat_find_stream_info(inputContext, nullptr);
    if (result < 0) {
        error = ffmpegError("Unable to read input", result);
        goto cleanup;
    }

    audioStreamIndex = av_find_best_stream(
            inputContext, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);
    if (audioStreamIndex < 0) {
        error = "No audio stream found in input.";
        goto cleanup;
    }

    inputStream = inputContext->streams[audioStreamIndex];
    decoder = avcodec_find_decoder(inputStream->codecpar->codec_id);
    encoder = avcodec_find_encoder_by_name(encoderName);
    if (decoder == nullptr || encoder == nullptr) {
        error = encoder == nullptr ? "Selected FFmpeg encoder is unavailable." : "Input decoder is unavailable.";
        goto cleanup;
    }

    decoderContext = avcodec_alloc_context3(decoder);
    encoderContext = avcodec_alloc_context3(encoder);
    if (decoderContext == nullptr || encoderContext == nullptr) {
        error = "Unable to allocate FFmpeg codec context.";
        goto cleanup;
    }
    result = avcodec_parameters_to_context(decoderContext, inputStream->codecpar);
    if (result >= 0) result = avcodec_open2(decoderContext, decoder, nullptr);
    if (result < 0) {
        error = ffmpegError("Unable to open input decoder", result);
        goto cleanup;
    }

    result = avformat_alloc_output_context2(
            &outputContext, nullptr, containerFormat, nullptr);
    if (result < 0 || outputContext == nullptr) {
        error = "Unable to create the selected output format.";
        goto cleanup;
    }

    encoderContext->sample_rate = selectSampleRate(
            encoder, requestedSampleRate > 0 ? requestedSampleRate : decoderContext->sample_rate);
    encoderContext->sample_fmt = selectSampleFormat(encoder);
    if (std::strcmp(encoderName, "libopencore_amrnb") == 0) {
        // AMR-NB has fixed codec requirements, independent of the source audio.
        encoderContext->sample_rate = 8000;
        encoderContext->sample_fmt = AV_SAMPLE_FMT_S16;
        requestedChannelCount = 1;
    }
    encoderContext->time_base = AVRational{1, encoderContext->sample_rate};
    configureRateControl(encoderContext, bitrate, qualityScale);
    result = configureChannelLayout(
            encoderContext, requestedChannelCount, &decoderContext->ch_layout);
    if (result >= 0 && (outputContext->oformat->flags & AVFMT_GLOBALHEADER)) {
        encoderContext->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
    }
    if (result >= 0) result = avcodec_open2(encoderContext, encoder, nullptr);
    if (result < 0) {
        error = ffmpegError("Unable to configure selected encoder", result);
        goto cleanup;
    }

    outputStream = avformat_new_stream(outputContext, nullptr);
    if (outputStream == nullptr) {
        error = "Unable to create output audio stream.";
        goto cleanup;
    }
    outputStream->time_base = encoderContext->time_base;
    result = avcodec_parameters_from_context(outputStream->codecpar, encoderContext);
    if (result >= 0) result = openSeekableOutput(outputContext, &outputFileDescriptor);
    if (result >= 0) outputIo = outputContext->pb;
    if (result >= 0) result = avformat_write_header(outputContext, nullptr);
    if (result < 0) {
        error = ffmpegError("Unable to write output header", result);
        goto cleanup;
    }

    result = swr_alloc_set_opts2(
            &resampler,
            &encoderContext->ch_layout,
            encoderContext->sample_fmt,
            encoderContext->sample_rate,
            &decoderContext->ch_layout,
            decoderContext->sample_fmt,
            decoderContext->sample_rate,
            0,
            nullptr);
    if (result >= 0) result = swr_init(resampler);
    if (result < 0) {
        error = ffmpegError("Unable to initialize audio resampler", result);
        goto cleanup;
    }

    audioFifo = av_audio_fifo_alloc(
            encoderContext->sample_fmt,
            encoderContext->ch_layout.nb_channels,
            1);
    if (audioFifo == nullptr) {
        error = "Unable to allocate FFmpeg audio buffer.";
        goto cleanup;
    }

    inputPacket = av_packet_alloc();
    decodedFrame = av_frame_alloc();
    if (inputPacket == nullptr || decodedFrame == nullptr) {
        error = "Unable to allocate FFmpeg audio buffers.";
        goto cleanup;
    }

    while ((result = av_read_frame(inputContext, inputPacket)) >= 0) {
        if (inputPacket->stream_index == audioStreamIndex) {
            result = avcodec_send_packet(decoderContext, inputPacket);
            while (result >= 0 && (result = avcodec_receive_frame(decoderContext, decodedFrame)) >= 0) {
                result = resampleAndQueue(
                        resampler, audioFifo, decodedFrame, encoderContext, outputContext, outputStream, &nextPts);
                av_frame_unref(decodedFrame);
            }
            if (result == AVERROR(EAGAIN) || result == AVERROR_EOF) result = 0;
        }
        av_packet_unref(inputPacket);
        if (result < 0) break;
    }
    if (result == AVERROR_EOF) result = 0;
    if (result < 0) {
        error = ffmpegError("Audio conversion failed", result);
        goto cleanup;
    }

    result = avcodec_send_packet(decoderContext, nullptr);
    while (result >= 0 && (result = avcodec_receive_frame(decoderContext, decodedFrame)) >= 0) {
        result = resampleAndQueue(
                resampler, audioFifo, decodedFrame, encoderContext, outputContext, outputStream, &nextPts);
        av_frame_unref(decodedFrame);
    }
    if (result == AVERROR(EAGAIN) || result == AVERROR_EOF) result = 0;
    if (result >= 0) result = encodeAvailableSamples(
            audioFifo, encoderContext, outputContext, outputStream, &nextPts, true);
    if (result >= 0) result = avcodec_send_frame(encoderContext, nullptr);
    if (result >= 0) result = writeEncodedPackets(encoderContext, outputContext, outputStream);
    if (result >= 0) result = av_write_trailer(outputContext);
    if (result < 0) error = ffmpegError("Unable to finalize output", result);

cleanup:
    av_packet_free(&inputPacket);
    av_frame_free(&decodedFrame);
    av_audio_fifo_free(audioFifo);
    swr_free(&resampler);
    avcodec_free_context(&decoderContext);
    avcodec_free_context(&encoderContext);
    if (outputContext != nullptr) {
        outputContext->pb = nullptr;
        avformat_free_context(outputContext);
    }
    closeSeekableOutput(&outputIo);
    avformat_close_input(&inputContext);
    close(outputFileDescriptor);
    return error;
}

} // namespace

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_arman_dev_converterpro_core_ffmpeg_FfmpegNative_nativeEncoders(
        JNIEnv *env,
        jobject /* thiz */) {
    return listCodecs(env, [](const AVCodec *codec) {
        return av_codec_is_encoder(codec);
    });
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_arman_dev_converterpro_core_ffmpeg_FfmpegNative_nativeDecoders(
        JNIEnv *env,
        jobject /* thiz */) {
    return listCodecs(env, [](const AVCodec *codec) {
        return av_codec_is_decoder(codec);
    });
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_arman_dev_converterpro_core_ffmpeg_FfmpegNative_nativeMuxers(
        JNIEnv *env,
        jobject /* thiz */) {
    std::vector<std::string> muxers;
    void *iterator = nullptr;

    while (const AVOutputFormat *format = av_muxer_iterate(&iterator)) {
        muxers.emplace_back(format->name);
    }

    return toJavaStringArray(env, muxers);
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_arman_dev_converterpro_core_ffmpeg_FfmpegNative_nativeDemuxers(
        JNIEnv *env,
        jobject /* thiz */) {
    std::vector<std::string> demuxers;
    void *iterator = nullptr;

    while (const AVInputFormat *format = av_demuxer_iterate(&iterator)) {
        demuxers.emplace_back(format->name);
    }

    return toJavaStringArray(env, demuxers);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_arman_dev_converterpro_core_ffmpeg_FfmpegNative_nativeConvert(
        JNIEnv *env,
        jobject /* thiz */,
        jstring inputPath,
        jint outputFileDescriptor,
        jstring containerFormat,
        jstring encoder,
        jint bitrateBitsPerSecond,
        jfloat qualityScale,
        jint sampleRate,
        jint channelCount) {
    const char *input = env->GetStringUTFChars(inputPath, nullptr);
    const char *container = env->GetStringUTFChars(containerFormat, nullptr);
    const char *encoderName = env->GetStringUTFChars(encoder, nullptr);
    const std::string error = transcodeAudio(
            input,
            outputFileDescriptor,
            container,
            encoderName,
            bitrateBitsPerSecond,
            qualityScale,
            sampleRate,
            channelCount);
    env->ReleaseStringUTFChars(inputPath, input);
    env->ReleaseStringUTFChars(containerFormat, container);
    env->ReleaseStringUTFChars(encoder, encoderName);
    return error.empty() ? nullptr : env->NewStringUTF(error.c_str());
}
