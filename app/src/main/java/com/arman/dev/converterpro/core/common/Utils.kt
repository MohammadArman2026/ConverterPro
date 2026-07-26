package com.arman.dev.converterpro.core.common

import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

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
}
