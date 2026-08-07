package com.arman.dev.converterpro.feature.converter_screen.domain.model

sealed interface ConversionState {
    data object Idle : ConversionState
    data object PreparingSpace : ConversionState
    data object NamingFile : ConversionState
    data object Converting : ConversionState
    data class Completed(val convertedFileCount: Int) : ConversionState
    data class Failed(val message: String) : ConversionState
}
