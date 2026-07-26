package com.arman.dev.converterpro.di

import android.content.Context
import com.arman.dev.converterpro.feature.home.data.repository.HomeRepositoryImpl
import com.arman.dev.converterpro.feature.home.domain.repository.HomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeRepositoryModule {

    @Provides
    @Singleton
    fun provideHomeRepository(@ApplicationContext context: Context): HomeRepository = HomeRepositoryImpl(context)
}