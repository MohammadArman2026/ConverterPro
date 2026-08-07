package com.arman.dev.converterpro.feature.converter_screen.domain.repository

import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.converter_screen.domain.model.ConversionSettings
import com.arman.dev.converterpro.feature.converter_screen.domain.model.ConversionState

interface ConverterRepository {

    /**
     * Converts [files] with [settings].
     *
     * Reports in-flight stages via [onProgress] (`PreparingSpace`, `NamingFile`, `Converting`).
     * Terminal outcomes are returned as [Result]: success carries the converted file count.
     */
    suspend fun convert(
        files: List<MediaFile>,
        settings: ConversionSettings,
        onProgress: (ConversionState) -> Unit,
    ): Result<Int>
}
