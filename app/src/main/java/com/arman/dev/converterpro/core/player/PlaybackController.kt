package com.arman.dev.converterpro.core.player

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Process-scoped [MediaController] connection to [AudioPlayerService].
 *
 * Owns the controller future and player listener. Does **not** create or release ExoPlayer;
 * the service is the only owner of the playback engine.
 */
@Singleton
class PlaybackController @Inject constructor(
    @ApplicationContext context: Context
) : AudioPlayer {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val connectMutex = Mutex()
    private val released = AtomicBoolean(false)

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var playerListener: Player.Listener? = null
    private var positionJob: Job? = null

    override fun setQueue(tracks: List<PlaybackTrack>, startIndex: Int) {
        if (tracks.isEmpty()) {
            stop()
            return
        }
        runOnController { player ->
            val index = startIndex.coerceIn(tracks.indices)
            player.shuffleModeEnabled = false
            player.repeatMode = RepeatMode.OFF.toPlayerRepeatMode()
            player.setMediaItems(tracks.map(PlaybackTrack::toMediaItem), index, 0L)
            player.prepare()
            player.play()
            publishState(player)
        }
    }

    override fun togglePlayPause() {
        runOnController { player ->
            if (player.isPlaying) player.pause() else player.play()
        }
    }

    override fun pause() {
        runOnController(MediaController::pause)
    }

    override fun skipToNext() {
        runOnController { player ->
            if (player.hasNextMediaItem()) player.seekToNextMediaItem()
        }
    }

    override fun skipToPrevious() {
        runOnController(MediaController::seekToPrevious)
    }

    override fun skipForward() {
        runOnController(MediaController::seekForward)
    }

    override fun skipBackward() {
        runOnController(MediaController::seekBack)
    }

    override fun seekTo(positionMs: Long) {
        runOnController { player ->
            val duration = player.duration.takeIf { it > 0L } ?: _state.value.durationMs
            val target = positionMs.coerceIn(0L, if (duration > 0L) duration else positionMs)
            player.seekTo(target)
            _state.update { it.copy(positionMs = target) }
        }
    }

    override fun toggleShuffle() {
        runOnController { player ->
            player.shuffleModeEnabled = !player.shuffleModeEnabled
            publishState(player)
        }
    }

    override fun cycleRepeatMode() {
        runOnController { player ->
            player.repeatMode = player.repeatMode.toRepeatMode().next().toPlayerRepeatMode()
            publishState(player)
        }
    }

    override fun consumeError() {
        _state.update { it.copy(error = null) }
    }

    override fun stop() {
        runOnController { player ->
            player.pause()
            player.stop()
            player.clearMediaItems()
            stopPositionUpdates()
            _state.value = PlaybackState()
        }
    }

    /**
     * Drops the controller and listener. Safe if never connected. Does not stop the service or
     * release ExoPlayer — those stay with [AudioPlayerService].
     */
    fun releaseController() {
        if (!released.compareAndSet(false, true)) return
        stopPositionUpdates()
        detachListener()
        controller = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        scope.coroutineContext[Job]?.cancel()
    }

    private fun runOnController(block: (MediaController) -> Unit) {
        scope.launch {
            runCatching { block(awaitController()) }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isPlaying = false,
                            isBuffering = false,
                            error = error.message ?: "Unable to connect to playback."
                        )
                    }
                }
        }
    }

    private suspend fun awaitController(): MediaController = connectMutex.withLock {
        if (released.get()) error("Playback controller was released.")
        controller?.takeIf { it.isConnected }?.let { return it }

        detachListener()
        stopPositionUpdates()
        controllerFuture?.let { MediaController.releaseFuture(it) }

        val token = SessionToken(
            appContext,
            ComponentName(appContext, AudioPlayerService::class.java)
        )
        val future = MediaController.Builder(appContext, token).buildAsync()
        controllerFuture = future
        val connected = future.awaitController()
        controller = connected
        attachListener(connected)
        startPositionUpdates(connected)
        publishState(connected)
        connected
    }

    private fun attachListener(player: MediaController) {
        val listener = object : Player.Listener {
            override fun onEvents(p: Player, events: Player.Events) {
                publishState(p)
                if (p.isPlaying) startPositionUpdates(player) else if (!p.isLoading) {
                    // Keep a slow tick while paused so a reconnect still shows a stable clock.
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isBuffering = false,
                        error = describePlaybackError(error)
                    )
                }
            }
        }
        playerListener = listener
        player.addListener(listener)
    }

    private fun detachListener() {
        val player = controller
        val listener = playerListener
        if (player != null && listener != null) {
            player.removeListener(listener)
        }
        playerListener = null
    }

    private fun startPositionUpdates(player: MediaController) {
        if (positionJob?.isActive == true) return
        positionJob = scope.launch {
            while (isActive && !released.get() && player.isConnected) {
                publishPosition(player)
                delay(if (player.isPlaying) POSITION_PLAYING_MS else POSITION_IDLE_MS)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    private fun publishPosition(player: MediaController) {
        if (!player.isConnected) return
        val mediaDuration = player.duration.takeIf { it > 0L }
        _state.update { current ->
            current.copy(
                positionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = mediaDuration ?: current.durationMs,
                bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L)
            )
        }
    }

    private fun publishState(player: Player) {
        val queue = player.queueTracks()
        val index = player.currentMediaItemIndex.takeIf { queue.isNotEmpty() && it in queue.indices }
            ?: -1
        val duration = player.duration.takeIf { it > 0L }
            ?: queue.getOrNull(index)?.durationMs
            ?: 0L
        _state.update { current ->
            current.copy(
                queue = queue,
                currentIndex = index,
                isPlaying = player.isPlaying,
                isBuffering = player.isLoading && !player.isPlaying,
                positionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = duration,
                bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L),
                isShuffleEnabled = player.shuffleModeEnabled,
                repeatMode = player.repeatMode.toRepeatMode(),
                error = current.error
            )
        }
    }

    private suspend fun ListenableFuture<MediaController>.awaitController(): MediaController =
        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                val executor = ContextCompat.getMainExecutor(appContext)
                addListener(
                    {
                        try {
                            continuation.resume(get())
                        } catch (error: ExecutionException) {
                            continuation.resumeWithException(error.cause ?: error)
                        } catch (error: Exception) {
                            continuation.resumeWithException(error)
                        }
                    },
                    executor
                )
                continuation.invokeOnCancellation {
                    MediaController.releaseFuture(this@awaitController)
                }
            }
        }

    private companion object {
        const val POSITION_PLAYING_MS = 400L
        const val POSITION_IDLE_MS = 1_000L
    }
}

internal fun describePlaybackError(error: PlaybackException): String {
    val reason = when (error.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
        PlaybackException.ERROR_CODE_IO_NO_PERMISSION,
        PlaybackException.ERROR_CODE_IO_UNSPECIFIED ->
            "the file could not be read"
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
        PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED,
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
        PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED ->
            "the file is incomplete or malformed"
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
        PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
        PlaybackException.ERROR_CODE_DECODING_FAILED,
        PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
        PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES ->
            "this device has no decoder for that format"
        PlaybackException.ERROR_CODE_TIMEOUT ->
            "playback timed out"
        else -> "the file could not be played"
    }
    return "Unable to play: $reason."
}
