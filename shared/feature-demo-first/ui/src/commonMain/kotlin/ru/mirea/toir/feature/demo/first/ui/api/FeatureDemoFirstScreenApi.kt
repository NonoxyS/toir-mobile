package ru.mirea.toir.feature.demo.first.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.mirea.toir.feature.demo.first.ui.DemoFirstScreen
import ru.mirea.toir.core.navigation.DemoFirstRoute
import ru.mirea.toir.core.navigation.Screen
import ru.mirea.toir.core.navigation.navigateOnResumed

fun NavController.navigateToDemoFirstScreen(
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(DemoFirstRoute) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) {
                inclusive = popUpInclusive
            }
        }
    }
}

fun NavGraphBuilder.composableDemoFirstScreen(
    onNavigateToSecond: () -> Unit,
) {
    composable<DemoFirstRoute> {
        DemoFirstScreen(onNavigateToSecond = onNavigateToSecond)
    }
}
