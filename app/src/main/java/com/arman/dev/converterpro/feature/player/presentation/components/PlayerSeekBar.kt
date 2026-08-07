package com.arman.dev.converterpro.feature.player.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arman.dev.converterpro.core.designsystem.color.AppOutline
import com.arman.dev.converterpro.core.designsystem.color.Primary
import com.arman.dev.converterpro.core.designsystem.color.TextHint
import com.arman.dev.converterpro.feature.home.presentation.components.ReusableText
import kotlin.math.roundToInt

@Composable
fun PlayerSeekBar(
    modifier: Modifier = Modifier,
    progress: Float,
    positionLabel: String,
    durationLabel: String,
    enabled: Boolean,
    onSeek: (Float) -> Unit
) {
    var trackWidthPx by remember { mutableFloatStateOf(0f) }

    /** Non-null while the user drags, so the thumb follows the finger instead of the player clock. */
    var scrubProgress by remember { mutableStateOf<Float?>(null) }

    val shownProgress = (scrubProgress ?: progress).coerceIn(0f, 1f)
    val density = LocalDensity.current
    val thumbSize = 14.dp
    val thumbSizePx = with(density) { thumbSize.toPx() }

    fun fractionAt(x: Float): Float =
        if (trackWidthPx <= 0f) 0f else (x / trackWidthPx).coerceIn(0f, 1f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .onSizeChanged { trackWidthPx = it.width.toFloat() }
                .pointerInput(enabled, trackWidthPx) {
                    if (!enabled || trackWidthPx <= 0f) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> scrubProgress = fractionAt(offset.x) },
                        onDragEnd = {
                            scrubProgress?.let(onSeek)
                            scrubProgress = null
                        },
                        onDragCancel = { scrubProgress = null }
                    ) { change, _ ->
                        scrubProgress = fractionAt(change.position.x)
                    }
                }
                .pointerInput(enabled, trackWidthPx) {
                    if (!enabled || trackWidthPx <= 0f) return@pointerInput
                    detectTapGestures { offset -> onSeek(fractionAt(offset.x)) }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(AppOutline)
            )

            Box(
                modifier = Modifier
                    .width(with(density) { (shownProgress * trackWidthPx).toDp() })
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Primary)
            )

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (shownProgress * (trackWidthPx - thumbSizePx))
                                .coerceAtLeast(0f)
                                .roundToInt(),
                            y = 0
                        )
                    }
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(Primary)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ReusableText(text = positionLabel, style = TimeLabelStyle)
            ReusableText(text = durationLabel, style = TimeLabelStyle)
        }
    }
}

private val TimeLabelStyle = TextStyle(
    fontSize = 12.sp,
    fontWeight = FontWeight.Normal,
    color = TextHint
)
