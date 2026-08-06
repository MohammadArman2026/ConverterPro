package com.arman.dev.converterpro.feature.files.domain.repository

import android.net.Uri
import com.arman.dev.converterpro.feature.files.domain.model.ConvertedFile
import kotlinx.coroutines.flow.Flow

interface FilesRepository {

    /**
     * Emits the converted files at least once, then again as richer details arrive.
     *
     * Reading bitrate and channel counts means opening every file, which is far slower than the
     * MediaStore query itself. Emitting in stages lets the list render immediately instead of
     * waiting for the slowest file.
     */
    fun convertedFiles(): Flow<Result<List<ConvertedFile>>>

    suspend fun deleteConvertedFile(uri: Uri): Result<Unit>
}
