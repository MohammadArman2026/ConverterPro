package com.arman.dev.converterpro.feature.files.data.cache

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConvertedFilesMergerTest {

    @Test
    fun reusesCachedMetadataWhenIdAndSizeAndNameMatch() {
        val store = listOf(storeFile(id = 1, name = "a.mp3", sizeBytes = 1_000, durationMs = 0))
        val cache = listOf(
            cachedFile(id = 1, name = "a.mp3", sizeBytes = 1_000, durationMs = 12_000, bitrateKbps = 192, channels = 2)
        )

        val merged = mergeConvertedFiles(store, cache)

        assertEquals(1, merged.size)
        assertEquals(12_000L, merged[0].durationMs)
        assertEquals(192, merged[0].bitrateKbps)
        assertEquals(2, merged[0].channels)
    }

    @Test
    fun dropsCacheRowsMissingFromStore() {
        val store = listOf(storeFile(id = 2, name = "kept.mp3", sizeBytes = 500))
        val cache = listOf(
            cachedFile(id = 1, name = "gone.mp3", sizeBytes = 400, durationMs = 8_000, bitrateKbps = 128, channels = 2),
            cachedFile(id = 2, name = "kept.mp3", sizeBytes = 500, durationMs = 9_000, bitrateKbps = 256, channels = 1)
        )

        val merged = mergeConvertedFiles(store, cache)

        assertEquals(listOf(2L), merged.map { it.id })
        assertEquals(256, merged[0].bitrateKbps)
    }

    @Test
    fun treatsSizeChangeAsNewFileWithoutReusedMetadata() {
        val store = listOf(storeFile(id = 1, name = "a.mp3", sizeBytes = 2_000, durationMs = 3_000))
        val cache = listOf(
            cachedFile(id = 1, name = "a.mp3", sizeBytes = 1_000, durationMs = 12_000, bitrateKbps = 192, channels = 2)
        )

        val merged = mergeConvertedFiles(store, cache)

        assertEquals(2_000L, merged[0].sizeBytes)
        assertEquals(3_000L, merged[0].durationMs)
        assertNull(merged[0].bitrateKbps)
        assertNull(merged[0].channels)
    }

    @Test
    fun treatsNameChangeAsNewFileWithoutReusedMetadata() {
        val store = listOf(storeFile(id = 1, name = "renamed.mp3", sizeBytes = 1_000, durationMs = 3_000))
        val cache = listOf(
            cachedFile(id = 1, name = "a.mp3", sizeBytes = 1_000, durationMs = 12_000, bitrateKbps = 192, channels = 2)
        )

        val merged = mergeConvertedFiles(store, cache)

        assertEquals("renamed.mp3", merged[0].name)
        assertNull(merged[0].bitrateKbps)
        assertNull(merged[0].channels)
    }

    @Test
    fun passesThroughNewStoreIdsWithoutCacheMetadata() {
        val store = listOf(storeFile(id = 9, name = "new.flac", sizeBytes = 4_000, durationMs = 1_000))
        val cache = emptyList<CachedConvertedFile>()

        val merged = mergeConvertedFiles(store, cache)

        assertEquals(store, merged)
        assertNull(merged[0].bitrateKbps)
        assertNull(merged[0].channels)
    }

    @Test
    fun preservesStoreOrder() {
        val store = listOf(
            storeFile(id = 3, name = "newest.mp3", sizeBytes = 100),
            storeFile(id = 1, name = "older.mp3", sizeBytes = 200)
        )
        val cache = listOf(
            cachedFile(id = 1, name = "older.mp3", sizeBytes = 200, durationMs = 5_000, bitrateKbps = 128, channels = 2),
            cachedFile(id = 3, name = "newest.mp3", sizeBytes = 100, durationMs = 6_000, bitrateKbps = 320, channels = 2)
        )

        val merged = mergeConvertedFiles(store, cache)

        assertEquals(listOf(3L, 1L), merged.map { it.id })
        assertEquals(320, merged[0].bitrateKbps)
        assertEquals(128, merged[1].bitrateKbps)
    }

    private fun storeFile(
        id: Long,
        name: String,
        sizeBytes: Long,
        durationMs: Long = 0L
    ) = CachedConvertedFile(
        id = id,
        name = name,
        sizeBytes = sizeBytes,
        durationMs = durationMs,
        bitrateKbps = null,
        channels = null
    )

    private fun cachedFile(
        id: Long,
        name: String,
        sizeBytes: Long,
        durationMs: Long,
        bitrateKbps: Int?,
        channels: Int?
    ) = CachedConvertedFile(
        id = id,
        name = name,
        sizeBytes = sizeBytes,
        durationMs = durationMs,
        bitrateKbps = bitrateKbps,
        channels = channels
    )
}
