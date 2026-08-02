package com.arman.dev.converterpro.feature.files.domain.repository

import android.net.Uri
import com.arman.dev.converterpro.feature.files.domain.model.ConvertedFile

interface FilesRepository {

    suspend fun loadConvertedFiles(): Result<List<ConvertedFile>>

    suspend fun deleteConvertedFile(uri: Uri): Result<Unit>
}
