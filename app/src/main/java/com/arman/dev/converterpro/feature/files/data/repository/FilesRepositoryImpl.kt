package com.arman.dev.converterpro.feature.files.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.arman.dev.converterpro.core.common.Utils
import com.arman.dev.converterpro.feature.files.domain.model.ConvertedFile
import com.arman.dev.converterpro.feature.files.domain.repository.FilesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Reads back the files the converter wrote into `Music/ConverterPro` via MediaStore.
 */
class FilesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FilesRepository {

    override suspend fun loadConvertedFiles(): Result<List<ConvertedFile>> =
        withContext(Dispatchers.IO) {
            try {
                val entries = queryConvertedFiles()
                val files = coroutineScope {
                    entries
                        .map { entry -> async { entry.withTrackDetails() } }
                        .awaitAll()
                }
                Result.success(files)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteConvertedFile(uri: Uri): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val deletedRows = context.contentResolver.delete(uri, null, null)
                if (deletedRows > 0) {
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalStateException("That file could not be deleted."))
                }
            } catch (e: SecurityException) {
                Result.failure(IllegalStateException("ConverterPro is not allowed to delete that file."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun queryConvertedFiles(): List<ConvertedFile> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION
        )

        val (selection, selectionArgs) = buildFolderSelection()

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        ) ?: return emptyList()

        return cursor.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            buildList {
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    add(
                        ConvertedFile(
                            id = id,
                            uri = ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                id
                            ),
                            name = it.getString(nameColumn) ?: "Unknown file",
                            sizeBytes = it.getLong(sizeColumn),
                            durationMs = it.getLong(durationColumn),
                            bitrateKbps = null,
                            channels = null
                        )
                    )
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun buildFolderSelection(): Pair<String, Array<String>> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?" to arrayOf("%$OUTPUT_FOLDER%")
        } else {
            "${MediaStore.Audio.Media.DATA} LIKE ?" to arrayOf("%/$OUTPUT_FOLDER/%")
        }

    /**
     * MediaStore exposes no channel count and no bitrate below API 31, so both are read from the
     * audio track itself and only estimated when the track omits them.
     */
    private fun ConvertedFile.withTrackDetails(): ConvertedFile {
        val extractor = MediaExtractor()

        return try {
            extractor.setDataSource(context, uri, null)

            var trackBitrate: Int? = null
            var trackChannels: Int? = null

            for (track in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(track)
                val mimeType = format.getString(MediaFormat.KEY_MIME)
                if (mimeType?.startsWith("audio/") != true) continue

                if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
                    trackBitrate = format.getInteger(MediaFormat.KEY_BIT_RATE) / 1000
                }
                if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                    trackChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                }
                break
            }

            copy(
                bitrateKbps = trackBitrate
                    ?: Utils.estimateBitrateKbps(sizeBytes, durationMs),
                channels = trackChannels
            )
        } catch (e: Exception) {
            copy(bitrateKbps = Utils.estimateBitrateKbps(sizeBytes, durationMs))
        } finally {
            extractor.release()
        }
    }

    private companion object {
        const val OUTPUT_FOLDER = "Music/ConverterPro"
    }
}
