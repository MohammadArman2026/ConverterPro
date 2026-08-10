package com.arman.dev.converterpro.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.converter_screen.presentation.ConverterScreenRoute
import com.arman.dev.converterpro.feature.files.presentation.FilesScreenRoute
import com.arman.dev.converterpro.feature.home.presentation.HomeScreenRoute
import com.arman.dev.converterpro.feature.player.presentation.PlayerScreenRoute
import com.arman.dev.converterpro.feature.settings.presentation.SettingsScreenRoute

private const val SELECTED_MEDIA_FILES_KEY = "selected_media_files"
private const val NAV_ANIM_MS = 320

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(NAV_ANIM_MS),
            ) + fadeIn(animationSpec = tween(NAV_ANIM_MS))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(NAV_ANIM_MS),
            ) + fadeOut(animationSpec = tween(NAV_ANIM_MS))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(NAV_ANIM_MS),
            ) + fadeIn(animationSpec = tween(NAV_ANIM_MS))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(NAV_ANIM_MS),
            ) + fadeOut(animationSpec = tween(NAV_ANIM_MS))
        },
    ) {
        composable(
            route = Routes.HOME
        ) {
            HomeScreenRoute(
                onNextClick = { mediaFiles ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set(SELECTED_MEDIA_FILES_KEY, ArrayList(mediaFiles))
                    navController.navigate(Routes.CONVERTER)
                },
                onFileClick = {
                    navController.navigateSingleTop(Routes.FILES)
                },
                onSettingClick = {
                    navController.navigateSingleTop(Routes.SETTINGS)
                },
            )
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
                onBackClick = navController::popBackStack,
                onConversionComplete = {
                    navController.navigate(Routes.FILES) {
                        popUpTo(Routes.CONVERTER) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(
            route = Routes.FILES
        ) {
            FilesScreenRoute(
                onOpenPlayer = { navController.navigateSingleTop(Routes.PLAYER) },
                onBackClick = navController::popBackStack
            )
        }

        composable(
            route = Routes.PLAYER
        ) {
            PlayerScreenRoute(onBackClick = navController::popBackStack)
        }

        composable(
            route = Routes.SETTINGS
        ) {
            SettingsScreenRoute(
                onBackClick = navController::popBackStack,
            )
        }
    }
}
