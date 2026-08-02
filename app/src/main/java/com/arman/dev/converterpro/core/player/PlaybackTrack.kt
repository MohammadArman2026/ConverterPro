package com.arman.dev.converterpro.core.player

import android.net.Uri

/**
 * A queue entry for the player. [subtitle] is pre-formatted by the feature that builds the queue so
 * the playback engine stays free of presentation concerns.
 */
data class PlaybackTrack(
    val id: Long,
    val uri: Uri,
    val title: String,
    val subtitle: String,
    val durationMs: Long
)
