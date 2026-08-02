package com.arman.dev.converterpro.di

import android.content.Context
import com.arman.dev.converterpro.feature.files.data.repository.FilesRepositoryImpl
import com.arman.dev.converterpro.feature.files.domain.repository.FilesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FilesRepositoryModule {

    @Provides
    @Singleton
    fun provideFilesRepository(@ApplicationContext context: Context): FilesRepository =
        FilesRepositoryImpl(context)
}
