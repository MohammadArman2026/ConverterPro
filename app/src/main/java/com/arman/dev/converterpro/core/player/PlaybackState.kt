package com.arman.dev.converterpro.core.player

data class PlaybackState(
    val queue: List<PlaybackTrack> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val error: String? = null
) {
    val currentTrack: PlaybackTrack? get() = queue.getOrNull(currentIndex)
}
