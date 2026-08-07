package com.arman.dev.converterpro.feature.converter_screen.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.arman.dev.converterpro.core.designsystem.color.Primary
import com.arman.dev.converterpro.feature.home.presentation.components.ReusableText

@Composable
fun ConverterScreenTopBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onConvertClick:()-> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ){
                ReusableText(
                    text = "Back",
                    modifier = Modifier.clickable{
                        onBackClick()
                    },
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Primary
                    )
                )

                ReusableText(
                    text = "Format",
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                )
            }
            ConvertButton(
                onConvertClick = onConvertClick
            )
        }
    }
}

@Composable
private fun ConvertButton(modifier: Modifier = Modifier,
                          onConvertClick: () -> Unit){
    Button(
        modifier = modifier
            .wrapContentSize(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary
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