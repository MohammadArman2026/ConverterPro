package com.arman.dev.converterpro.feature.player.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.IconBackground
import com.arman.dev.converterpro.core.designsystem.color.IconStroke
import com.arman.dev.converterpro.core.designsystem.color.PrimaryPlayerBackground
import com.arman.dev.converterpro.core.player.RepeatMode

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    enabled: Boolean,
    onShuffleClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FlatControl(
            icon = R.drawable.outline_shuffle_24,
            contentDescription = "Shuffle",
            tint = if (isShuffleEnabled) PrimaryPlayerBackground else IconStroke,
            onClick = onShuffleClick
        )

        CircleControl(
            icon = R.drawable.outline_skip_previous_24,
            contentDescription = "Previous track",
            enabled = enabled,
            onClick = onPreviousClick
        )

        PlayPauseControl(
            isPlaying = isPlaying,
            enabled = enabled,
            onClick = onPlayPauseClick
        )

        CircleControl(
            icon = R.drawable.outline_skip_next_24,
            contentDescription = "Next track",
            enabled = enabled,
            onClick = onNextClick
        )

        FlatControl(
            icon = when (repeatMode) {
                RepeatMode.ONE -> R.drawable.outline_repeat_one_24
                else -> R.drawable.outline_repeat_24
            },
            contentDescription = "Repeat mode",
            tint = if (repeatMode == RepeatMode.OFF) IconStroke else PrimaryPlayerBackground,
            onClick = onRepeatClick
        )
    }
}

@Composable
private fun PlayPauseControl(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier.size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(
                    Brush.radialGradient(
                        0.34f to PrimaryPlayerBackground.copy(alpha = if (enabled) 0.40f else 0.12f),
                        0.58f to PrimaryPlayerBackground.copy(alpha = if (enabled) 0.14f else 0.04f),
                        1f to Color.Transparent
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (enabled) PrimaryPlayerBackground else IconBackground)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    if (isPlaying) R.drawable.outline_pause_24 else R.drawable.outline_play_arrow_24
                ),
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = if (enabled) Color.Black else IconStroke,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun CircleControl(
    modifier: Modifier = Modifier,
    icon: Int,
    contentDescription: String,
    enabled: Boolean,
    size: Dp = 52.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(IconBackground)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = if (enabled) PrimaryPlayerBackground else IconStroke,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun FlatControl(
    modifier: Modifier = Modifier,
    icon: Int,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}
