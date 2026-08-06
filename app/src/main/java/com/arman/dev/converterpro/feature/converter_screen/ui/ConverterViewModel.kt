package com.arman.dev.converterpro.feature.converter_screen.ui
import android.net.Uri
import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import java.io.File
import java.util.UUID
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arman.dev.converterpro.core.ffmpeg.FfmpegConversionCommand
import com.arman.dev.converterpro.core.ffmpeg.FfmpegNative
import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.converter_screen.domain.model.BitRate
import com.arman.dev.converterpro.feature.converter_screen.domain.model.BitrateValue
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Channel
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Encoder
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Extension
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Map
import com.arman.dev.converterpro.feature.converter_screen.domain.model.SampleRate
import com.arman.dev.converterpro.feature.converter_screen.domain.model.channelCount
import com.arman.dev.converterpro.feature.converter_screen.domain.model.containerFormat
import com.arman.dev.converterpro.feature.converter_screen.domain.model.ffmpegEncoder
import com.arman.dev.converterpro.feature.converter_screen.domain.model.bitsPerSecond
import com.arman.dev.converterpro.feature.converter_screen.domain.model.hertz
import com.arman.dev.converterpro.feature.converter_screen.domain.model.mimeType
import com.arman.dev.converterpro.feature.converter_screen.domain.model.qualityScale
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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

        refreshBitrateValues()
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

        refreshBitrateValues()
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

    fun convert() {
        if (_uiState.value.conversionState !is ConversionState.Idle) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val files = _uiState.value.mediaFile
                require(files.isNotEmpty()) { "Select at least one audio file." }

                updateConversionState(ConversionState.PreparingSpace)
                ensureEnoughStorage(files.sumOf(MediaFile::size))

                var convertedCount = 0
                files.forEach { input ->
                    updateConversionState(ConversionState.NamingFile)
                    val outputUri = createOutputFile(input)

                    try {
                        updateConversionState(ConversionState.Converting)
                        convertFile(input, outputUri)
                        markOutputReady(outputUri)
                        convertedCount++
                    } catch (error: Exception) {
                        context.contentResolver.delete(outputUri, null, null)
                        throw error
                    }
                }

                updateConversionState(ConversionState.Completed(convertedCount))
                delay(COMPLETED_STATE_VISIBLE_MS)
                updateConversionState(ConversionState.Idle)
            } catch (error: Exception) {
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

    private fun convertFile(input: MediaFile, outputUri: Uri) {
        val cachedInput = copyInputToCache(input)
        val outputDescriptor = context.contentResolver.openFileDescriptor(outputUri, "rw")
            ?: error("Unable to create the output file.")

        try {
            val command = buildConversionCommand(
                inputPath = cachedInput.absolutePath,
                outputFileDescriptor = outputDescriptor.detachFd(),
            )
            FfmpegNative.convert(command)?.let(::error)
        } finally {
            cachedInput.delete()
        }
    }

    private fun buildConversionCommand(
        inputPath: String,
        outputFileDescriptor: Int,
    ): FfmpegConversionCommand {
        val state = _uiState.value
        val isVariableBitrate = state.selectedBitRate == BitRate.VBR
        return FfmpegConversionCommand(
            inputPath = inputPath,
            outputFileDescriptor = outputFileDescriptor,
            containerFormat = state.selectedExtension.containerFormat,
            encoder = state.selectedEncoder.ffmpegEncoder,
            bitrateBitsPerSecond = state.selectedBitrateValue.bitsPerSecond
                .takeUnless { isVariableBitrate },
            qualityScale = state.selectedBitrateValue.qualityScale
                .takeIf { isVariableBitrate },
            sampleRate = state.selectedSampleRate.hertz,
            channelCount = state.selectedChannel.channelCount,
        )
    }

    private fun copyInputToCache(input: MediaFile): File {
        val cacheFile = File(context.cacheDir, "conversion-${UUID.randomUUID()}.media")
        context.contentResolver.openInputStream(input.uri)?.use { source ->
            cacheFile.outputStream().use(source::copyTo)
        } ?: error("Unable to open ${input.name ?: "the input file"}.")
        return cacheFile
    }

    private fun createOutputFile(input: MediaFile): Uri {
        val state = _uiState.value
        val outputName = input.name
            ?.substringBeforeLast('.')
            .orEmpty()
            .ifBlank { "audio" }
            .plus("_converted.${state.selectedExtension.dropDown}")

        return outputTargets(state.selectedExtension)
            .firstNotNullOfOrNull { target -> reserveOutputFile(outputName, target) }
            ?: error("Unable to reserve output storage.")
    }

    /**
     * Where a converted file may be stored, in order of preference.
     *
     * MediaStore rejects any MIME type the platform cannot map back to a file extension, and niche
     * containers such as WavPack are absent from that table on many builds. Such files resolve to
     * media type "none", which MediaStore only permits under Download or Documents — never Music —
     * so they cannot live alongside the recognised formats.
     */
    private fun outputTargets(extension: Extension): List<OutputTarget> = listOf(
        OutputTarget(
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            mimeType = extension.mimeType,
            primaryDirectory = Environment.DIRECTORY_MUSIC,
        ),
        OutputTarget(
            collection = MediaStore.Files.getContentUri(EXTERNAL_VOLUME),
            mimeType = null,
            primaryDirectory = Environment.DIRECTORY_DOWNLOADS,
        ),
        OutputTarget(
            collection = MediaStore.Files.getContentUri(EXTERNAL_VOLUME),
            mimeType = null,
            primaryDirectory = Environment.DIRECTORY_DOCUMENTS,
        ),
    )

    private fun reserveOutputFile(displayName: String, target: OutputTarget): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            target.mimeType?.let { put(MediaStore.MediaColumns.MIME_TYPE, it) }
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${target.primaryDirectory}/$OUTPUT_DIRECTORY_NAME",
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        return try {
            context.contentResolver.insert(target.collection, values)
        } catch (error: IllegalArgumentException) {
            null
        }
    }

    private data class OutputTarget(
        val collection: Uri,
        val mimeType: String?,
        val primaryDirectory: String,
    )

    private fun markOutputReady(outputUri: Uri) {
        context.contentResolver.update(
            outputUri,
            ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
            null,
            null,
        )
    }

    private fun ensureEnoughStorage(inputBytes: Long) {
        val availableBytes = StatFs(context.filesDir.absolutePath).availableBytes
        require(availableBytes > inputBytes) { "Not enough available storage for conversion." }
    }

    private fun updateConversionState(state: ConversionState) {
        _uiState.update { it.copy(conversionState = state) }
    }

    private companion object {
        const val COMPLETED_STATE_VISIBLE_MS = 1_000L
        const val EXTERNAL_VOLUME = "external"
        const val OUTPUT_DIRECTORY_NAME = "ConverterPro"
    }


}
