package com.arman.dev.converterpro.feature.home.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arman.dev.converterpro.R
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
    iconSize : Dp = 28.dp,
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
            modifier = Modifier.size(28.dp),
            tint = color
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {

}