package com.arman.dev.converterpro.feature.home.ui
import com.arman.dev.converterpro.core.model.MediaFile

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String ? = null,
    val mediaList:List<MediaFile> = emptyList()
)
