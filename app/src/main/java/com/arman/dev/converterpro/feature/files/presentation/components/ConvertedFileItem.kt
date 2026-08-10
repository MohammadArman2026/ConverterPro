package com.arman.dev.converterpro.feature.files.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.ControlSurface
import com.arman.dev.converterpro.core.designsystem.color.DangerRed
import com.arman.dev.converterpro.core.designsystem.color.DropDownBackground
import com.arman.dev.converterpro.core.designsystem.color.DropDownStroke
import com.arman.dev.converterpro.core.designsystem.color.IconStroke
import com.arman.dev.converterpro.core.designsystem.color.PrimaryPlayerBackground
import com.arman.dev.converterpro.feature.files.domain.model.ConvertedFile
import com.arman.dev.converterpro.feature.home.presentation.components.ReusableText

@Composable
fun ConvertedFileItem(
    modifier: Modifier = Modifier,
    file: ConvertedFile,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val nameColor = if (isPlaying) PrimaryPlayerBackground else Color.White
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(DropDownBackground)
            .border(1.dp, DropDownStroke, MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryPlayerBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_audiotrack_24),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = file.name,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = nameColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            ReusableText(
                text = file.listSubtitle,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = IconStroke
                )
            )
        }

        CircularAction(
            icon = R.drawable.outline_play_arrow_24,
            contentDescription = "Play ${file.name}",
            background = PrimaryPlayerBackground,
            iconTint = Color.Black,
            onClick = onPlayClick
        )

        CircularAction(
            icon = R.drawable.outline_share_24,
            contentDescription = "Share ${file.name}",
            background = ControlSurface,
            iconTint = PrimaryPlayerBackground,
            onClick = onShareClick
        )

        CircularAction(
            icon = R.drawable.outline_delete_24,
            contentDescription = "Delete ${file.name}",
            background = ControlSurface,
            iconTint = DangerRed,
            onClick = onDeleteClick
        )
    }
}

@Composable
private fun CircularAction(
    modifier: Modifier = Modifier,
    icon: Int,
    contentDescription: String,
    background: Color,
    iconTint: Color,
    size: Dp = 34.dp,
    iconSize: Dp = 18.dp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "actionPressScale",
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(background)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = Color.White.copy(alpha = 0.35f),
                ),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0E0E)
@Composable
private fun ConvertedFileItemPreview() {
    ConvertedFileItem(
        file = ConvertedFile(
            id = 1L,
            uri = "content://media/external/audio/media/1".toUri(),
            name = "VID20260801174100.aac",
            sizeBytes = 3_355_443L,
            durationMs = 161_000L,
            bitrateKbps = 256,
            channels = 2
        ),
        isPlaying = false,
        onPlayClick = {},
        onShareClick = {},
        onDeleteClick = {}
    )
}
