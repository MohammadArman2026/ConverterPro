package com.arman.dev.converterpro.feature.converter_screen.domain.model

sealed interface ConversionState {
    data object Idle : ConversionState

    /** In-flight conversion with [percent] in `0`..`100`. */
    data class InProgress(
        val percent: Int,
        val message: String,
    ) : ConversionState

    data class Completed(val convertedFileCount: Int) : ConversionState
    data class Failed(val message: String) : ConversionState
}
