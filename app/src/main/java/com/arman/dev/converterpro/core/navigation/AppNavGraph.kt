package com.arman.dev.converterpro.core.navigation

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import com.arman.dev.converterpro.core.designsystem.color.PrimaryBackground
import com.arman.dev.converterpro.core.model.MediaFile
import com.arman.dev.converterpro.feature.converter_screen.presentation.ConverterScreenRoute
import com.arman.dev.converterpro.feature.files.presentation.FilesScreenRoute
import com.arman.dev.converterpro.feature.home.presentation.HomeScreenRoute
import com.arman.dev.converterpro.feature.player.presentation.PlayerScreenRoute
import com.arman.dev.converterpro.feature.settings.presentation.SettingsScreenRoute

private const val SELECTED_MEDIA_FILES_KEY = "selected_media_files"
private const val NAV_ANIM_MS = 480

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.ROOT
) {
    HandleIncomingDeepLinks(navController)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.background(PrimaryBackground),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(
                    durationMillis = NAV_ANIM_MS,
                    easing = FastOutSlowInEasing,
                ),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(
                    durationMillis = NAV_ANIM_MS,
                    easing = FastOutSlowInEasing,
                ),
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(
                    durationMillis = NAV_ANIM_MS,
                    easing = FastOutSlowInEasing,
                ),
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(
                    durationMillis = NAV_ANIM_MS,
                    easing = FastOutSlowInEasing,
                ),
            )
        },
    ) {
        navigation(
            route = Routes.ROOT,
            startDestination = Routes.HOME,
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
                        navController.navigate(Routes.FILES)
                    },
                    onSettingClick = {
                        navController.navigate(Routes.SETTINGS)
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
                route = Routes.PLAYER,
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = DeepLinks.PLAYER_URI
                        action = Intent.ACTION_VIEW
                    }
                )
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
}

@Composable
private fun HandleIncomingDeepLinks(navController: NavHostController) {
    val context = LocalContext.current
    DisposableEffect(navController, context) {
        val activity = context as? ComponentActivity
            ?: return@DisposableEffect onDispose { }
        val listener = Consumer<Intent> { intent ->
            activity.intent = intent
            navController.handleDeepLink(intent)
        }
        activity.addOnNewIntentListener(listener)
        onDispose { activity.removeOnNewIntentListener(listener) }
    }
}
