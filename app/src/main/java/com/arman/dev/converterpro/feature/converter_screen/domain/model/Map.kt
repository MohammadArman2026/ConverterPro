package com.arman.dev.converterpro.feature.converter_screen.domain.model

object Map {

    val extensionToEncoding = mapOf(
        Extension.WAV to listOf(
            Encoder.WAV
        ),
        Extension.FLAC to listOf(
            Encoder.FLAC
        ),
        Extension.AAC to listOf(
            Encoder.AAC
        ),
        Extension.M4A to listOf(
            Encoder.AAC
        ),
        Extension.AMR to listOf(
            Encoder.AMR_NB
        ),
        Extension.OGG to listOf(
            Encoder.LIB_OPUS,
            Encoder.LIB_VORBIS
        ),
        Extension.MP2 to listOf(
            Encoder.MP2
        ),
        Extension.OPUS to listOf(
            Encoder.LIB_OPUS,
            Encoder.LIB_VORBIS
        ),
        Extension.AC3 to listOf(
            Encoder.AC3
        ),
        Extension.WAV_PACK to listOf(
            Encoder.WAV_PACK
        )
    )


    val encodingToBitrate = mapOf(
        Encoder.WAV to listOf(
            BitRate.AUTO
        ),
        Encoder.FLAC to listOf(
            BitRate.AUTO
        ),
        Encoder.AAC to listOf(
            BitRate.AUTO ,
            BitRate.CBR,
            BitRate.VBR
        ),
        Encoder.AMR_NB to listOf(
            BitRate.CBR
        ),
        Encoder.LIB_VORBIS to listOf(
            BitRate.AUTO ,
            BitRate.CBR,
            BitRate.VBR
        ),
        // libopus targets a bitrate rather than a quality index, so it has no VBR entry.
        Encoder.LIB_OPUS to listOf(
            BitRate.AUTO,
            BitRate.CBR
        ),
        // FFmpeg's mp2 and ac3 encoders reject AV_CODEC_FLAG_QSCALE, so VBR is unavailable.
        Encoder.MP2 to listOf(
            BitRate.AUTO,
            BitRate.CBR
        ),
        Encoder.AC3 to listOf(
            BitRate.AUTO,
            BitRate.CBR
        ),
        Encoder.WAV_PACK to listOf(
            BitRate.AUTO,
        )
    )

    val encodingToSampleRate = mapOf(
        Encoder.WAV to listOf(
            SampleRate.AUTO,
            SampleRate.SR0,
            SampleRate.SR8000,
            SampleRate.SR11025,
            SampleRate.SR12000,
            SampleRate.SR16000,
            SampleRate.SR22050,
            SampleRate.SR24000,
            SampleRate.SR32000,
            SampleRate.SR44100,
            SampleRate.SR48000,
            SampleRate.SR96000
        ),
        Encoder.FLAC to listOf(
            SampleRate.AUTO,
            SampleRate.SR0,
            SampleRate.SR8000,
            SampleRate.SR11025,
            SampleRate.SR12000,
            SampleRate.SR16000,
            SampleRate.SR22050,
            SampleRate.SR24000,
            SampleRate.SR32000,
            SampleRate.SR44100,
            SampleRate.SR48000,
        ),
        Encoder.AAC to listOf(
            SampleRate.AUTO,
            SampleRate.SR0,
            SampleRate.SR8000,
            SampleRate.SR11025,
            SampleRate.SR12000,
            SampleRate.SR16000,
            SampleRate.SR22050,
            SampleRate.SR24000,
            SampleRate.SR32000,
            SampleRate.SR44100,
            SampleRate.SR48000,
        ),
        Encoder.AMR_NB to listOf(
            SampleRate.SR8000
        ),
        Encoder.LIB_VORBIS to listOf(
            SampleRate.AUTO,
            SampleRate.SR0,
            SampleRate.SR8000,
            SampleRate.SR11025,
            SampleRate.SR12000,
            SampleRate.SR16000,
            SampleRate.SR22050,
            SampleRate.SR24000,
            SampleRate.SR32000,
            SampleRate.SR44100,
            SampleRate.SR48000,
            SampleRate.SR96000
        ),
        Encoder.LIB_OPUS to listOf(
            SampleRate.AUTO,
            SampleRate.SR0,
            SampleRate.SR8000,
            SampleRate.SR11025,
            SampleRate.SR12000,
            SampleRate.SR16000,
            SampleRate.SR22050,
            SampleRate.SR24000,
            SampleRate.SR32000,
            SampleRate.SR44100,
            SampleRate.SR48000,
            SampleRate.SR96000
        ),
        Encoder.MP2 to listOf(
            SampleRate.AUTO,
            SampleRate.SR16000,
            SampleRate.SR22050,
            SampleRate.SR24000,
            SampleRate.SR32000,
            SampleRate.SR44100,
            SampleRate.SR48000,
        ),
        Encoder.AC3 to listOf(
            SampleRate.AUTO,
            SampleRate.SR0,
            SampleRate.SR8000,
            SampleRate.SR11025,
            SampleRate.SR12000,
            SampleRate.SR16000,
            SampleRate.SR22050,
            SampleRate.SR24000,
            SampleRate.SR32000,
            SampleRate.SR44100,
            SampleRate.SR48000,
            SampleRate.SR96000
        ),
        Encoder.WAV_PACK to listOf(
            SampleRate.AUTO,
            SampleRate.SR0,
            SampleRate.SR8000,
            SampleRate.SR11025,
            SampleRate.SR12000,
            SampleRate.SR16000,
            SampleRate.SR22050,
            SampleRate.SR24000,
            SampleRate.SR32000,
            SampleRate.SR44100,
            SampleRate.SR48000,
            SampleRate.SR96000
        )

    )

    /**
     * Values the currently selected encoder actually accepts.
     *
     * Offering a bitrate an encoder rejects surfaces as an opaque "unable to configure selected
     * encoder" failure at conversion time, so every list here is restricted to combinations the
     * encoder can open with.
     */
    fun bitrateValuesFor(
        encoder: Encoder,
        bitRate: BitRate,
        sampleRate: SampleRate
    ): List<BitrateValue> = when (bitRate) {
        BitRate.AUTO -> listOf(BitrateValue.AUTO)
        BitRate.VBR -> encodingToVBRValue[encoder].orEmpty()
        BitRate.CBR -> cbrValuesFor(encoder, sampleRate)
    }

    private fun cbrValuesFor(
        encoder: Encoder,
        sampleRate: SampleRate
    ): List<BitrateValue> = when (encoder) {
        Encoder.AMR_NB -> amrNarrowBandBitrates
        Encoder.MP2 -> mp2BitratesFor(sampleRate)
        Encoder.AC3 -> ac3Bitrates
        Encoder.LIB_VORBIS -> vorbisBitrates
        else -> generalPurposeBitrates
    }

    /**
     * MPEG audio layer II defines two disjoint bitrate tables, chosen by sample rate. Sample rates
     * of 32 kHz and above are MPEG-1, which starts at 32 kbps; the lower rates are MPEG-2, which
     * reaches only 160 kbps. When the sample rate follows the source it is unknown until conversion
     * starts, so only bitrates legal under both tables are offered.
     */
    private fun mp2BitratesFor(sampleRate: SampleRate): List<BitrateValue> {
        val hertz = sampleRate.dropDown.toIntOrNull()
        return when {
            hertz == null || hertz <= 0 -> mpeg1AndMpeg2LayerIIBitrates
            hertz >= MPEG1_MIN_SAMPLE_RATE -> mpeg1LayerIIBitrates
            else -> mpeg2LayerIIBitrates
        }
    }

    private val encodingToVBRValue = mapOf(
        // libvorbis maps global_quality onto its documented -1..10 quality scale.
        Encoder.LIB_VORBIS to listOf(
            BitrateValue.VBR0,
            BitrateValue.VBR1,
            BitrateValue.VBR2,
            BitrateValue.VBR3,
            BitrateValue.VBR4,
            BitrateValue.VBR5,
            BitrateValue.VBR6,
            BitrateValue.VBR7,
            BitrateValue.VBR8,
            BitrateValue.VBR9,
            BitrateValue.VBR10
        ),
        // The native AAC encoder degrades badly outside a narrow quality window.
        Encoder.AAC to listOf(
            BitrateValue.VBR1,
            BitrateValue.VBR2,
            BitrateValue.VBR3,
            BitrateValue.VBR4,
            BitrateValue.VBR5
        )
    )

    private val generalPurposeBitrates = listOf(
        BitrateValue.CBR16,
        BitrateValue.CBR32,
        BitrateValue.CBR40,
        BitrateValue.CBR48,
        BitrateValue.CBR56,
        BitrateValue.CBR64,
        BitrateValue.CBR80,
        BitrateValue.CBR96,
        BitrateValue.CBR112,
        BitrateValue.CBR128,
        BitrateValue.CBR160,
        BitrateValue.CBR192,
        BitrateValue.CBR224,
        BitrateValue.CBR256,
        BitrateValue.CBR320
    )

    private val mpeg1LayerIIBitrates = listOf(
        BitrateValue.CBR32,
        BitrateValue.CBR48,
        BitrateValue.CBR56,
        BitrateValue.CBR64,
        BitrateValue.CBR80,
        BitrateValue.CBR96,
        BitrateValue.CBR112,
        BitrateValue.CBR128,
        BitrateValue.CBR160,
        BitrateValue.CBR192,
        BitrateValue.CBR224,
        BitrateValue.CBR256,
        BitrateValue.CBR320,
        BitrateValue.CBR384
    )

    private val mpeg2LayerIIBitrates = listOf(
        BitrateValue.CBR16,
        BitrateValue.CBR32,
        BitrateValue.CBR40,
        BitrateValue.CBR48,
        BitrateValue.CBR56,
        BitrateValue.CBR64,
        BitrateValue.CBR80,
        BitrateValue.CBR96,
        BitrateValue.CBR112,
        BitrateValue.CBR128,
        BitrateValue.CBR160
    )

    private val mpeg1AndMpeg2LayerIIBitrates =
        mpeg1LayerIIBitrates.filter { it in mpeg2LayerIIBitrates }

    private val ac3Bitrates = listOf(
        BitrateValue.CBR32,
        BitrateValue.CBR40,
        BitrateValue.CBR48,
        BitrateValue.CBR56,
        BitrateValue.CBR64,
        BitrateValue.CBR80,
        BitrateValue.CBR96,
        BitrateValue.CBR112,
        BitrateValue.CBR128,
        BitrateValue.CBR160,
        BitrateValue.CBR192,
        BitrateValue.CBR224,
        BitrateValue.CBR256,
        BitrateValue.CBR320,
        BitrateValue.CBR384,
        BitrateValue.CBR448,
        BitrateValue.CBR512
    )

    // libvorbis' bitrate management refuses targets below roughly 48 kbps at CD sample rates.
    private val vorbisBitrates =
        generalPurposeBitrates.dropWhile { it != BitrateValue.CBR48 }

    private val amrNarrowBandBitrates = listOf(
        BitrateValue.CBR4_75,
        BitrateValue.CBR5_15,
        BitrateValue.CBR5_9,
        BitrateValue.CBR6_7,
        BitrateValue.CBR7_4,
        BitrateValue.CBR7_95,
        BitrateValue.CBR10_2,
        BitrateValue.CBR12_2
    )

    private const val MPEG1_MIN_SAMPLE_RATE = 32_000
}
