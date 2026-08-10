package com.arman.dev.converterpro.feature.settings.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import com.arman.dev.converterpro.core.designsystem.color.DropDownBackground
import com.arman.dev.converterpro.core.designsystem.color.DropDownStroke
import com.arman.dev.converterpro.core.designsystem.color.IconStroke
import com.arman.dev.converterpro.feature.home.presentation.components.ReusableText

@Composable
fun SettingItemCard(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, DropDownStroke, shape)
            .clickable(onClick = onClick),
        color = DropDownBackground,
        shape = shape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReusableText(
                text = title,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                ),
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(R.drawable.baseline_chevron_right_24),
                contentDescription = null,
                tint = IconStroke,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
