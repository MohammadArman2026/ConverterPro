package com.arman.dev.converterpro.feature.home.data.repository

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.home.domain.repository.HomeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
    HomeRepository {
    override suspend fun processUris(
        newUris: List<Uri>,
        existingUris: List<Uri>
    ): Result<List<MediaFile>> = withContext(Dispatchers.IO) {
        try {
            val existing = existingUris.toSet()
            val mediaFiles = coroutineScope {
                newUris
                    .distinct()
                    .filterNot { it in existing }
                    .map { uri ->
                        async {
                            extractMediaFile(uri)
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
            }
            Result.success(mediaFiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    private fun extractMediaFile(uri: Uri): MediaFile? {

        var codec: String? = null
        var bitrate: Int? = null
        var sampleRate: Int? = null
        var channelCount: Int? = null
        var mimeType : String? = null

        val extractor = MediaExtractor()
        val retriever = MediaMetadataRetriever()

        return try {
            extractor.setDataSource(context, uri, null)

            var hasAudioTrack = false

            for (i in 0 until extractor.trackCount) {

                val format = extractor.getTrackFormat(i)
                 mimeType = format.getString(MediaFormat.KEY_MIME)

                if (mimeType?.startsWith("audio/") == true) {

                    hasAudioTrack = true
                    codec = mimeType

                    if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
                        bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE)
                    }

                    if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                        sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    }

                    if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                        channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    }

                    break
                }
            }

            if (!hasAudioTrack) {
                return null
            }

            var name: String? = null
            var size = 0L

            context.contentResolver.query(
                uri,
                arrayOf(
                    OpenableColumns.DISPLAY_NAME,
                    OpenableColumns.SIZE
                ),
                null,
                null,
                null
            )?.use { cursor ->

                if (cursor.moveToFirst()) {

                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex)
                    }

                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }

            retriever.setDataSource(context, uri)

            val duration = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLong()

            MediaFile(
                uri = uri,
                name = name,
                size = size,
                durationMs = duration,
                codec = codec,
                bitrate = bitrate,
                sampleRate = sampleRate,
                channels = channelCount,
                mimeType = mimeType,
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            extractor.release()
            retriever.release()
        }
    }
}