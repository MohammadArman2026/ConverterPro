package com.arman.dev.converterpro.feature.home.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arman.dev.converterpro.core.designsystem.color.AppBackground
import com.arman.dev.converterpro.core.designsystem.color.IconBackground
import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.home.domain.model.SettingOption
import com.arman.dev.converterpro.feature.home.ui.components.AudioFile
import com.arman.dev.converterpro.feature.home.ui.components.HomeScreenBottomBar
import com.arman.dev.converterpro.feature.home.ui.components.HomeScreenTopBar


@Composable
fun HomeScreenRoute(onNextClick: (List<MediaFile>) -> Unit){

    val homeViewModel: HomeViewModel = hiltViewModel()
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val isNextButtonVisible by remember {
        derivedStateOf { uiState.mediaList.isNotEmpty() }
    }

    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            homeViewModel.processUris(uris)
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            homeViewModel.processUris(uris)
        }
    }

    HomeScreenUi(
        modifier = Modifier,
        uiState = uiState,
        onSettingClick = {
            when(it){
                SettingOption.UPGRADE -> {}
                SettingOption.CONVERTED_FILES -> {}
                SettingOption.WRITE_A_FEEDBACK -> {}
                SettingOption.SHARE_THIS_APP -> {}
                SettingOption.MANAGE_SUBSCRIPTION -> {}
                SettingOption.CONTACT_US -> {}
            }
        },
        onNextClick = {
        onNextClick(uiState.mediaList)
        },
        onFilePick = {
            filePickerLauncher.launch(
                arrayOf("audio/*", "video/*")
            )
        },
        onVideoPick = {
            videoPickerLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.VideoOnly
                )
            )
        },
        isNextButtonVisible = isNextButtonVisible,
        onRemoveClick = {
            homeViewModel.removeUri(it)
        }
    )
}


@Composable
fun HomeScreenUi(
    modifier: Modifier = Modifier,
    onSettingClick :(SettingOption)-> Unit,
    onNextClick:()-> Unit,
    onFilePick:()-> Unit,
    onVideoPick:()-> Unit,
    uiState: HomeUiState,
    isNextButtonVisible : Boolean,
    onRemoveClick:(Uri)-> Unit
){
    Column (
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ){
        HomeScreenTopBar(
            modifier = Modifier,
            onSettingClick = onSettingClick,
            isNextButtonVisible = isNextButtonVisible,
            onNextClick = onNextClick
        )

        when{
            uiState.isLoading ->{
                Box(modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center){
                    CircularProgressIndicator(
                        modifier = Modifier,
                        color = IconBackground,
                        strokeWidth = 2.dp
                    )
                }
            }
            uiState.error!=null ->{
                Box(modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center){
                    Text(
                        text = "hello world" ,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                    )
                }
            }
            else->{
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        uiState.mediaList,
                        key = { it.uri }
                    ){item->
                        AudioFile(
                            modifier = Modifier.fillMaxWidth(),
                            mediaFile = item,
                            onRemoveClick = onRemoveClick
                        )
                    }
                }
            }
        }

        HomeScreenBottomBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            onFileClick = onFilePick,
            onVideoClick = onVideoPick
        )
    }
}

@Composable
@Preview(showBackground = true)
fun HomeScreenPreview(){
    HomeScreenUi(
        modifier = Modifier,
        onSettingClick = {},
        onFilePick = {},
        onNextClick = {},
        onVideoPick = {},
        uiState = HomeUiState(),
        isNextButtonVisible = true,
        onRemoveClick = {}
    )
}