package com.arman.dev.converterpro.feature.converter_screen.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.PrimaryPlayerBackground
import com.arman.dev.converterpro.core.designsystem.color.TopBarBackground
import com.arman.dev.converterpro.feature.home.presentation.components.ReusableIcon
import com.arman.dev.converterpro.feature.home.presentation.components.ReusableText

@Composable
private fun ConvertButton(modifier: Modifier = Modifier,
                          onConvertClick: () -> Unit){
    Button(
        modifier = modifier
            .wrapContentSize(),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryPlayerBackground,
            contentColor = Color.Black
        ),

        shape = MaterialTheme.shapes.medium,
        onClick = onConvertClick
    ) {
        ReusableText(
            text = "Convert",
            modifier = Modifier,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        )
    }
}

@Composable
fun ConvertTopBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onConvertClick: () -> Unit
){
    Box (
        modifier = modifier
            .fillMaxWidth()
            .background(TopBarBackground)
    ){
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            ReusableIcon(
                modifier = Modifier,
                onClick = onBackClick,
                icon = R.drawable.outline_chevron_left_24
            )

            ConvertButton(
                onConvertClick = onConvertClick
            )
        }
    }
}