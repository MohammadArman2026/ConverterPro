package com.arman.dev.converterpro.core.player

import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

internal const val EXTRA_TRACK_ID = "track_id"
internal const val EXTRA_TRACK_SUBTITLE = "track_subtitle"
internal const val EXTRA_TRACK_DURATION_MS = "track_duration_ms"

@OptIn(UnstableApi::class)
internal fun PlaybackTrack.toMediaItem(): MediaItem {
    val extras = Bundle().apply {
        putLong(EXTRA_TRACK_ID, id)
        putString(EXTRA_TRACK_SUBTITLE, subtitle)
        putLong(EXTRA_TRACK_DURATION_MS, durationMs)
    }
    return MediaItem.Builder()
        .setUri(uri)
        .setMediaId(id.toString())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(subtitle.ifBlank { null })
                .setIsPlayable(true)
                .setExtras(extras)
                .build()
        )
        .build()
}

@OptIn(UnstableApi::class)
internal fun MediaItem.toPlaybackTrack(): PlaybackTrack {
    val extras = mediaMetadata.extras
    val id = mediaId.toLongOrNull()
        ?: extras?.getLong(EXTRA_TRACK_ID)?.takeIf { it != 0L }
        ?: 0L
    val uri = localConfiguration?.uri ?: Uri.EMPTY
    return PlaybackTrack(
        id = id,
        uri = uri,
        title = mediaMetadata.title?.toString() ?: "Unknown",
        subtitle = extras?.getString(EXTRA_TRACK_SUBTITLE)
            ?: mediaMetadata.artist?.toString().orEmpty(),
        durationMs = extras?.getLong(EXTRA_TRACK_DURATION_MS) ?: 0L
    )
}

internal fun Player.queueTracks(): List<PlaybackTrack> =
    (0 until mediaItemCount).map { getMediaItemAt(it).toPlaybackTrack() }

internal fun Int.toRepeatMode(): RepeatMode = when (this) {
    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
    else -> RepeatMode.OFF
}

internal fun RepeatMode.toPlayerRepeatMode(): Int = when (this) {
    RepeatMode.OFF -> Player.REPEAT_MODE_OFF
    RepeatMode.ALL -> Player.REPEAT_MODE_ALL
    RepeatMode.ONE -> Player.REPEAT_MODE_ONE
}
