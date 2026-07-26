package com.arman.dev.converterpro.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.arman.dev.converterpro.feature.converter_screen.ui.ConverterScreenRoute

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
            ConverterScreenRoute(
                mediaFile = emptyList(),
                onBackClick = {}
            )
        }
    }
}
