package com.arman.dev.converterpro.feature.files.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arman.dev.converterpro.R
import com.arman.dev.converterpro.core.designsystem.color.AppBackground
import com.arman.dev.converterpro.core.designsystem.color.AppOutline
import com.arman.dev.converterpro.core.designsystem.color.IconContainer
import com.arman.dev.converterpro.core.designsystem.color.Primary
import com.arman.dev.converterpro.core.designsystem.color.TextHint
import com.arman.dev.converterpro.feature.files.domain.model.ConvertedFile
import com.arman.dev.converterpro.feature.files.ui.components.ConvertedFileItem
import com.arman.dev.converterpro.feature.files.ui.components.FilesScreenTopBar
import com.arman.dev.converterpro.feature.home.ui.components.ReusableText
import kotlinx.coroutines.delay

@Composable
fun FilesScreenRoute(
    onOpenPlayer: () -> Unit,
    onBackClick: () -> Unit
) {
    val filesViewModel: FilesViewModel = hiltViewModel()
    val uiState by filesViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val audioPermission = remember { requiredAudioPermission() }
    var isPermissionResolved by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        // Files this app created stay readable either way, so the list loads regardless.
        isPermissionResolved = true
    }

    LaunchedEffect(isPermissionResolved) {
        if (isPermissionResolved) {
            filesViewModel.loadFiles()
        } else {
            permissionLauncher.launch(audioPermission)
        }
    }

    FilesScreenUi(
        uiState = uiState,
        onBackClick = onBackClick,
        onPlayClick = { file ->
            filesViewModel.onPlayClick(file)
            onOpenPlayer()
        },
        onShareClick = { file -> context.shareAudioFile(file) },
        onDeleteClick = filesViewModel::onDeleteClick,
        onMessageShown = filesViewModel::onMessageShown
    )
}

@Composable
fun FilesScreenUi(
    modifier: Modifier = Modifier,
    uiState: FilesUiState,
    onBackClick: () -> Unit,
    onPlayClick: (ConvertedFile) -> Unit,
    onShareClick: (ConvertedFile) -> Unit,
    onDeleteClick: (ConvertedFile) -> Unit,
    onMessageShown: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        FilesScreenTopBar(
            fileCountLabel = uiState.fileCountLabel,
            onBackClick = onBackClick
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                uiState.isLoading -> LoadingState()

                uiState.files.isEmpty() && uiState.error != null -> EmptyState(
                    text = uiState.error
                )

                uiState.isEmpty -> EmptyState(
                    text = "No converted files yet.\nConverted audio shows up here."
                )

                else -> FileList(
                    uiState = uiState,
                    onPlayClick = onPlayClick,
                    onShareClick = onShareClick,
                    onDeleteClick = onDeleteClick
                )
            }

            InlineSnackbar(
                message = uiState.message ?: uiState.error.takeIf { uiState.files.isNotEmpty() },
                onDismiss = onMessageShown,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun FileList(
    modifier: Modifier = Modifier,
    uiState: FilesUiState,
    onPlayClick: (ConvertedFile) -> Unit,
    onShareClick: (ConvertedFile) -> Unit,
    onDeleteClick: (ConvertedFile) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = uiState.files,
            key = ConvertedFile::id,
            contentType = { FILE_CONTENT_TYPE }
        ) { file ->
            ConvertedFileItem(
                modifier = Modifier.animateItem(),
                file = file,
                isPlaying = uiState.playingFileId == file.id,
                onPlayClick = { onPlayClick(file) },
                onShareClick = { onShareClick(file) },
                onDeleteClick = { onDeleteClick(file) }
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    text: String
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(IconContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_audiotrack_24),
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(28.dp)
            )
        }

        ReusableText(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = TextHint,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}

@Composable
private fun InlineSnackbar(
    modifier: Modifier = Modifier,
    message: String?,
    onDismiss: () -> Unit
) {
    if (message == null) return

    LaunchedEffect(message) {
        delay(SNACKBAR_VISIBLE_MS)
        onDismiss()
    }

    Snackbar(
        modifier = modifier,
        containerColor = AppOutline,
        contentColor = Primary
    ) {
        ReusableText(
            text = message,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
        )
    }
}

private fun requiredAudioPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

private fun Context.shareAudioFile(file: ConvertedFile) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "audio/*"
        putExtra(Intent.EXTRA_STREAM, file.uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(shareIntent, "Share ${file.name}"))
}

private const val FILE_CONTENT_TYPE = "converted_file"
private const val SNACKBAR_VISIBLE_MS = 2_500L

@Preview(showBackground = true)
@Composable
private fun FilesScreenPreview() {
    FilesScreenUi(
        uiState = FilesUiState(files = emptyList()),
        onBackClick = {},
        onPlayClick = {},
        onShareClick = {},
        onDeleteClick = {},
        onMessageShown = {}
    )
}
