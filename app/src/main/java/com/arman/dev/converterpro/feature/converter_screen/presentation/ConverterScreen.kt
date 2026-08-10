package com.arman.dev.converterpro.feature.converter_screen.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arman.dev.converterpro.core.designsystem.color.AppSurface
import com.arman.dev.converterpro.core.designsystem.color.DropDownStroke
import com.arman.dev.converterpro.core.designsystem.color.PrimaryBackground
import com.arman.dev.converterpro.core.designsystem.color.PrimaryPlayerBackground
import com.arman.dev.converterpro.core.designsystem.color.TopBarBackground
import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.converter_screen.domain.model.BitRate
import com.arman.dev.converterpro.feature.converter_screen.domain.model.BitrateValue
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Channel
import com.arman.dev.converterpro.feature.converter_screen.domain.model.ConversionState
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Encoder
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Extension
import com.arman.dev.converterpro.feature.converter_screen.domain.model.SampleRate
import com.arman.dev.converterpro.feature.converter_screen.presentation.components.ConvertTopBar
import com.arman.dev.converterpro.feature.converter_screen.presentation.components.CustomDropDown
import com.arman.dev.converterpro.feature.converter_screen.presentation.components.FileBox
import com.arman.dev.converterpro.feature.converter_screen.presentation.components.RenameFileDialog
import com.arman.dev.converterpro.feature.home.presentation.components.ReusableText
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun ConverterScreenRoute(
    mediaFile: List<MediaFile>,
    onBackClick: () -> Unit,
    onConversionComplete: () -> Unit,
) {
    val viewModel: ConverterViewModel = hiltViewModel()
    LaunchedEffect(mediaFile) {
        viewModel.setMediaFile(mediaFile)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.conversionState) {
        if (uiState.conversionState is ConversionState.Completed) {
            delay(COMPLETED_NAVIGATION_DELAY_MS)
            onConversionComplete()
        }
    }

    var showDialog by remember {
        mutableStateOf(false)
    }
    var name by remember {
        mutableStateOf("")
    }
    var uri by remember {
        mutableStateOf("")
    }
    ConverterScreenUi(
        onConvertClick = viewModel::convert,
        onBackClick = onBackClick,
        list = uiState.mediaFile,
        onFileClick = {
            name = it.name?.substringBeforeLast(".") ?: "no name"
            uri = it.uri.toString()
            showDialog = true
        },
        showDialog = showDialog,
        selectedName = name,
        onDismiss = {
            showDialog = false
        },
        onConfirm = { it ->
            viewModel.onSingleFileNameChange(newName = it, uri = uri.toUri())
            showDialog = false
        },
        uiState = uiState,
        onExtensionSelected = viewModel::onExtensionSelected,
        onEncodingSelected = viewModel::onEncodingSelected,
        onBitrateSelected = viewModel::onBitrateSelected,
        onBitrateValueSelected = viewModel::onBitrateValueSelected,
        onChannelSelected = viewModel::onChannelSelected,
        onSampleRateSelected = viewModel::onSampleRateSelected ,
    )

    if (uiState.conversionState !is ConversionState.Idle) {
        ConversionProgressDialog(
            state = uiState.conversionState,
            onErrorDismissed = viewModel::dismissConversionError,
        )
    }
}

@Composable
private fun ConversionProgressDialog(
    state: ConversionState,
    onErrorDismissed: () -> Unit,
) {
    val targetPercent = when (state) {
        is ConversionState.InProgress -> state.percent.coerceIn(0, 100)
        is ConversionState.Completed -> 100
        is ConversionState.Failed -> 0
        ConversionState.Idle -> return
    }
    val message = when (state) {
        is ConversionState.InProgress -> state.message
        is ConversionState.Completed ->
            "Conversion complete (${state.convertedFileCount} file(s))"
        is ConversionState.Failed -> state.message
        ConversionState.Idle -> return
    }
    val animatedProgress by animateFloatAsState(
        targetValue = targetPercent / 100f,
        animationSpec = tween(durationMillis = PROGRESS_ANIM_MS),
        label = "conversionProgress",
    )
    val displayedPercent = (animatedProgress * 100f).roundToInt().coerceIn(0, 100)

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppSurface, shape = MaterialTheme.shapes.large)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (state !is ConversionState.Failed) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(96.dp),
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = PrimaryPlayerBackground,
                        trackColor = DropDownStroke,
                        strokeWidth = 6.dp,
                    )
                    Text(
                        text = "$displayedPercent%",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Text(
                text = message,
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )
            if (state is ConversionState.Failed) {
                Button(
                    onClick = onErrorDismissed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPlayerBackground,
                    ),
                ) {
                    Text("Close", color = Color.Black)
                }
            }
        }
    }
}

private const val PROGRESS_ANIM_MS = 450
private const val COMPLETED_NAVIGATION_DELAY_MS = 1_000L

@Composable
fun ConverterScreenUi(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onConvertClick: () -> Unit,
    list: List<MediaFile>,
    onFileClick: (MediaFile) -> Unit,
    showDialog: Boolean,
    selectedName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    uiState: ConverterUiState,
    onExtensionSelected: (Extension) -> Unit,
    onEncodingSelected: (Encoder) -> Unit,
    onBitrateSelected: (BitRate) -> Unit,
    onBitrateValueSelected: (BitrateValue) -> Unit,
    onChannelSelected: (Channel) -> Unit,
    onSampleRateSelected: (SampleRate) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PrimaryBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ConvertTopBar(
                modifier = Modifier.background(TopBarBackground),
                onConvertClick = onConvertClick,
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingRow(label = "Extension") {
                    CustomDropDown(
                        modifier = Modifier.weight(7f),
                        selectedDropDown = uiState.selectedExtension,
                        dropDownList = uiState.extensionList,
                        onSelectDropDown = onExtensionSelected
                    )
                }

                SettingRow(label = "Encoding") {
                    CustomDropDown(
                        modifier = Modifier.weight(7f),
                        selectedDropDown = uiState.selectedEncoder,
                        dropDownList = uiState.encoderList,
                        onSelectDropDown = onEncodingSelected
                    )
                }

                SettingRow(label = "Bitrate") {
                    Row(
                        modifier = Modifier.weight(7f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CustomDropDown(
                            modifier = Modifier.weight(2f),
                            selectedDropDown = uiState.selectedBitRate,
                            dropDownList = uiState.bitrateList,
                            onSelectDropDown = onBitrateSelected
                        )
                        CustomDropDown(
                            modifier = Modifier.weight(2f),
                            selectedDropDown = uiState.selectedBitrateValue,
                            dropDownList = uiState.bitrateValues,
                            onSelectDropDown = onBitrateValueSelected
                        )
                    }
                }

                SettingRow(label = "Channels") {
                    CustomDropDown(
                        modifier = Modifier.weight(7f),
                        selectedDropDown = uiState.selectedChannel,
                        dropDownList = uiState.channelList,
                        onSelectDropDown = onChannelSelected
                    )
                }

                SettingRow(label = "Sample Rate") {
                    CustomDropDown(
                        modifier = Modifier.weight(7f),
                        selectedDropDown = uiState.selectedSampleRate,
                        dropDownList = uiState.sampleRateList,
                        onSelectDropDown = onSampleRateSelected
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FileBox(
                    file = list,
                    onFileClick = onFileClick
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Tap an item to rename output file",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
        if (showDialog) {
            RenameFileDialog(
                initialName = selectedName,
                onDismiss = onDismiss,
                onConfirm = onConfirm
            )
        }
    }
}

@Composable
private fun SettingRow(
    label: String,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReusableText(
            text = label,
            modifier = Modifier.weight(3f),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
        content()
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    ConverterScreenUi(
        onBackClick = {},
        onConvertClick = {},
        list = emptyList(),
        modifier = Modifier,
        onFileClick = {},
        showDialog = false,
        selectedName = "",
        onDismiss = {},
        onConfirm = {},
        uiState = ConverterUiState(),
        onExtensionSelected = {},
        onEncodingSelected = {},
        onBitrateSelected = {},
        onBitrateValueSelected = {},
        onChannelSelected = {},
        onSampleRateSelected = {},
    )
}
