package com.arman.dev.converterpro.feature.files.ui

import com.arman.dev.converterpro.feature.files.domain.model.ConvertedFile

data class FilesUiState(
    val isLoading: Boolean = false,
    val files: List<ConvertedFile> = emptyList(),
    val playingFileId: Long? = null,
    val error: String? = null,
    val message: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && error == null && files.isEmpty()

    val fileCountLabel: String?
        get() = when {
            files.isEmpty() -> null
            files.size == 1 -> "1 file"
            else -> "${files.size} files"
        }
}
