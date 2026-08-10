package com.arman.dev.converterpro.feature.player.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.TopBarBackground
import com.arman.dev.converterpro.feature.home.presentation.components.ReusableIcon

@Composable
fun PlayerScreenTopBar(
    modifier: Modifier = Modifier,
    title: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(TopBarBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReusableIcon(
                icon = R.drawable.outline_chevron_left_24,
                modifier = Modifier,
                size = 40.dp,
                iconSize = 24.dp,
                onClick = onBackClick
            )

            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Balances the back button so the title stays optically centered.
            Box(modifier = Modifier.size(40.dp))
        }
    }
}
