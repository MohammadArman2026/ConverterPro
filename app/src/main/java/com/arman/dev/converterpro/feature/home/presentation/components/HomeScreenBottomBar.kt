package com.arman.dev.converterpro.feature.home.presentation.components

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.Primary
import com.arman.dev.converterpro.core.designsystem.color.PrimaryPlayerBackground


@Composable
fun HomeBottomBar(
    modifier: Modifier = Modifier,
    onFileClick: () -> Unit,
    onSettingClick: () -> Unit,
    onImportClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomIcon(
            icon = R.drawable.outline_drive_file_move_24,
            onClick = onFileClick
        )

        UploadIcon (
            onClick = onImportClick
        )

        BottomIcon(
            icon = R.drawable.outline_settings_24,
            onClick = onSettingClick
        )
    }
}

@Composable
fun UploadIcon(
    modifier: Modifier = Modifier,
    icon: Int = R.drawable.outline_upload_file_24,
    iconBackground: Color = PrimaryPlayerBackground,
    iconColor: Color = Color.Black,
    iconBoxSize :Dp = 32.dp ,
    iconSize : Dp = 24.dp,
    onClick: () -> Unit
){
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "uploadIconPress",
    )

    Box (
        modifier = modifier
            .size(iconBoxSize)
            .scale(scale)
            .clip(CircleShape)
            .background(iconBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = Color.Black.copy(alpha = 0.25f),
                ),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center
    ){
        Icon(
            painter = painterResource(icon),
            tint = iconColor,
            modifier = Modifier.size(iconSize),
            contentDescription = "icon"
        )
    }
}

@Composable
private fun BottomIcon(
    modifier: Modifier = Modifier,
    icon:Int ,
    size: Dp = 32.dp,
    color: Color = Color.White,
    onClick: () -> Unit
){
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "bottomIconPress",
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = Color.White.copy(alpha = 0.35f),
                ),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = "icon",
            modifier = Modifier.size(24.dp),
            tint = color
        )
    }
}

@Composable
fun HomeScreenBottomBar(
    modifier: Modifier = Modifier,
    onFileClick: () -> Unit,
    onVideoClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                MaterialTheme.shapes.large
            )
            .border(2.dp, Primary, MaterialTheme.shapes.large)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ReusableText(
                text = "Import Audio or Video Files",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                FileSelectorItem(
                    modifier = Modifier.weight(1f),
                    onClick = onFileClick,
                    text = "Files"
                )

                FileSelectorItem(
                    modifier = Modifier.weight(1f),
                    onClick = onVideoClick,
                    text = "Videos",
                    icon = R.drawable.outline_video_library_24
                )
            }
        }
    }
}

@Composable
private fun FileSelectorItem(
    modifier: Modifier = Modifier,
    icon: Int = R.drawable.outline_drive_file_move_24,
    iconBackground: Color = Primary,
    iconColor: Color = Color.Black,
    onClick: () -> Unit,
    text: String
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(iconBackground)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                tint = iconColor,
                contentDescription = "icon"
            )

            ReusableText(
                text = text,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                )
            )
        }
    }
}


@Composable
@Preview(showBackground = true)
private fun Preview() {
    HomeScreenBottomBar(
        modifier = Modifier,
        onFileClick = {},
        onVideoClick = {}
    )
}