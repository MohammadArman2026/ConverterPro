package com.arman.dev.converterpro.feature.converter_screen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arman.dev.converterpro.core.model.MediaFile

@Composable
fun FileBox(
    file: List<MediaFile>,
    onFileClick: (mediaInfo: MediaFile) -> Unit
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1F1F1F))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(file) { item ->
                FileCard(
                    onFileClick = {
                        onFileClick(item)
                    },
                    fileName = item.name?:"no name"
                )
            }
        }
    }
}
@Composable
fun FileCard(
    onFileClick: () -> Unit,
    fileName: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .padding(horizontal = 4.dp)
            .drawBehind {
                val strokeWidth = 0.5.dp.toPx()
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            }
            .clickable { onFileClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = fileName,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}