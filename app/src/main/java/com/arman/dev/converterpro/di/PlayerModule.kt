package com.arman.dev.converterpro.di

import com.arman.dev.converterpro.core.player.AudioPlayer
import com.arman.dev.converterpro.core.player.AudioPlayerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * The player is a singleton so the files screen can load a queue that the player screen then shows.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindAudioPlayer(impl: AudioPlayerImpl): AudioPlayer
}
