package com.arman.dev.converterpro.feature.player.presentation

import com.arman.dev.converterpro.core.player.RepeatMode

data class PlayerUiState(
    val fileName: String = "",
    val statusLabel: String = "Nothing playing",
    val subtitle: String = "",
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val progress: Float = 0f,
    val positionLabel: String = "0:00",
    val durationLabel: String = "0:00",
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val hasTrack: Boolean = false,
    val error: String? = null
)
