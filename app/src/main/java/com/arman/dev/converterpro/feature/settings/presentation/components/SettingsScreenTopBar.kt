package com.arman.dev.converterpro.feature.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.TopBarBackground
import com.arman.dev.converterpro.feature.home.presentation.components.ReusableIcon
import com.arman.dev.converterpro.feature.home.presentation.components.ReusableText

@Composable
fun SettingsScreenTopBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TopBarBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReusableIcon(
            icon = R.drawable.outline_chevron_left_24,
            onClick = onBackClick,
        )
        Spacer(modifier = Modifier.width(12.dp))
        ReusableText(
            text = "Settings",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            ),
        )
    }
}
