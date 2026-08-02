package com.arman.dev.converterpro.feature.files.domain.model

import android.net.Uri
import com.arman.dev.converterpro.core.common.Utils
import com.arman.dev.converterpro.core.player.PlaybackTrack

data class ConvertedFile(
    val id: Long,
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val bitrateKbps: Int?,
    val channels: Int?
) {
    /** Upper-cased container extension, e.g. `AAC`. */
    val format: String = name.substringAfterLast('.', "").uppercase().ifBlank { "AUDIO" }

    /** `AAC · 3.2 MB · 02:41` */
    val listSubtitle: String = listOf(
        format,
        Utils.formatFileSize(sizeBytes),
        Utils.formatDuration(durationMs, padMinutes = true)
    ).joinToString(SEPARATOR)

    /** `AAC · 256 kbps · Stereo` */
    val playerSubtitle: String = listOfNotNull(
        format,
        bitrateKbps?.let { "$it kbps" },
        Utils.channelLabel(channels)
    ).joinToString(SEPARATOR)

    fun toPlaybackTrack(): PlaybackTrack = PlaybackTrack(
        id = id,
        uri = uri,
        title = name,
        subtitle = playerSubtitle,
        durationMs = durationMs
    )

    private companion object {
        const val SEPARATOR = " · "
    }
}
