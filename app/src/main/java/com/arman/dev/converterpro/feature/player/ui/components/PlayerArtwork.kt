package com.arman.dev.converterpro.feature.player.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.Primary
import com.arman.dev.converterpro.core.designsystem.color.PlayerArtGradientEnd
import com.arman.dev.converterpro.core.designsystem.color.PlayerArtGradientStart
import com.arman.dev.converterpro.core.designsystem.color.PlayerArtRing

@Composable
fun PlayerArtwork(
    modifier: Modifier = Modifier,
    isPlaying: Boolean
) {
    val pulse = rememberInfiniteTransition(label = "artworkGlow")
    val glowScale by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = GLOW_PULSE_MS),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.45f,
        animationSpec = tween(durationMillis = GLOW_FADE_MS),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(if (isPlaying) glowScale else 1f)
                .alpha(glowAlpha)
                .background(
                    Brush.radialGradient(
                        0.30f to Primary.copy(alpha = 0.32f),
                        0.46f to Primary.copy(alpha = 0.14f),
                        0.62f to Primary.copy(alpha = 0.04f),
                        1f to Color.Transparent
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(255.dp)
                .clip(CircleShape)
                .background(PlayerArtRing)
                .border(1.dp, Primary.copy(alpha = 0.20f), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(205.dp)
                .clip(CircleShape)
                .border(1.dp, Primary.copy(alpha = 0.12f), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(PlayerArtGradientStart, PlayerArtGradientEnd)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_music_note_24),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(74.dp)
            )
        }
    }
}

private const val GLOW_PULSE_MS = 1_600
private const val GLOW_FADE_MS = 400
