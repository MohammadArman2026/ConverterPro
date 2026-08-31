package com.arman.dev.converterpro.feature.files.domain.repository

import android.net.Uri
import com.arman.dev.converterpro.feature.files.domain.model.ConvertedFile
import kotlinx.coroutines.flow.Flow

interface FilesRepository {

    /**
     * Last known list from disk or memory, without touching MediaStore.
     *
     * Used to paint Files immediately after process death or a later visit.
     */
    fun peekCachedFiles(): List<ConvertedFile>

    /**
     * Emits the cached list first when one exists, then the MediaStore list, then again as
     * richer details arrive for files that were not already cached.
     *
     * Reading bitrate and channel counts means opening every file, which is far slower than the
     * MediaStore query itself. Emitting in stages lets the list render immediately instead of
     * waiting for the slowest file.
     */
    fun convertedFiles(): Flow<Result<List<ConvertedFile>>>

    suspend fun deleteConvertedFile(uri: Uri): Result<Unit>
}
