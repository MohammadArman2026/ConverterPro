package com.arman.dev.converterpro.feature.converter_screen.ui
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.converter_screen.domain.model.BitRate
import com.arman.dev.converterpro.feature.converter_screen.domain.model.BitrateValue
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Channel
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Encoder
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Extension
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Map
import com.arman.dev.converterpro.feature.converter_screen.domain.model.SampleRate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor() : ViewModel() {

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

    /**
     * ---------------------------------------------------------
     * Extension
     * ---------------------------------------------------------
     */
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

    /**
     * ---------------------------------------------------------
     * Encoder
     * ---------------------------------------------------------
     */
    fun onEncodingSelected(encoder: Encoder) {

        _uiState.update {
            it.copy(selectedEncoder = encoder)
        }

        updateEncoder(encoder)
    }

    private fun updateEncoder(encoder: Encoder) {

        val bitrateList = Map.encodingToBitrate[encoder].orEmpty()
        val sampleRateList = Map.encodingToSampleRate[encoder].orEmpty()

        val selectedBitrate =
            bitrateList.firstOrNull() ?: BitRate.AUTO

        val selectedSampleRate =
            sampleRateList.firstOrNull() ?: SampleRate.AUTO

        _uiState.update {
            it.copy(
                bitrateList = bitrateList,
                selectedBitRate = selectedBitrate,
                sampleRateList = sampleRateList,
                selectedSampleRate = selectedSampleRate
            )
        }

        updateBitrate(
            encoder = encoder,
            bitrate = selectedBitrate
        )
    }

    /**
     * ---------------------------------------------------------
     * Bitrate
     * ---------------------------------------------------------
     */
    fun onBitrateSelected(bitRate: BitRate) {

        _uiState.update {
            it.copy(selectedBitRate = bitRate)
        }

        updateBitrate(
            encoder = _uiState.value.selectedEncoder,
            bitrate = bitRate
        )
    }

    private fun updateBitrate(
        encoder: Encoder,
        bitrate: BitRate
    ) {

        val bitrateValues = when {
            encoder == Encoder.AMR_NB &&
                    bitrate == BitRate.CBR ->
                Map.encodingToCBRValue[encoder].orEmpty()

            else ->
                Map.BitrateToBitrateValue[bitrate].orEmpty()
        }

        _uiState.update {
            it.copy(
                bitrateValues = bitrateValues,
                selectedBitrateValue = bitrateValues.firstOrNull()
                    ?: BitrateValue.AUTO
            )
        }
    }

    /**
     * ---------------------------------------------------------
     * Bitrate Value
     * ---------------------------------------------------------
     */
    fun onBitrateValueSelected(value: BitrateValue) {
        _uiState.update {
            it.copy(selectedBitrateValue = value)
        }
    }

    /**
     * ---------------------------------------------------------
     * Channel
     * ---------------------------------------------------------
     */
    fun onChannelSelected(channel: Channel) {
        _uiState.update {
            it.copy(selectedChannel = channel)
        }
    }

    /**
     * ---------------------------------------------------------
     * Sample Rate
     * ---------------------------------------------------------
     */
    fun onSampleRateSelected(sampleRate: SampleRate) {
        _uiState.update {
            it.copy(selectedSampleRate = sampleRate)
        }
    }

    /**
     * ---------------------------------------------------------
     * File Name
     * ---------------------------------------------------------
     */
    private fun updateAllFileNames() {

        val extension = _uiState.value.selectedExtension.dropDown

        val updatedFiles = _uiState.value.mediaFile.map { media ->

            val baseName = media.name
                ?.substringBeforeLast('.')
                ?: ""

            media.copy(
                name = "$baseName.$extension"
            )
        }

        _uiState.update {
            it.copy(mediaFile = updatedFiles)
        }
    }

    fun onSingleFileNameChange(
        newName: String,
        uri: Uri
    ) {

        val extension = _uiState.value.selectedExtension.dropDown

        val updatedFiles = _uiState.value.mediaFile.map { media ->

            if (media.uri == uri) {
                media.copy(
                    name = "$newName.$extension"
                )
            } else {
                media
            }
        }

        _uiState.update {
            it.copy(mediaFile = updatedFiles)
        }
    }
}