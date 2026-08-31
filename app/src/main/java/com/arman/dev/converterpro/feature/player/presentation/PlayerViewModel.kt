package com.arman.dev.converterpro.feature.player.presentation

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

    fun onSkipForward() = audioPlayer.skipForward()

    fun onSkipBackward() = audioPlayer.skipBackward()

    fun onShuffleClick() = audioPlayer.toggleShuffle()

    fun onRepeatClick() = audioPlayer.cycleRepeatMode()

    fun onErrorShown() = audioPlayer.consumeError()

    /** [progress] is the seek bar fraction, `0f`..`1f`. */
    fun onSeek(progress: Float) {
        val duration = audioPlayer.state.value.durationMs
        if (duration <= 0L) return
        audioPlayer.seekTo((duration * progress.coerceIn(0f, 1f)).toLong())
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
            isPlaying -> "Playing"
            else -> "Paused"
        },
        subtitle = track?.subtitle.orEmpty(),
        isPlaying = isPlaying,
        currentPosition = positionMs,
        duration = effectiveDuration,
        currentTrack = track,
        bufferedPosition = bufferedPositionMs,
        isLoading = isBuffering,
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
