package com.arman.dev.converterpro.feature.converter_screen.ui

import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.converter_screen.domain.model.BitRate
import com.arman.dev.converterpro.feature.converter_screen.domain.model.BitrateValue
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Channel
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Encoder
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Extension
import com.arman.dev.converterpro.feature.converter_screen.domain.model.SampleRate

data class ConverterUiState(
    val isLoading: Boolean = false ,
    val conversionState: ConversionState = ConversionState.Idle,
    val error: String ? = null,
    val mediaFile: List<MediaFile> = emptyList() ,
    val selectedExtension: Extension = Extension.AAC,
    val selectedEncoder: Encoder = Encoder.AAC,
    val selectedBitRate: BitRate = BitRate.AUTO,
    val selectedBitrateValue: BitrateValue = BitrateValue.AUTO,
    val selectedChannel: Channel = Channel.AUTO,
    val selectedSampleRate: SampleRate = SampleRate.AUTO,
    val extensionList: List<Extension> = Extension.entries.toList(),
    val encoderList: List<Encoder> = emptyList(),
    val bitrateList : List<BitRate> = emptyList(),
    val bitrateValues : List<BitrateValue> = emptyList(),
    val channelList: List<Channel> = Channel.entries.toList(),
    val sampleRateList: List<SampleRate> = emptyList()
)
