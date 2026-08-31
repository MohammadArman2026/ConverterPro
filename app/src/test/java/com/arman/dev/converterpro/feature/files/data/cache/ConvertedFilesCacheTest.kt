package com.arman.dev.converterpro.feature.files.data.cache

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ConvertedFilesCacheTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun peekReturnsEmptyListWhenFileIsMissing() {
        val cache = ConvertedFilesCache(tempFolder.newFile("converted_files_cache.json").also { it.delete() })

        assertTrue(cache.peek().isEmpty())
    }

    @Test
    fun replaceAllRoundTripsThroughANewCacheInstance() {
        val file = tempFolder.newFile("converted_files_cache.json")
        val original = ConvertedFilesCache(file)
        val stored = listOf(
            CachedConvertedFile(
                id = 11,
                name = "track_converted.mp3",
                sizeBytes = 4_096,
                durationMs = 15_000,
                bitrateKbps = 192,
                channels = 2
            )
        )

        original.replaceAll(stored)

        val reopened = ConvertedFilesCache(file)
        assertEquals(stored, reopened.peek())
    }

    @Test
    fun roundTripPreservesNullBitrateAndChannelsAndQuotedNames() {
        val file = tempFolder.newFile("converted_files_cache.json")
        val original = ConvertedFilesCache(file)
        val stored = listOf(
            CachedConvertedFile(
                id = 7,
                name = "song \"quoted\" \\ name.wv",
                sizeBytes = 800,
                durationMs = 2_000,
                bitrateKbps = null,
                channels = null
            )
        )

        original.replaceAll(stored)

        assertEquals(stored, ConvertedFilesCache(file).peek())
    }

    @Test
    fun upsertInsertsUnknownIdAtTheFront() {
        val cache = ConvertedFilesCache(tempFolder.newFile("converted_files_cache.json"))
        cache.replaceAll(
            listOf(
                CachedConvertedFile(1, "old.mp3", 100, 1_000, 128, 2)
            )
        )
        val newest = CachedConvertedFile(2, "new.mp3", 200, 2_000, 256, 2)

        cache.upsert(newest)

        assertEquals(listOf(2L, 1L), cache.peek().map { it.id })
    }

    @Test
    fun upsertReplacesExistingIdInPlace() {
        val cache = ConvertedFilesCache(tempFolder.newFile("converted_files_cache.json"))
        cache.replaceAll(
            listOf(
                CachedConvertedFile(5, "a.mp3", 100, 1_000, 128, 2),
                CachedConvertedFile(6, "b.mp3", 200, 2_000, 192, 2)
            )
        )
        val updated = CachedConvertedFile(5, "a.mp3", 150, 1_000, 320, 1)

        cache.upsert(updated)

        assertEquals(listOf(5L, 6L), cache.peek().map { it.id })
        assertEquals(updated, cache.peek()[0])
    }

    @Test
    fun removeDropsMatchingIdAndPersists() {
        val file = tempFolder.newFile("converted_files_cache.json")
        val cache = ConvertedFilesCache(file)
        cache.replaceAll(
            listOf(
                CachedConvertedFile(1, "a.mp3", 100, 1_000, 128, 2),
                CachedConvertedFile(2, "b.mp3", 200, 2_000, 192, 2)
            )
        )

        cache.remove(1)

        assertEquals(listOf(2L), cache.peek().map { it.id })
        assertEquals(listOf(2L), ConvertedFilesCache(file).peek().map { it.id })
    }

    @Test
    fun snapshotDoesNotReadDiskUntilPeek() {
        val file = tempFolder.newFile("converted_files_cache.json")
        ConvertedFilesCache(file).replaceAll(
            listOf(CachedConvertedFile(3, "c.mp3", 300, 3_000, 128, 2))
        )

        val cold = ConvertedFilesCache(file)
        assertTrue(cold.snapshot().isEmpty())
        assertEquals(listOf(3L), cold.peek().map { it.id })
        assertEquals(listOf(3L), cold.snapshot().map { it.id })
    }
}
