package com.arman.dev.converterpro

import android.app.Application
import com.arman.dev.converterpro.feature.files.data.cache.ConvertedFilesCache
import com.arman.dev.converterpro.feature.files.domain.repository.FilesRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ConverterProApplication : Application() {

    @Inject
    lateinit var convertedFilesCache: ConvertedFilesCache

    @Inject
    lateinit var filesRepository: FilesRepository

    override fun onCreate() {
        super.onCreate()
        convertedFilesCache.warmAsync()
        filesRepository.peekCachedFiles()
    }
}
