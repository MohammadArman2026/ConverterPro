package com.arman.dev.converterpro.core.player

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {

    val state: StateFlow<PlaybackState>

    /** Replaces the queue and starts playing [startIndex]. */
    fun setQueue(tracks: List<PlaybackTrack>, startIndex: Int)

    fun togglePlayPause()

    fun pause()

    fun skipToNext()

    fun skipToPrevious()

    fun seekTo(positionMs: Long)

    fun toggleShuffle()

    fun cycleRepeatMode()

    fun consumeError()

    fun stop()
}
