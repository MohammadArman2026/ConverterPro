package com.arman.dev.converterpro.core.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.arman.dev.converterpro.MainActivity
import com.arman.dev.converterpro.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Owns the single [ExoPlayer] and [MediaSession] for the process.
 *
 * UI and ViewModels never hold these objects; they talk to this service through [MediaController]
 * via [PlaybackController].
 */
@AndroidEntryPoint
class AudioPlayerService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var playerReleased = false

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setSeekBackIncrementMs(SEEK_INCREMENT_MS)
            .setSeekForwardIncrementMs(SEEK_INCREMENT_MS)
            .build()
            .apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
            }

        val sessionActivity = PendingIntent.getActivity(
            this,
            SESSION_ACTIVITY_REQUEST,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivity)
            .setExtras(Bundle.EMPTY)
            .build()

        val notifications = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .setChannelName(R.string.playback_notification_channel)
            .build()
        notifications.setSmallIcon(R.drawable.outline_audiotrack_24)
        setMediaNotificationProvider(notifications)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        releasePlayback()
        super.onDestroy()
    }

    /**
     * Safe to call more than once. Releases ExoPlayer then MediaSession, never the reverse,
     * and never from a UI component.
     */
    private fun releasePlayback() {
        val session = mediaSession ?: return
        mediaSession = null
        if (!playerReleased) {
            playerReleased = true
            val player = session.player
            player.stop()
            player.clearMediaItems()
            player.release()
        }
        session.release()
    }

    private companion object {
        const val SEEK_INCREMENT_MS = 10_000L
        const val SESSION_ACTIVITY_REQUEST = 1001
        const val NOTIFICATION_CHANNEL_ID = "converterpro_playback"
    }
}
