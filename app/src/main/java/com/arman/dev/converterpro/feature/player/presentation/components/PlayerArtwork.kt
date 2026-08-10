package com.arman.dev.converterpro.feature.player.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.PrimaryPlayerBackground

@Composable
fun PlayerArtwork(
    modifier: Modifier = Modifier,
    isPlaying: Boolean
) {
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.55f,
        animationSpec = tween(durationMillis = GLOW_FADE_MS),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Soft outer bloom
        Box(
            modifier = Modifier
                .size(300.dp)
                .alpha(glowAlpha)
                .background(
                    Brush.radialGradient(
                        0.22f to PrimaryPlayerBackground.copy(alpha = 0.18f),
                        0.40f to PrimaryPlayerBackground.copy(alpha = 0.12f),
                        0.62f to PrimaryPlayerBackground.copy(alpha = 0.05f),
                        1f to Color.Transparent
                    )
                )
        )

        // Mid glow ring around the disc
        Box(
            modifier = Modifier
                .size(240.dp)
                .alpha(glowAlpha)
                .background(
                    Brush.radialGradient(
                        0.35f to PrimaryPlayerBackground.copy(alpha = 0.55f),
                        0.52f to PrimaryPlayerBackground.copy(alpha = 0.28f),
                        0.72f to PrimaryPlayerBackground.copy(alpha = 0.10f),
                        1f to Color.Transparent
                    )
                )
        )

        // Tight bright halo hugging the circle edge
        Box(
            modifier = Modifier
                .size(200.dp)
                .alpha(glowAlpha)
                .background(
                    Brush.radialGradient(
                        0.48f to PrimaryPlayerBackground.copy(alpha = 0.85f),
                        0.62f to PrimaryPlayerBackground.copy(alpha = 0.45f),
                        0.78f to PrimaryPlayerBackground.copy(alpha = 0.16f),
                        1f to Color.Transparent
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(168.dp)
                .clip(CircleShape)
                .background(PrimaryPlayerBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_music_note_24),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

private const val GLOW_FADE_MS = 400
