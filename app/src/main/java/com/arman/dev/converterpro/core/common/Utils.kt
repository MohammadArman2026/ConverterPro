package com.arman.dev.converterpro.core.common

import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

object Utils {
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            Locale.getDefault(),
            "%.1f %s",
            bytes / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups.coerceAtMost(units.lastIndex)]
        )
    }

    /**
     * @param padMinutes pads single digit minutes with a leading zero, e.g. `02:41` instead of `2:41`.
     */
    fun formatDuration(durationMs: Long, padMinutes: Boolean = false): String {
        val totalSeconds = (durationMs.coerceAtLeast(0L) / 1000.0).roundToLong()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            padMinutes -> String.format(Locale.US, "%02d:%02d", minutes, seconds)
            else -> String.format(Locale.US, "%d:%02d", minutes, seconds)
        }
    }

    /**
     * MediaStore has no reliable bitrate column below API 31, so it is derived from size over duration.
     */
    fun estimateBitrateKbps(sizeBytes: Long, durationMs: Long?): Int? {
        if (sizeBytes <= 0L || durationMs == null || durationMs <= 0L) return null
        return ((sizeBytes * 8.0) / durationMs).roundToLong().toInt().takeIf { it > 0 }
    }

    fun channelLabel(channels: Int?): String? = when {
        channels == null || channels <= 0 -> null
        channels == 1 -> "Mono"
        channels == 2 -> "Stereo"
        else -> "$channels channels"
    }
}
