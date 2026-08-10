package com.arman.dev.converterpro

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.arman.dev.converterpro.core.designsystem.color.PrimaryBackground
import com.arman.dev.converterpro.core.designsystem.theme.ConverterProTheme
import com.arman.dev.converterpro.core.ffmpeg.FfmpegNative
import com.arman.dev.converterpro.core.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logFfmpegCapabilities()
        enableEdgeToEdge()
        setContent {
            ConverterProTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PrimaryBackground)
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    private fun logFfmpegCapabilities() {
        val capabilities = FfmpegNative.capabilities()

        logComponents("Encoders", capabilities.encoders)
        logComponents("Decoders", capabilities.decoders)
        logComponents("Muxers", capabilities.muxers)
        logComponents("Demuxers", capabilities.demuxers)
    }

    private fun logComponents(type: String, components: List<String>) {
        Log.i(TAG, "$type (${components.size})")

        var batch = StringBuilder()
        var batchIndex = 1
        components.forEach { component ->
            val separator = if (batch.isEmpty()) "" else ", "

            if (batch.length + separator.length + component.length > MAX_LOG_MESSAGE_LENGTH) {
                Log.i(TAG, "$type [$batchIndex]: $batch")
                batch = StringBuilder(component)
                batchIndex++
            } else {
                batch.append(separator).append(component)
            }
        }

        if (batch.isNotEmpty()) {
            Log.i(TAG, "$type [$batchIndex]: $batch")
        }
    }

    private companion object {
        const val TAG = "FFmpeg"
        const val MAX_LOG_MESSAGE_LENGTH = 3_000
    }
}
