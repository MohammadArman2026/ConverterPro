package com.arman.dev.converterpro.feature.converter_screen.domain.model

/**
 * Fully resolved conversion parameters for one batch.
 *
 * Built by the presentation layer from dropdown selections so the data layer only needs
 * primitive / string values for MediaStore and FFmpeg.
 */
data class ConversionSettings(
    val outputExtension: String,
    val containerFormat: String,
    val mimeType: String?,
    val encoder: String,
    val bitrateBitsPerSecond: Int?,
    val qualityScale: Float?,
    val sampleRateHz: Int?,
    val channelCount: Int?,
)
