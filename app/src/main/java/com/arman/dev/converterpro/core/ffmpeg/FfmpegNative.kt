package com.arman.dev.converterpro.core.ffmpeg

/**
 * Entry point for the FFmpeg libraries bundled with this application.
 *
 * The libraries are loaded in dependency order before the JNI bridge. Accessing this object
 * therefore fails fast with [UnsatisfiedLinkError] when an ABI is missing a required library.
 */
object FfmpegNative {
    init {
        listOf(
            "avutil",
            "swresample",
            "swscale",
            "avcodec",
            "avformat",
            "avfilter",
            "avdevice",
            "native-lib",
        ).forEach(System::loadLibrary)
    }

    /** Returns all capabilities compiled into the bundled FFmpeg build. */
    fun capabilities(): FfmpegCapabilities = FfmpegCapabilities(
        encoders = nativeEncoders().asList().sorted(),
        decoders = nativeDecoders().asList().sorted(),
        muxers = nativeMuxers().asList().sorted(),
        demuxers = nativeDemuxers().asList().sorted(),
    )

    fun encoders(): List<String> = nativeEncoders().asList().sorted()

    fun decoders(): List<String> = nativeDecoders().asList().sorted()

    fun muxers(): List<String> = nativeMuxers().asList().sorted()

    fun demuxers(): List<String> = nativeDemuxers().asList().sorted()

    /**
     * Converts one audio stream using the supplied command-equivalent settings.
     *
     * @return an error message when conversion fails, or `null` on success.
     */
    fun convert(command: FfmpegConversionCommand): String? = nativeConvert(
        inputPath = command.inputPath,
        outputFileDescriptor = command.outputFileDescriptor,
        containerFormat = command.containerFormat,
        encoder = command.encoder,
        bitrateKbps = command.bitrateKbps ?: 0,
        sampleRate = command.sampleRate ?: 0,
        channelCount = command.channelCount ?: 0,
    )

    private external fun nativeEncoders(): Array<String>
    private external fun nativeDecoders(): Array<String>
    private external fun nativeMuxers(): Array<String>
    private external fun nativeDemuxers(): Array<String>
    private external fun nativeConvert(
        inputPath: String,
        outputFileDescriptor: Int,
        containerFormat: String,
        encoder: String,
        bitrateKbps: Int,
        sampleRate: Int,
        channelCount: Int,
    ): String?
}

data class FfmpegConversionCommand(
    val inputPath: String,
    val outputFileDescriptor: Int,
    val containerFormat: String,
    val encoder: String,
    val bitrateKbps: Int?,
    val sampleRate: Int?,
    val channelCount: Int?,
)

/** A snapshot of the codecs and container formats enabled in the bundled FFmpeg build. */
data class FfmpegCapabilities(
    val encoders: List<String>,
    val decoders: List<String>,
    val muxers: List<String>,
    val demuxers: List<String>,
)
