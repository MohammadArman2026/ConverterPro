package com.arman.dev.converterpro.feature.converter_screen.domain.model

object Map {

    val extensionToEncoding = mapOf(
        Extension.MP3 to listOf(
            Encoder.LIB_MP3_LAME
        ),
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
        Encoder.LIB_MP3_LAME to listOf(
            BitRate.AUTO ,
            BitRate.CBR,
            BitRate.VBR
        ),
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
        Encoder.LIB_OPUS to listOf(
            BitRate.AUTO,
            BitRate.CBR,
            BitRate.VBR
        ),
        Encoder.MP2 to listOf(
            BitRate.AUTO,
            BitRate.CBR,
            BitRate.VBR
        ),
        Encoder.AC3 to listOf(
            BitRate.AUTO,
            BitRate.CBR,
            BitRate.VBR
        ),
        Encoder.WAV_PACK to listOf(
            BitRate.AUTO,
        )
    )

    val BitrateToBitrateValue = mapOf(
        BitRate.AUTO to listOf(
            BitrateValue.AUTO
        ),
        BitRate.CBR to listOf(
            BitrateValue.CBR16,
            BitrateValue.CBR32,
            BitrateValue.CBR40,
            BitrateValue.CBR48,
            BitrateValue.CBR56,
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
        ),
        BitRate.VBR to listOf(
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
        )
    )

    val encodingToSampleRate = mapOf(
        Encoder.LIB_MP3_LAME to listOf(
            SampleRate.AUTO,
            SampleRate.SR8000,
            SampleRate.SR11025,
            SampleRate.SR12000,
            SampleRate.SR16000,
            SampleRate.SR22050,
            SampleRate.SR24000,
            SampleRate.SR32000,
            SampleRate.SR44100,
            SampleRate.SR48000
        ),
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
        )

    )

    val encodingToCBRValue = mapOf(
        Encoder.AMR_NB to listOf(
            BitrateValue.CBR4_75,
            BitrateValue.CBR5_15,
            BitrateValue.CBR5_9,
            BitrateValue.CBR6_7,
            BitrateValue.CBR7_4,
            BitrateValue.CBR7_95,
            BitrateValue.CBR10_2,
            BitrateValue.CBR12_2
        )
    )
}