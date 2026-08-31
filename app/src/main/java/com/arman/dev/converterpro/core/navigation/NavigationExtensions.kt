package com.arman.dev.converterpro.core.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

fun NavHostController.openPlayerFromDeepLink() {
    if (currentDestination?.route == Routes.PLAYER) return
    if (popBackStack(Routes.PLAYER, inclusive = false)) return
    if (currentDestination?.route != Routes.FILES) {
        navigate(Routes.FILES) { launchSingleTop = true }
    }
    navigate(Routes.PLAYER) { launchSingleTop = true }
}

fun NavHostController.navigateAndClearBackStack(route: String) {
    navigate(route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}
