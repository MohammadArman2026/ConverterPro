package com.arman.dev.converterpro.di

import android.content.Context
import com.arman.dev.converterpro.feature.files.data.cache.ConvertedFilesCache
import com.arman.dev.converterpro.feature.files.data.repository.FilesRepositoryImpl
import com.arman.dev.converterpro.feature.files.domain.repository.FilesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

private const val CONVERTED_FILES_CACHE_NAME = "converted_files_cache.json"

@Module
@InstallIn(SingletonComponent::class)
object FilesRepositoryModule {

    @Provides
    @Singleton
    fun provideConvertedFilesCache(@ApplicationContext context: Context): ConvertedFilesCache =
        ConvertedFilesCache(File(context.filesDir, CONVERTED_FILES_CACHE_NAME))

    @Provides
    @Singleton
    fun provideFilesRepository(
        @ApplicationContext context: Context,
        cache: ConvertedFilesCache
    ): FilesRepository = FilesRepositoryImpl(context, cache)
}
