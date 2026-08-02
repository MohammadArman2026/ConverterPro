package com.arman.dev.converterpro.core.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaFile(
    val uri: Uri,
    val name: String?,
    val mimeType: String?,
    val size: Long,
    val durationMs: Long?,
    val bitrate: Int?,
    val codec: String?,
    val channels: Int?,
    val sampleRate: Int?
) : Parcelable
