package com.arman.dev.converterpro.feature.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.arman.dev.converterpro.core.designsystem.color.AppBackground
import com.arman.dev.converterpro.core.designsystem.color.AppOutline
import com.arman.dev.converterpro.core.designsystem.color.AppSurface
import com.arman.dev.converterpro.core.designsystem.color.IconBackground
import com.arman.dev.converterpro.feature.home.domain.model.SettingOption

@Composable
fun HomeScreenTopBar(
    modifier: Modifier = Modifier,
    onSettingClick: (SettingOption) -> Unit,
    onNextClick: () -> Unit,
    isNextButtonVisible: Boolean
) {
    var isMenuExpanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReusableText(
                    text = "Converter Pro",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                    )
                )

                ReusableText(
                    text = "Import media",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IconBackground,
                    )
                )
            }
            if (isNextButtonVisible) {
                NextButton(onNextClick = onNextClick)
            } else {
                Box{
                    ReusableIcon(onClick = {
                        isMenuExpanded = !isMenuExpanded
                    })
                    DropdownMenu(
                        modifier = Modifier,
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                        containerColor = AppOutline,
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp , IconBackground),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        SettingOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.title,
                                        color = IconBackground,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    onSettingClick(option)
                                },
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                            )
                        }
                    }
                }
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
    iconTint: Color = AppBackground,
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
            .background(IconBackground)
            .clickable {
                onNextClick()
            },
        contentAlignment = Alignment.Center
    ) {
        ReusableText(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 10.dp),
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



