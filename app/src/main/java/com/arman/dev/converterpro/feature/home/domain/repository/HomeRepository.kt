package com.arman.dev.converterpro.feature.home.domain.repository

import android.net.Uri
import com.arman.dev.converterpro.core.model.MediaFile

interface HomeRepository {
    suspend fun processUris(newUris: List<Uri>, existingUris: List<Uri>): Result<List<MediaFile>>
}