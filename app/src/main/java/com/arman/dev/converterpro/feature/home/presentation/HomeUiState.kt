package com.arman.dev.converterpro.feature.home.presentation
import com.arman.dev.converterpro.core.model.MediaFile

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String ? = null,
    val mediaList:List<MediaFile> = emptyList()
)
