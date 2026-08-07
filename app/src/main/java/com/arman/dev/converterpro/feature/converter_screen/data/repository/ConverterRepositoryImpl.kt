package com.arman.dev.converterpro.feature.converter_screen.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import com.arman.dev.converterpro.core.ffmpeg.FfmpegConversionCommand
import com.arman.dev.converterpro.core.ffmpeg.FfmpegNative
import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.converter_screen.domain.model.ConversionSettings
import com.arman.dev.converterpro.feature.converter_screen.domain.model.ConversionState
import com.arman.dev.converterpro.feature.converter_screen.domain.repository.ConverterRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class ConverterRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ConverterRepository {

    override suspend fun convert(
        files: List<MediaFile>,
        settings: ConversionSettings,
        onProgress: (ConversionState) -> Unit,
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            require(files.isNotEmpty()) { "Select at least one audio file." }

            onProgress(ConversionState.PreparingSpace)
            ensureEnoughStorage(files.sumOf(MediaFile::size))

            var convertedCount = 0
            files.forEach { input ->
                onProgress(ConversionState.NamingFile)
                val outputUri = createOutputFile(input, settings)

                try {
                    onProgress(ConversionState.Converting)
                    convertFile(input, outputUri, settings)
                    markOutputReady(outputUri)
                    convertedCount++
                } catch (error: Exception) {
                    context.contentResolver.delete(outputUri, null, null)
                    throw error
                }
            }

            Result.success(convertedCount)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun convertFile(
        input: MediaFile,
        outputUri: Uri,
        settings: ConversionSettings,
    ) {
        val cachedInput = copyInputToCache(input)
        val outputDescriptor = context.contentResolver.openFileDescriptor(outputUri, "rw")
            ?: error("Unable to create the output file.")

        try {
            val command = FfmpegConversionCommand(
                inputPath = cachedInput.absolutePath,
                outputFileDescriptor = outputDescriptor.detachFd(),
                containerFormat = settings.containerFormat,
                encoder = settings.encoder,
                bitrateBitsPerSecond = settings.bitrateBitsPerSecond,
                qualityScale = settings.qualityScale,
                sampleRate = settings.sampleRateHz,
                channelCount = settings.channelCount,
            )
            FfmpegNative.convert(command)?.let(::error)
        } finally {
            cachedInput.delete()
        }
    }

    private fun copyInputToCache(input: MediaFile): File {
        val cacheFile = File(context.cacheDir, "conversion-${UUID.randomUUID()}.media")
        context.contentResolver.openInputStream(input.uri)?.use { source ->
            cacheFile.outputStream().use(source::copyTo)
        } ?: error("Unable to open ${input.name ?: "the input file"}.")
        return cacheFile
    }

    private fun createOutputFile(input: MediaFile, settings: ConversionSettings): Uri {
        val outputName = input.name
            ?.substringBeforeLast('.')
            .orEmpty()
            .ifBlank { "audio" }
            .plus("_converted.${settings.outputExtension}")

        return outputTargets(settings.mimeType)
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
    private fun outputTargets(mimeType: String?): List<OutputTarget> = listOf(
        OutputTarget(
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            mimeType = mimeType,
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

    private companion object {
        const val EXTERNAL_VOLUME = "external"
        const val OUTPUT_DIRECTORY_NAME = "ConverterPro"
    }
}
