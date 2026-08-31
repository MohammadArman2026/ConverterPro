package com.arman.dev.converterpro.di

import com.arman.dev.converterpro.core.player.AudioPlayer
import com.arman.dev.converterpro.core.player.PlaybackController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the MediaController facade. ExoPlayer itself is created only inside AudioPlayerService.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindAudioPlayer(impl: PlaybackController): AudioPlayer
}
