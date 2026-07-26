package com.arman.dev.converterpro.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.arman.dev.converterpro.core.designsystem.shapes.AppShapes
import com.arman.dev.converterpro.core.designsystem.typography.AppTypography


@Composable
fun ConverterProTheme(
        content: @Composable () -> Unit
) {
    MaterialTheme(
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
