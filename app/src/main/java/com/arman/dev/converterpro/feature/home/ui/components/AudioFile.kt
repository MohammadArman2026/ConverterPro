package com.arman.dev.converterpro.feature.home.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.IconBackground
import com.arman.dev.converterpro.core.model.MediaFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioFile(
    modifier: Modifier,
    mediaFile: MediaFile,
    onRemoveClick: (Uri) -> Unit,
) {
    var isExpanded by remember(mediaFile.uri) { mutableStateOf(false) }
    var anchorWidth by remember { mutableIntStateOf(0) }
    val removeCallback by rememberUpdatedState(onRemoveClick)

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded }
    ) {

        Row(
            modifier = modifier
                .menuAnchor()
                .fillMaxWidth()
                .height(70.dp)
                .onGloballyPositioned {
                    anchorWidth = it.size.width
                }
                .clip(MaterialTheme.shapes.medium)
                .background(Color(0xFF1B1B1B))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { isExpanded = true }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 8.dp,
                            bottomStart = 8.dp,
                            topEnd = 16.dp,
                            bottomEnd = 8.dp
                        )
                    )
                    .background(IconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        R.drawable.baseline_music_note_24
                    ),
                    modifier = Modifier.size(32.dp),
                    contentDescription = "music_logo",
                    tint = Color.Black
                )
            }

            mediaFile.name?.let {
                Text(
                    text = it,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 2.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(IconBackground)
                    .clickable {
                        removeCallback(mediaFile.uri)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        R.drawable.outline_remove_24
                    ),
                    tint = Color.Black,
                    contentDescription = "remove"
                )
            }
        }

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            shape = MaterialTheme.shapes.medium,
            containerColor = Color(0XFF1F1F1F),
            modifier = Modifier
                .width(with(LocalDensity.current) { anchorWidth.toDp() }),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(mediaFile.name ?: "no name", color = Color.White)
                Text("Size: ${mediaFile.size}", color = Color.White)
                Text("Sample rate: ${mediaFile.sampleRate ?: 0}", color = Color.White)
                Text("Channels: ${mediaFile.channels ?: 0}", color = Color.White)
                Text("Codec: ${mediaFile.codec ?: 0}", color = Color.White)
                Text("Duration: ${mediaFile.durationMs ?: 0} ms", color = Color.White)
            }
        }
    }
}