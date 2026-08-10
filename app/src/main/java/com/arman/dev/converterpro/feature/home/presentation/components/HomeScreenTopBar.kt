package com.arman.dev.converterpro.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.IconBackground
import com.arman.dev.converterpro.core.designsystem.color.Primary
import com.arman.dev.converterpro.core.designsystem.color.PrimaryPlayerBackground
import com.arman.dev.converterpro.core.designsystem.color.TopBarBackground


@Composable
fun HomeTopBar(
    modifier: Modifier = Modifier,
    topBarColor : Color = TopBarBackground,
    isNextButtonVisible: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(topBarColor)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp , vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ReusableText(
                text = "Converter Pro",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            )
            if (isNextButtonVisible) {
                NextButton(
                    onNextClick = onClick
                )
            }
        }
    }
}

@Composable
fun ReusableIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    backgroundColor: Color = IconBackground,
    icon: Int = R.drawable.outline_settings_24,
    iconTint: Color = Color.White,
    size: Dp = 32.dp,
    iconSize: Dp = 24.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            tint = iconTint,
            modifier = Modifier.size(iconSize),
            contentDescription = "Icon"
        )
    }
}

@Composable
fun NextButton(
    onNextClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(PrimaryPlayerBackground)
            .clickable {
                onNextClick()
            },
        contentAlignment = Alignment.Center
    ) {
        ReusableText(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
            text = "Next",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        )
    }
}


@Composable
fun ReusableText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle,
) {
    Text(
        text = text,
        style = style,
        modifier = modifier
    )
}



