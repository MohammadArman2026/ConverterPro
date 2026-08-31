package com.arman.dev.converterpro.di

import android.content.Context
import com.arman.dev.converterpro.feature.converter_screen.data.repository.ConverterRepositoryImpl
import com.arman.dev.converterpro.feature.converter_screen.domain.repository.ConverterRepository
import com.arman.dev.converterpro.feature.files.data.cache.ConvertedFilesCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConverterRepositoryModule {

    @Provides
    @Singleton
    fun provideConverterRepository(
        @ApplicationContext context: Context,
        convertedFilesCache: ConvertedFilesCache,
    ): ConverterRepository = ConverterRepositoryImpl(context, convertedFilesCache)
}
