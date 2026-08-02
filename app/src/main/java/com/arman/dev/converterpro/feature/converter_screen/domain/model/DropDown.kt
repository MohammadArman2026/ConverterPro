package com.arman.dev.converterpro.feature.converter_screen.domain.model

interface DropDown {
    val dropDown: String
}

enum class Extension(
    override val dropDown: String
) : DropDown {
    WAV("wav"),
    FLAC("flac"),
    AAC("aac"),
    M4A("m4a"),
    AMR("amr"),
    OGG("ogg"),
    MP2("mp2"),
    OPUS("opus"),
    AC3("ac3"),
    WAV_PACK("wv")
}

enum class Encoder(
    override val dropDown: String
) : DropDown {
    WAV("WAV"),
    FLAC("FLAC"),
    AAC("AAC"),
    LIB_VORBIS("LIB_VORBIS"),
    LIB_OPUS("LIB_OPUS"),
    AC3("AC3"),
    AMR_NB("AMR_NB"),
    MP2("MP2"),
    WAV_PACK("WAV_PACK")
}

enum class BitRate(
    override val dropDown: String
) : DropDown {
    AUTO("AUTO"),
    CBR("CBR"),
    VBR("VBR")
}


enum class BitrateValue(
    override val dropDown: String
): DropDown{
    AUTO("Auto"),
    CBR16("16.0"),
    CBR32("32.0"),
    CBR40("40.0"),
    CBR48("48.0"),
    CBR56("56.0"),
    CBR64("64.0"),
    CBR80("80.0"),
    CBR96("96.0"),
    CBR112("112.0"),
    CBR128("128.0"),
    CBR160("160.0"),
    CBR192("192.0"),
    CBR224("224.0"),
    CBR256("256.0"),
    CBR320("320.0"),
    CBR384("384.0"),
    CBR448("448.0"),
    CBR512("512.0"),
    CBR4_75("4.75"),
    CBR5_15("5.15"),
    CBR5_9("5.9"),
    CBR6_7("6.7"),
    CBR7_4("7.4"),
    CBR7_95("7.95"),
    CBR10_2("10.2"),
    CBR12_2("12.2"),
    VBR0("0.0"),
    VBR1("1.0"),
    VBR2("2.0"),
    VBR3("3.0"),
    VBR4("4.0"),
    VBR5("5.0"),
    VBR6("6.0"),
    VBR7("7.0"),
    VBR8("8.0"),
    VBR9("9.0"),
    VBR10("10.0")
}

enum class AutoValue(
    override val dropDown: String
) : DropDown {
    AUTO("Auto")
}



enum class Channel(
    override val dropDown: String
) : DropDown {
    MONO("MONO"),
    STEREO("STEREO"),
    AUTO("AUTO")
}

enum class SampleRate(
    override val dropDown: String
) : DropDown {
    AUTO("Auto"),
    SR0("0"),
    SR8000("8000"),
    SR11025("11025"),
    SR12000("12000"),
    SR16000("16000"),
    SR22050("22050"),
    SR24000("24000"),
    SR32000("32000"),
    SR44100("44100"),
    SR48000("48000"),
    SR96000("96000")
}
