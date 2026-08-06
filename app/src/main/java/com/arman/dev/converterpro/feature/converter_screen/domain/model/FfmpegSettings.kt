package com.arman.dev.converterpro.feature.converter_screen.domain.model

internal val Extension.containerFormat: String
    get() = when (this) {
        Extension.AAC -> "adts"
        Extension.M4A -> "ipod"
        Extension.WAV_PACK -> "wv"
        else -> dropDown
    }

internal val Extension.mimeType: String
    get() = when (this) {
        Extension.WAV -> "audio/wav"
        Extension.FLAC -> "audio/flac"
        Extension.AAC -> "audio/aac"
        Extension.M4A -> "audio/mp4"
        Extension.AMR -> "audio/amr"
        Extension.OGG -> "audio/ogg"
        Extension.MP2 -> "audio/mpeg"
        Extension.OPUS -> "audio/ogg"
        Extension.AC3 -> "audio/ac3"
        Extension.WAV_PACK -> "audio/x-wavpack"
    }

internal val Encoder.ffmpegEncoder: String
    get() = when (this) {
        Encoder.WAV -> "pcm_s16le"
        Encoder.FLAC -> "flac"
        Encoder.AAC -> "aac"
        Encoder.LIB_VORBIS -> "libvorbis"
        Encoder.LIB_OPUS -> "libopus"
        Encoder.AC3 -> "ac3"
        Encoder.AMR_NB -> "libopencore_amrnb"
        Encoder.MP2 -> "mp2"
        Encoder.WAV_PACK -> "wavpack"
    }

/** The dropdown label is expressed in kilobits per second; FFmpeg expects bits per second. */
internal val BitrateValue.bitsPerSecond: Int?
    get() = dropDown.toDoubleOrNull()?.times(1_000)?.toInt()?.takeIf { it > 0 }

/**
 * The variable bitrate quality index handed to FFmpeg's `global_quality`.
 *
 * The same dropdown labels back both rate control modes, so this must only be read when the
 * selected [BitRate] is [BitRate.VBR]; otherwise a 128 kbps choice would become quality 128.
 */
internal val BitrateValue.qualityScale: Float?
    get() = dropDown.toFloatOrNull()?.takeIf { it >= 0f }

internal val SampleRate.hertz: Int?
    get() = dropDown.toIntOrNull()?.takeIf { it > 0 }

internal val Channel.channelCount: Int?
    get() = when (this) {
        Channel.MONO -> 1
        Channel.STEREO -> 2
        Channel.AUTO -> null
    }
