package com.arman.dev.converterpro.core.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.arman.dev.converterpro.MainActivity

object DeepLinks {
    const val PLAYER_SCHEME = "converterpro"
    const val PLAYER_HOST = "player"
    const val PLAYER_URI = "$PLAYER_SCHEME://$PLAYER_HOST"

    fun playerSessionActivityIntent(context: Context): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse(PLAYER_URI)).apply {
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NEW_TASK
        }

    fun matchesPlayerUri(uri: String?): Boolean =
        uri == PLAYER_URI || uri?.startsWith("$PLAYER_URI?") == true

    fun isPlayerIntent(intent: Intent?): Boolean =
        matchesPlayerUri(intent?.data?.toString())

    fun startDestinationForUri(uri: String?): String =
        if (matchesPlayerUri(uri)) Routes.PLAYER else Routes.HOME
}
