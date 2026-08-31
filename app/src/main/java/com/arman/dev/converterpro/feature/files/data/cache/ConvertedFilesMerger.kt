package com.arman.dev.converterpro.feature.files.data.cache

/**
 * MediaStore is the source of truth for which files exist. Cached bitrate, channels, and
 * duration are reused only when the same id, size, and name are still on disk.
 */
fun mergeConvertedFiles(
    store: List<CachedConvertedFile>,
    cache: List<CachedConvertedFile>
): List<CachedConvertedFile> {
    val cacheById = cache.associateBy { it.id }
    return store.map { storeFile ->
        val cached = cacheById[storeFile.id] ?: return@map storeFile
        if (cached.sizeBytes != storeFile.sizeBytes || cached.name != storeFile.name) {
            storeFile
        } else {
            storeFile.copy(
                durationMs = cached.durationMs.takeIf { it > 0L } ?: storeFile.durationMs,
                bitrateKbps = cached.bitrateKbps,
                channels = cached.channels
            )
        }
    }
}
