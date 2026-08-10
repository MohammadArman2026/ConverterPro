package com.arman.dev.converterpro.feature.converter_screen.domain.repository

import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.converter_screen.domain.model.ConversionSettings
import com.arman.dev.converterpro.feature.converter_screen.domain.model.ConversionState

interface ConverterRepository {

    /**
     * Converts [files] with [settings].
     *
     * Reports in-flight progress via [onProgress] as [ConversionState.InProgress]
     * with a percent from `0` to `100`. Terminal outcomes are returned as [Result].
     */
    suspend fun convert(
        files: List<MediaFile>,
        settings: ConversionSettings,
        onProgress: (ConversionState) -> Unit,
    ): Result<Int>
}
