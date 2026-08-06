package com.arman.dev.converterpro.core.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Playback is backed by the platform [MediaPlayer] so the app needs no extra media dependency.
 * All calls run on the main thread, which keeps [MediaPlayer]'s state machine single threaded.
 */
@Singleton
class AudioPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioPlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    /** Queue indices in the order they are traversed; reshuffled whenever shuffle is toggled. */
    private var playbackOrder: List<Int> = emptyList()

    override fun setQueue(tracks: List<PlaybackTrack>, startIndex: Int) {
        if (tracks.isEmpty()) {
            stop()
            return
        }

        val index = startIndex.coerceIn(tracks.indices)
        _state.update { it.copy(queue = tracks, error = null) }
        rebuildPlaybackOrder(trackCount = tracks.size, firstIndex = index)
        playTrackAt(index)
    }

    override fun togglePlayPause() {
        val player = mediaPlayer
        if (player == null) {
            val index = _state.value.currentIndex.takeIf { it >= 0 } ?: return
            playTrackAt(index)
            return
        }

        runCatching {
            if (player.isPlaying) {
                player.pause()
                stopProgressUpdates()
                _state.update { it.copy(isPlaying = false) }
            } else {
                player.start()
                _state.update { it.copy(isPlaying = true) }
                startProgressUpdates()
            }
        }
    }

    override fun pause() {
        val player = mediaPlayer ?: return
        runCatching {
            if (player.isPlaying) {
                player.pause()
                stopProgressUpdates()
                _state.update { it.copy(isPlaying = false) }
            }
        }
    }

    override fun skipToNext() {
        resolveAdjacentIndex(forward = true, allowWrap = true)?.let(::playTrackAt)
    }

    override fun skipToPrevious() {
        val hasPlayedEnough = _state.value.positionMs > RESTART_THRESHOLD_MS
        if (hasPlayedEnough) {
            seekTo(0L)
            return
        }
        resolveAdjacentIndex(forward = false, allowWrap = true)?.let(::playTrackAt)
    }

    override fun seekTo(positionMs: Long) {
        val duration = _state.value.durationMs
        val target = positionMs.coerceIn(0L, if (duration > 0L) duration else positionMs)

        _state.update { it.copy(positionMs = target) }
        mediaPlayer?.let { player -> runCatching { player.seekTo(target.toInt()) } }
    }

    override fun toggleShuffle() {
        val enabled = !_state.value.isShuffleEnabled
        _state.update { it.copy(isShuffleEnabled = enabled) }
        rebuildPlaybackOrder(
            trackCount = _state.value.queue.size,
            firstIndex = _state.value.currentIndex
        )
    }

    override fun cycleRepeatMode() {
        _state.update { it.copy(repeatMode = it.repeatMode.next()) }
    }

    override fun consumeError() {
        if (_state.value.error != null) {
            _state.update { it.copy(error = null) }
        }
    }

    override fun stop() {
        releasePlayer()
        _state.update {
            it.copy(
                currentIndex = -1,
                isPlaying = false,
                isBuffering = false,
                positionMs = 0L,
                durationMs = 0L
            )
        }
    }

    private fun playTrackAt(index: Int) {
        val track = _state.value.queue.getOrNull(index) ?: return
        releasePlayer()

        _state.update {
            it.copy(
                currentIndex = index,
                isPlaying = false,
                isBuffering = true,
                positionMs = 0L,
                durationMs = track.durationMs,
                error = null
            )
        }

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setOnPreparedListener { player ->
                _state.update {
                    it.copy(
                        isBuffering = false,
                        isPlaying = true,
                        durationMs = player.duration.toLong().coerceAtLeast(track.durationMs)
                    )
                }
                player.start()
                startProgressUpdates()
            }
            setOnCompletionListener { onTrackCompleted() }
            setOnErrorListener { _, _, extra ->
                failCurrentTrack(track.title, describeError(extra))
                true
            }
        }

        runCatching {
            mediaPlayer?.setDataSource(context, track.uri)
            mediaPlayer?.prepareAsync()
        }.onFailure { failCurrentTrack(track.title, "the file could not be opened") }
    }

    /**
     * Turns [MediaPlayer]'s error code into something a user can act on.
     *
     * The distinction matters because the converter can produce formats the platform cannot decode
     * at all, such as AC3, MP2 and WavPack. Reporting those as a missing decoder rather than a
     * generic failure avoids sending anyone hunting for a broken file.
     */
    private fun describeError(extra: Int): String = when (extra) {
        MediaPlayer.MEDIA_ERROR_UNSUPPORTED ->
            "this device has no decoder for that format"
        MediaPlayer.MEDIA_ERROR_MALFORMED ->
            "the file is incomplete or malformed"
        MediaPlayer.MEDIA_ERROR_IO ->
            "the file could not be read"
        MediaPlayer.MEDIA_ERROR_TIMED_OUT ->
            "playback timed out"
        else -> "the file could not be played"
    }

    private fun onTrackCompleted() {
        stopProgressUpdates()

        if (_state.value.repeatMode == RepeatMode.ONE) {
            playTrackAt(_state.value.currentIndex)
            return
        }

        val nextIndex = resolveAdjacentIndex(
            forward = true,
            allowWrap = _state.value.repeatMode == RepeatMode.ALL
        )

        if (nextIndex == null) {
            _state.update { it.copy(isPlaying = false, positionMs = it.durationMs) }
        } else {
            playTrackAt(nextIndex)
        }
    }

    private fun failCurrentTrack(title: String, reason: String) {
        releasePlayer()
        _state.update {
            it.copy(
                isPlaying = false,
                isBuffering = false,
                error = "Unable to play $title: $reason."
            )
        }
    }

    private fun resolveAdjacentIndex(forward: Boolean, allowWrap: Boolean): Int? {
        val order = playbackOrder
        if (order.isEmpty()) return null

        val position = order.indexOf(_state.value.currentIndex)
        if (position == -1) return order.firstOrNull()

        val target = if (forward) position + 1 else position - 1
        return when {
            target in order.indices -> order[target]
            !allowWrap -> null
            forward -> order.first()
            else -> order.last()
        }
    }

    private fun rebuildPlaybackOrder(trackCount: Int, firstIndex: Int) {
        if (trackCount <= 0) {
            playbackOrder = emptyList()
            return
        }

        playbackOrder = when {
            !_state.value.isShuffleEnabled -> List(trackCount) { it }
            firstIndex !in 0 until trackCount -> (0 until trackCount).shuffled()
            else -> listOf(firstIndex) + ((0 until trackCount) - firstIndex).shuffled()
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = scope.launch {
            while (isActive) {
                val position = mediaPlayer?.let { player ->
                    runCatching { player.currentPosition.toLong() }.getOrNull()
                }
                if (position != null) {
                    _state.update { it.copy(positionMs = position) }
                }
                delay(PROGRESS_INTERVAL_MS)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun releasePlayer() {
        stopProgressUpdates()
        mediaPlayer?.let { player ->
            runCatching {
                player.setOnPreparedListener(null)
                player.setOnCompletionListener(null)
                player.setOnErrorListener(null)
                player.reset()
                player.release()
            }
        }
        mediaPlayer = null
    }

    private companion object {
        const val PROGRESS_INTERVAL_MS = 400L
        const val RESTART_THRESHOLD_MS = 3_000L
    }
}
