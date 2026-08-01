#include <jni.h>

#include <string>
#include <vector>

extern "C" {
#include <libavcodec/codec.h>
#include <libavformat/avformat.h>
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
