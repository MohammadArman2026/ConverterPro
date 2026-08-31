package com.arman.dev.converterpro.feature.files.data.cache

/**
 * Disk-friendly snapshot of a converted file. [uri] is rebuilt from [id] when the list is shown.
 */
data class CachedConvertedFile(
    val id: Long,
    val name: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val bitrateKbps: Int?,
    val channels: Int?
) {
    val needsTrackDetails: Boolean
        get() = bitrateKbps == null || channels == null || durationMs <= 0L
}
