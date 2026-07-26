package com.arman.dev.converterpro.feature.converter_screen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arman.dev.converterpro.core.designsystem.color.AppBackground
import com.arman.dev.converterpro.core.designsystem.color.AppSurface
import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.converter_screen.domain.model.BitRate
import com.arman.dev.converterpro.feature.converter_screen.domain.model.BitrateValue
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Channel
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Encoder
import com.arman.dev.converterpro.feature.converter_screen.domain.model.Extension
import com.arman.dev.converterpro.feature.converter_screen.domain.model.SampleRate
import com.arman.dev.converterpro.feature.converter_screen.ui.components.ConverterScreenTopBar
import com.arman.dev.converterpro.feature.converter_screen.ui.components.CustomDropDown
import com.arman.dev.converterpro.feature.converter_screen.ui.components.FileBox
import com.arman.dev.converterpro.feature.converter_screen.ui.components.RenameFileDialog
import com.arman.dev.converterpro.feature.home.ui.components.ReusableText

@Composable
fun ConverterScreenRoute(
    mediaFile: List<MediaFile>,
    onBackClick: () -> Unit
) {
    val viewModel: ConverterViewModel = hiltViewModel()
    LaunchedEffect(mediaFile) {
        viewModel.setMediaFile(mediaFile)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        onConvertClick = {},
        onBackClick = {},
        list = mediaFile,
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
}


@Composable
fun ConverterScreenUi(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onConvertClick: () -> Unit,
    list: List<MediaFile>,
    onFileClick:(MediaFile)-> Unit,
    showDialog: Boolean,
    selectedName: String,
    onDismiss:()-> Unit,
    onConfirm:(String)-> Unit,
    uiState: ConverterUiState,
    onExtensionSelected:(Extension)-> Unit,
    onEncodingSelected:(Encoder)-> Unit,
    onBitrateSelected:(BitRate)-> Unit,
    onBitrateValueSelected:(BitrateValue)-> Unit,
    onChannelSelected:(Channel)-> Unit,
    onSampleRateSelected:(SampleRate)->Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            ConverterScreenTopBar(
                modifier = Modifier
                    .background(AppSurface),
                onConvertClick = onConvertClick,
                onBackClick = onBackClick
            )

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReusableText(
                        text = "Extension",
                        modifier = Modifier.weight(3f),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                    CustomDropDown(
                        modifier = Modifier.weight(7f),
                        selectedDropDown = uiState.selectedExtension,
                        dropDownList = uiState.extensionList,
                        onSelectDropDown = onExtensionSelected
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReusableText(
                        text = "Encoding",
                        modifier = Modifier.weight(3f),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                    CustomDropDown(
                        modifier = Modifier.weight(7f),
                        selectedDropDown = uiState.selectedEncoder,
                        dropDownList = uiState.encoderList,
                        onSelectDropDown = onEncodingSelected
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReusableText(
                        text = "Bitrate",
                        modifier = Modifier.weight(3f),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReusableText(
                        text = "Channels",
                        modifier = Modifier.weight(3f),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                    CustomDropDown(
                        modifier = Modifier.weight(7f),
                        selectedDropDown = uiState.selectedChannel,
                        dropDownList = uiState.channelList,
                        onSelectDropDown = onChannelSelected
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReusableText(
                        text = "Sample Rate",
                        modifier = Modifier.weight(3f),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                    CustomDropDown(
                        modifier = Modifier.weight(7f),
                        selectedDropDown = uiState.selectedSampleRate,
                        dropDownList = uiState.sampleRateList,
                        onSelectDropDown = onSampleRateSelected
                    )
                }

                Spacer(Modifier.weight(1f))
                FileBox(
                    file = list,
                    onFileClick = onFileClick
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text ="Tap an item to rename output file",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp).navigationBarsPadding())
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