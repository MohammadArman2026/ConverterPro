package com.arman.dev.converterpro.feature.player.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arman.dev.converterpro.core.common.Utils
import com.arman.dev.converterpro.core.player.AudioPlayer
import com.arman.dev.converterpro.core.player.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = audioPlayer.state
        .map(PlaybackState::toUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
            initialValue = audioPlayer.state.value.toUiState()
        )

    fun onPlayPauseClick() = audioPlayer.togglePlayPause()

    fun onNextClick() = audioPlayer.skipToNext()

    fun onPreviousClick() = audioPlayer.skipToPrevious()

    fun onShuffleClick() = audioPlayer.toggleShuffle()

    fun onRepeatClick() = audioPlayer.cycleRepeatMode()

    fun onErrorShown() = audioPlayer.consumeError()

    /** [progress] is the seek bar fraction, `0f`..`1f`. */
    fun onSeek(progress: Float) {
        val duration = audioPlayer.state.value.durationMs
        if (duration <= 0L) return
        audioPlayer.seekTo((duration * progress.coerceIn(0f, 1f)).toLong())
    }

    /**
     * The app has no playback service, so audio is paused rather than left running unattended once
     * the player screen goes away.
     */
    override fun onCleared() {
        super.onCleared()
        audioPlayer.pause()
    }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

private fun PlaybackState.toUiState(): PlayerUiState {
    val track = currentTrack
    val effectiveDuration = durationMs.coerceAtLeast(0L)

    return PlayerUiState(
        fileName = track?.title.orEmpty(),
        statusLabel = when {
            track == null -> "Nothing playing"
            isBuffering -> "Loading"
            isPlaying -> "Now Playing"
            else -> "Paused"
        },
        subtitle = track?.subtitle.orEmpty(),
        isPlaying = isPlaying,
        isBuffering = isBuffering,
        progress = if (effectiveDuration > 0L) {
            (positionMs.toFloat() / effectiveDuration).coerceIn(0f, 1f)
        } else {
            0f
        },
        positionLabel = Utils.formatDuration(positionMs),
        durationLabel = Utils.formatDuration(effectiveDuration),
        isShuffleEnabled = isShuffleEnabled,
        repeatMode = repeatMode,
        hasTrack = track != null,
        error = error
    )
}
