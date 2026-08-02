package com.arman.dev.converterpro.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.converter_screen.ui.ConverterScreenRoute
import com.arman.dev.converterpro.feature.home.ui.HomeScreenRoute

private const val SELECTED_MEDIA_FILES_KEY = "selected_media_files"

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = Routes.HOME
        ) {
            HomeScreenRoute { mediaFiles ->
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set(SELECTED_MEDIA_FILES_KEY, ArrayList(mediaFiles))
                navController.navigate(Routes.CONVERTER)
            }
        }

        composable(
            route = Routes.CONVERTER
        ) {
            val mediaFiles = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<ArrayList<MediaFile>>(SELECTED_MEDIA_FILES_KEY)
                .orEmpty()

            ConverterScreenRoute(
                mediaFile = mediaFiles,
                onBackClick = navController::popBackStack
            )
        }
    }
}
