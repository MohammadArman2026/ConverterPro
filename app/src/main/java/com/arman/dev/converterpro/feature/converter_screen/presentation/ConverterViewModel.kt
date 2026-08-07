package com.arman.dev.converterpro.feature.converter_screen.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.converter_screen.domain.model.BitRate
import com.arman.dev.converterpro.feature.converter_screen.domain.model.BitrateValue
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Channel
import com.arman.dev.converterpro.feature.converter_screen.domain.model.ConversionSettings
import com.arman.dev.converterpro.feature.converter_screen.domain.model.ConversionState
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Encoder
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Extension
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Map
import com.arman.dev.converterpro.feature.converter_screen.domain.model.SampleRate
import com.arman.dev.converterpro.feature.converter_screen.domain.model.bitsPerSecond
import com.arman.dev.converterpro.feature.converter_screen.domain.model.channelCount
import com.arman.dev.converterpro.feature.converter_screen.domain.model.containerFormat
import com.arman.dev.converterpro.feature.converter_screen.domain.model.ffmpegEncoder
import com.arman.dev.converterpro.feature.converter_screen.domain.model.hertz
import com.arman.dev.converterpro.feature.converter_screen.domain.model.mimeType
import com.arman.dev.converterpro.feature.converter_screen.domain.model.qualityScale
import com.arman.dev.converterpro.feature.converter_screen.domain.repository.ConverterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val converterRepository: ConverterRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState = _uiState.asStateFlow()

    init {
        initialize()
    }

    /**
     * Initializes all dependent dropdowns using the default extension (AAC).
     */
    private fun initialize() {
        updateExtension(_uiState.value.selectedExtension)
    }

    fun setMediaFile(mediaFiles: List<MediaFile>) {
        _uiState.update {
            it.copy(mediaFile = mediaFiles)
        }
        updateAllFileNames()
    }

    fun onExtensionSelected(extension: Extension) {
        updateExtension(extension)
        updateAllFileNames()
    }

    private fun updateExtension(extension: Extension) {
        val encoderList = Map.extensionToEncoding[extension].orEmpty()
        val selectedEncoder = encoderList.firstOrNull() ?: return

        _uiState.update {
            it.copy(
                selectedExtension = extension,
                encoderList = encoderList,
                selectedEncoder = selectedEncoder
            )
        }

        updateEncoder(selectedEncoder)
    }

    fun onEncodingSelected(encoder: Encoder) {
        _uiState.update {
            it.copy(selectedEncoder = encoder)
        }
        updateEncoder(encoder)
    }

    private fun updateEncoder(encoder: Encoder) {
        val bitrateList = Map.encodingToBitrate[encoder].orEmpty()
        val sampleRateList = Map.encodingToSampleRate[encoder].orEmpty()

        val selectedBitrate = bitrateList.firstOrNull() ?: BitRate.AUTO
        val selectedSampleRate = sampleRateList.firstOrNull() ?: SampleRate.AUTO

        _uiState.update {
            it.copy(
                bitrateList = bitrateList,
                selectedBitRate = selectedBitrate,
                sampleRateList = sampleRateList,
                selectedSampleRate = selectedSampleRate
            )
        }

        refreshBitrateValues()
    }

    fun onBitrateSelected(bitRate: BitRate) {
        _uiState.update {
            it.copy(selectedBitRate = bitRate)
        }
        refreshBitrateValues()
    }

    /**
     * Recomputes the bitrate dropdown from the current encoder, rate control mode and sample rate.
     *
     * The sample rate participates because MPEG layer II switches bitrate tables at 32 kHz. An
     * already valid selection is kept so that changing an unrelated dropdown does not silently
     * reset the user's choice.
     */
    private fun refreshBitrateValues() {
        val state = _uiState.value
        val bitrateValues = Map.bitrateValuesFor(
            encoder = state.selectedEncoder,
            bitRate = state.selectedBitRate,
            sampleRate = state.selectedSampleRate
        )

        _uiState.update {
            it.copy(
                bitrateValues = bitrateValues,
                selectedBitrateValue = it.selectedBitrateValue
                    .takeIf { selected -> selected in bitrateValues }
                    ?: bitrateValues.firstOrNull()
                    ?: BitrateValue.AUTO
            )
        }
    }

    fun onBitrateValueSelected(value: BitrateValue) {
        _uiState.update {
            it.copy(selectedBitrateValue = value)
        }
    }

    fun onChannelSelected(channel: Channel) {
        _uiState.update {
            it.copy(selectedChannel = channel)
        }
    }

    fun onSampleRateSelected(sampleRate: SampleRate) {
        _uiState.update {
            it.copy(selectedSampleRate = sampleRate)
        }
        refreshBitrateValues()
    }

    private fun updateAllFileNames() {
        val extension = _uiState.value.selectedExtension.dropDown
        val updatedFiles = _uiState.value.mediaFile.map { media ->
            val baseName = media.name
                ?.substringBeforeLast('.')
                ?: ""
            media.copy(name = "$baseName.$extension")
        }
        _uiState.update {
            it.copy(mediaFile = updatedFiles)
        }
    }

    fun onSingleFileNameChange(newName: String, uri: Uri) {
        val extension = _uiState.value.selectedExtension.dropDown
        val updatedFiles = _uiState.value.mediaFile.map { media ->
            if (media.uri == uri) {
                media.copy(name = "$newName.$extension")
            } else {
                media
            }
        }
        _uiState.update {
            it.copy(mediaFile = updatedFiles)
        }
    }

    fun convert() {
        if (_uiState.value.conversionState !is ConversionState.Idle) return

        viewModelScope.launch(Dispatchers.IO) {
            val result = converterRepository.convert(
                files = _uiState.value.mediaFile,
                settings = buildConversionSettings(),
                onProgress = ::updateConversionState,
            )

            result
                .onSuccess { convertedCount ->
                    updateConversionState(ConversionState.Completed(convertedCount))
                    delay(COMPLETED_STATE_VISIBLE_MS)
                    updateConversionState(ConversionState.Idle)
                }
                .onFailure { error ->
                    updateConversionState(
                        ConversionState.Failed(error.message ?: "Conversion failed."),
                    )
                }
        }
    }

    fun dismissConversionError() {
        if (_uiState.value.conversionState is ConversionState.Failed) {
            updateConversionState(ConversionState.Idle)
        }
    }

    private fun buildConversionSettings(): ConversionSettings {
        val state = _uiState.value
        val isVariableBitrate = state.selectedBitRate == BitRate.VBR
        return ConversionSettings(
            outputExtension = state.selectedExtension.dropDown,
            containerFormat = state.selectedExtension.containerFormat,
            mimeType = state.selectedExtension.mimeType,
            encoder = state.selectedEncoder.ffmpegEncoder,
            bitrateBitsPerSecond = state.selectedBitrateValue.bitsPerSecond
                .takeUnless { isVariableBitrate },
            qualityScale = state.selectedBitrateValue.qualityScale
                .takeIf { isVariableBitrate },
            sampleRateHz = state.selectedSampleRate.hertz,
            channelCount = state.selectedChannel.channelCount,
        )
    }

    private fun updateConversionState(state: ConversionState) {
        _uiState.update { it.copy(conversionState = state) }
    }

    private companion object {
        const val COMPLETED_STATE_VISIBLE_MS = 1_000L
    }
}
