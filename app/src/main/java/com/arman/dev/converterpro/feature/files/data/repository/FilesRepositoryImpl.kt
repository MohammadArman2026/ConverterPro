package com.arman.dev.converterpro.feature.files.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.arman.dev.converterpro.core.common.Utils
import com.arman.dev.converterpro.feature.files.data.cache.CachedConvertedFile
import com.arman.dev.converterpro.feature.files.data.cache.ConvertedFilesCache
import com.arman.dev.converterpro.feature.files.data.cache.mergeConvertedFiles
import com.arman.dev.converterpro.feature.files.domain.model.ConvertedFile
import com.arman.dev.converterpro.feature.files.domain.repository.FilesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Reads back the files the converter wrote into its `ConverterPro` output folder via MediaStore,
 * overlaying cached bitrate, channels, and duration so revisits do not reopen every file.
 */
class FilesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cache: ConvertedFilesCache
) : FilesRepository {

    override fun peekCachedFiles(): List<ConvertedFile> = cache.peek().toConvertedFiles()

    override fun convertedFiles(): Flow<Result<List<ConvertedFile>>> = flow {
        val cached = cache.peek()
        if (cached.isNotEmpty()) {
            emit(Result.success(cached.toConvertedFiles()))
        }

        val store = queryConvertedFiles()
        val merged = mergeConvertedFiles(store.map { it.toCached() }, cached)
            .toConvertedFiles()
        emit(Result.success(merged))

        if (merged.isEmpty()) {
            cache.replaceAll(emptyList())
            return@flow
        }

        val detailed = coroutineScope {
            merged.map { entry ->
                async(metadataDispatcher) {
                    if (entry.toCached().needsTrackDetails) entry.withTrackDetails() else entry
                }
            }.awaitAll()
        }
        cache.replaceAll(detailed.map { it.toCached() })
        emit(Result.success(detailed))
    }.catch { error ->
        emit(Result.failure(error))
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteConvertedFile(uri: Uri): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val deletedRows = context.contentResolver.delete(uri, null, null)
                if (deletedRows > 0) {
                    cache.remove(ContentUris.parseId(uri))
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

    /**
     * Reads the generic files collection rather than the audio collection.
     *
     * Containers whose MIME type the platform cannot resolve, such as WavPack, are written outside
     * the audio collection, so querying only audio would hide them from this screen.
     */
    private fun queryConvertedFiles(): List<ConvertedFile> {
        val collection = filesCollection()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DURATION
        )

        val (selection, selectionArgs) = buildFolderSelection()

        val cursor = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        ) ?: return emptyList()

        return cursor.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)

            buildList {
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    add(
                        ConvertedFile(
                            id = id,
                            uri = ContentUris.withAppendedId(collection, id),
                            name = it.getString(nameColumn) ?: "Unknown file",
                            sizeBytes = it.getLong(sizeColumn),
                            durationMs = if (it.isNull(durationColumn)) 0L else it.getLong(durationColumn),
                            bitrateKbps = null,
                            channels = null
                        )
                    )
                }
            }
        }
    }

    /**
     * Matches the output folder under any parent directory.
     *
     * Recognised audio formats are written to `Music/ConverterPro`, but containers whose MIME type
     * the platform cannot resolve are only allowed under `Download` or `Documents`, so the parent
     * is left as a wildcard. The surrounding separators keep the folder's own row out of the
     * results, since that row is named after the folder and sits one level up.
     */
    @Suppress("DEPRECATION")
    private fun buildFolderSelection(): Pair<String, Array<String>> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? AND " +
                    "${MediaStore.MediaColumns.IS_PENDING} = 0" to arrayOf("%/$OUTPUT_FOLDER/%")
        } else {
            "${MediaStore.MediaColumns.DATA} LIKE ?" to arrayOf("%/$OUTPUT_FOLDER/%")
        }

    /**
     * MediaStore exposes no channel count and no bitrate below API 31, so both are read from the
     * audio track itself and only estimated when the track omits them. Duration is read here too,
     * because rows outside the audio collection carry no duration.
     */
    private fun ConvertedFile.withTrackDetails(): ConvertedFile {
        val extractor = MediaExtractor()

        return try {
            extractor.setDataSource(context, uri, null)

            var trackBitrate: Int? = null
            var trackChannels: Int? = null
            var trackDurationMs = durationMs

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
                if (trackDurationMs <= 0L && format.containsKey(MediaFormat.KEY_DURATION)) {
                    trackDurationMs = format.getLong(MediaFormat.KEY_DURATION) / 1_000
                }
                break
            }

            copy(
                durationMs = trackDurationMs,
                bitrateKbps = trackBitrate
                    ?: Utils.estimateBitrateKbps(sizeBytes, trackDurationMs),
                channels = trackChannels
            )
        } catch (e: Exception) {
            copy(bitrateKbps = Utils.estimateBitrateKbps(sizeBytes, durationMs))
        } finally {
            extractor.release()
        }
    }

    private fun List<CachedConvertedFile>.toConvertedFiles(): List<ConvertedFile> {
        val collection = filesCollection()
        return map { cached ->
            ConvertedFile(
                id = cached.id,
                uri = ContentUris.withAppendedId(collection, cached.id),
                name = cached.name,
                sizeBytes = cached.sizeBytes,
                durationMs = cached.durationMs,
                bitrateKbps = cached.bitrateKbps,
                channels = cached.channels
            )
        }
    }

    private fun ConvertedFile.toCached(): CachedConvertedFile = CachedConvertedFile(
        id = id,
        name = name,
        sizeBytes = sizeBytes,
        durationMs = durationMs,
        bitrateKbps = bitrateKbps,
        channels = channels
    )

    /**
     * Each [MediaExtractor] holds a native decoder, so metadata reads are capped rather than fanned
     * out across the whole IO pool.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val metadataDispatcher = Dispatchers.IO.limitedParallelism(METADATA_PARALLELISM)

    private companion object {
        const val OUTPUT_FOLDER = "ConverterPro"
        const val EXTERNAL_VOLUME = "external"
        const val METADATA_PARALLELISM = 4

        fun filesCollection(): Uri = MediaStore.Files.getContentUri(EXTERNAL_VOLUME)
    }
}
