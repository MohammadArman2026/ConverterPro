package com.arman.dev.converterpro.feature.files.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.AppSurface
import com.arman.dev.converterpro.core.designsystem.color.ControlSurface
import com.arman.dev.converterpro.core.designsystem.color.Primary
import com.arman.dev.converterpro.core.designsystem.color.TextHint
import com.arman.dev.converterpro.core.designsystem.color.TopBarBackground
import com.arman.dev.converterpro.feature.home.presentation.components.ReusableIcon
import com.arman.dev.converterpro.feature.home.presentation.components.ReusableText

@Composable
fun FilesScreenTopBar(
    modifier: Modifier = Modifier,
    fileCountLabel: String?,
    onBackClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(TopBarBackground)
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){

            Spacer(Modifier.width(16.dp))
            ReusableIcon(
                modifier = Modifier,
                onClick = onBackClick,
                icon = R.drawable.outline_chevron_left_24
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReusableText(
                    text = "Files",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                    )
                )

                fileCountLabel?.let { it->
                    ReusableText(
                        text = it,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}
