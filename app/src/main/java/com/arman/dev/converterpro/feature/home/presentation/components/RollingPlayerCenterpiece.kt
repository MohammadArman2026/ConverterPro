package com.arman.dev.converterpro.feature.home.presentation.components


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.CrimsonDark
import com.arman.dev.converterpro.core.designsystem.color.CrimsonLight
import com.arman.dev.converterpro.core.designsystem.color.CrimsonPrimary
import com.arman.dev.converterpro.core.designsystem.color.CrimsonSurfaceTint
import com.arman.dev.converterpro.core.designsystem.color.StringBlush
import com.arman.dev.converterpro.core.designsystem.color.StringCoral
import kotlin.math.sin

@Composable
fun RollingPlayerCenterpiece(
    modifier: Modifier = Modifier,
    onUniversalPick: () -> Unit,
    onVideoPick: () -> Unit,
    onAudioPick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "studio_anim")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingMusicRibbonAndNotes(
                modifier = Modifier.fillMaxSize(),
                wavePhase = wavePhase
            )

            RollingVinylDisc(
                rotationAngle = rotationAngle,
                size = 175.dp,
                onClick = onUniversalPick
            )
        }

        ConvertMediaButton(
            onClick = onUniversalPick
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StudioActionCard(
                modifier = Modifier.weight(1f),
                title = "Video to Audio",
                subtitle = "Extract soundtrack",
                iconRes = R.drawable.outline_video_library_24,
                iconTint = CrimsonPrimary,
                onClick = onVideoPick
            )

            StudioActionCard(
                modifier = Modifier.weight(1f),
                title = "Audio to Audio",
                subtitle = "Format & Bitrate",
                iconRes = R.drawable.outline_audiotrack_24,
                iconTint = CrimsonPrimary,
                onClick = onAudioPick
            )
        }
    }
}

@Composable
private fun FloatingMusicRibbonAndNotes(
    modifier: Modifier = Modifier,
    wavePhase: Float
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height * 0.50f

        // 3 flowing melody ribbon lines curving like a musical ribbon staff across the disc
        val ribbonOffsets = listOf(-8f, 0f, 8f)
        ribbonOffsets.forEachIndexed { index, offset ->
            val path = Path()
            val step = 8f
            var x = 0f
            var first = true

            while (x <= width) {
                val progress = x / width
                val envelope = sin(progress * Math.PI).toFloat()
                // flowing S-wave melody ribbon
                val y = centerY + offset +
                        sin(progress * Math.PI * 2.2f + wavePhase).toFloat() * 26f * envelope

                if (first) {
                    path.moveTo(x, y)
                    first = false
                } else {
                    path.lineTo(x, y)
                }
                x += step
            }

            val ribbonColor = when (index) {
                0 -> CrimsonPrimary.copy(alpha = 0.50f)
                1 -> CrimsonLight.copy(alpha = 0.65f)
                else -> StringCoral.copy(alpha = 0.40f)
            }

            drawPath(
                path = path,
                color = ribbonColor,
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Floating Musical Notes
        val noteConfigs = listOf(
            Triple("♪", 0.10f, centerY - 32f),
            Triple("♫", 0.22f, centerY + 24f),
            Triple("♩", 0.74f, centerY - 28f),
            Triple("♬", 0.88f, centerY + 18f),
            Triple("♪", 0.50f, centerY - 52f),
        )

        noteConfigs.forEachIndexed { i, (glyph, xFraction, baseY) ->
            val noteX = width * xFraction
            val noteY = baseY + sin(wavePhase + i * 1.3f) * 8f

            drawText(
                textMeasurer = textMeasurer,
                text = glyph,
                topLeft = Offset(noteX, noteY),
                style = TextStyle(
                    color = CrimsonPrimary.copy(alpha = 0.85f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // sparkle particle dots
        val sparkleOffsets = listOf(
            Offset(width * 0.16f, centerY - 14f + sin(wavePhase) * 6f),
            Offset(width * 0.32f, centerY + 36f + sin(wavePhase + 1.5f) * 7f),
            Offset(width * 0.68f, centerY - 34f + sin(wavePhase + 2.2f) * 6f),
            Offset(width * 0.82f, centerY + 30f + sin(wavePhase + 0.8f) * 5f),
        )

        sparkleOffsets.forEach { offset ->
            drawCircle(
                color = CrimsonLight.copy(alpha = 0.70f),
                radius = 2.5.dp.toPx(),
                center = offset
            )
        }
    }
}

@Composable
private fun RollingVinylDisc(
    rotationAngle: Float,
    size: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(12.dp, CircleShape, spotColor = Color(0x55000000))
            .clip(CircleShape)
            .rotate(rotationAngle)
            .clickable(
                indication = ripple(bounded = true, color = Color.White),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Grooved vinyl record
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val maxRadius = this.size.width / 2

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2C2C2E), Color(0xFF161618), Color(0xFF0D0D0E)),
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius
            )

            // Concentric grooves
            listOf(0.92f, 0.86f, 0.80f, 0.74f, 0.68f, 0.62f, 0.56f).forEach { fraction ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = maxRadius * fraction,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Gloss sheen
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        Color.White.copy(alpha = 0.0f),
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.0f),
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.0f),
                    )
                ),
                radius = maxRadius
            )
        }

        // Center Red Label
        Box(
            modifier = Modifier
                .size(size * 0.38f)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(CrimsonLight, CrimsonDark)))
                .border(1.5.dp, StringBlush, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "STUDIO",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFCCCCCC), CircleShape)
                )
                Text(
                    text = "PRO",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun ConvertMediaButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = CrimsonPrimary.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(CrimsonPrimary, CrimsonLight)))
            .clickable(
                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.3f)),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 28.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_upload_file_24),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tap to Convert Media",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
private fun StudioActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    iconRes: Int,
    iconTint: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(120),
        label = "cardPress"
    )

    Box(
        modifier = modifier
            .scale(animatedScale)
            .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = Color(0x18000000))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF0F0F3), RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = CrimsonPrimary.copy(alpha = 0.15f)),
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CrimsonSurfaceTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF7A7A85)
            )
        }
    }
}