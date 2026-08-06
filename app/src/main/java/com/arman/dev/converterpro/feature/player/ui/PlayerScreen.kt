package com.arman.dev.converterpro.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arman.dev.converterpro.core.designsystem.color.AppBackground
import com.arman.dev.converterpro.core.designsystem.color.AppOutline
import com.arman.dev.converterpro.core.designsystem.color.DangerRed
import com.arman.dev.converterpro.core.designsystem.color.Primary
import com.arman.dev.converterpro.core.designsystem.color.TextHint
import com.arman.dev.converterpro.feature.home.ui.components.ReusableText
import com.arman.dev.converterpro.feature.player.ui.components.PlayerArtwork
import com.arman.dev.converterpro.feature.player.ui.components.PlayerControls
import com.arman.dev.converterpro.feature.player.ui.components.PlayerScreenTopBar
import com.arman.dev.converterpro.feature.player.ui.components.PlayerSeekBar
import kotlinx.coroutines.delay

@Composable
fun PlayerScreenRoute(onBackClick: () -> Unit) {
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val uiState by playerViewModel.uiState.collectAsStateWithLifecycle()

    PlayerScreenUi(
        uiState = uiState,
        onBackClick = onBackClick,
        onShuffleClick = playerViewModel::onShuffleClick,
        onPreviousClick = playerViewModel::onPreviousClick,
        onPlayPauseClick = playerViewModel::onPlayPauseClick,
        onNextClick = playerViewModel::onNextClick,
        onRepeatClick = playerViewModel::onRepeatClick,
        onSeek = playerViewModel::onSeek,
        onErrorShown = playerViewModel::onErrorShown
    )
}

@Composable
fun PlayerScreenUi(
    modifier: Modifier = Modifier,
    uiState: PlayerUiState,
    onBackClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onErrorShown: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        PlayerScreenTopBar(
            title = uiState.fileName.ifBlank { "Player" },
            onBackClick = onBackClick
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                PlayerArtwork(isPlaying = uiState.isPlaying)

                Spacer(modifier = Modifier.height(28.dp))

                ReusableText(
                    text = uiState.statusLabel,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        textAlign = TextAlign.Center
                    )
                )

                if (uiState.subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    ReusableText(
                        text = uiState.subtitle,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextHint,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    delay(ERROR_VISIBLE_MS)
                    onErrorShown()
                }

                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = AppOutline,
                    contentColor = DangerRed
                ) {
                    ReusableText(
                        text = error,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = DangerRed
                        )
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            PlayerSeekBar(
                progress = uiState.progress,
                positionLabel = uiState.positionLabel,
                durationLabel = uiState.durationLabel,
                enabled = uiState.hasTrack,
                onSeek = onSeek
            )

            PlayerControls(
                isPlaying = uiState.isPlaying,
                isShuffleEnabled = uiState.isShuffleEnabled,
                repeatMode = uiState.repeatMode,
                enabled = uiState.hasTrack,
                onShuffleClick = onShuffleClick,
                onPreviousClick = onPreviousClick,
                onPlayPauseClick = onPlayPauseClick,
                onNextClick = onNextClick,
                onRepeatClick = onRepeatClick
            )
        }
    }
}

private const val ERROR_VISIBLE_MS = 3_000L

@Preview(showBackground = true)
@Composable
private fun PlayerScreenPreview() {
    PlayerScreenUi(
        uiState = PlayerUiState(
            fileName = "VID20260801174100.aac",
            statusLabel = "Now Playing",
            subtitle = "AAC · 256 kbps · Stereo",
            isPlaying = true,
            progress = 0.42f,
            positionLabel = "1:08",
            durationLabel = "2:41",
            hasTrack = true
        ),
        onBackClick = {},
        onShuffleClick = {},
        onPreviousClick = {},
        onPlayPauseClick = {},
        onNextClick = {},
        onRepeatClick = {},
        onSeek = {},
        onErrorShown = {}
    )
}
