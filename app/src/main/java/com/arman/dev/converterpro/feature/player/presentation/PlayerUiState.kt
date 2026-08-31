package com.arman.dev.converterpro.feature.player.presentation

import com.arman.dev.converterpro.core.player.PlaybackTrack
import com.arman.dev.converterpro.core.player.RepeatMode

data class PlayerUiState(
    val fileName: String = "",
    val statusLabel: String = "Nothing playing",
    val subtitle: String = "",
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val currentTrack: PlaybackTrack? = null,
    val bufferedPosition: Long = 0L,
    val isLoading: Boolean = false,
    val isBuffering: Boolean = false,
    val progress: Float = 0f,
    val positionLabel: String = "0:00",
    val durationLabel: String = "0:00",
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val hasTrack: Boolean = false,
    val error: String? = null
)
