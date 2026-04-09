package ru.mirea.toir.feature.demo.second.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.mirea.toir.feature.demo.second.ui.DemoSecondScreen
import ru.mirea.toir.core.navigation.DemoSecondRoute
import ru.mirea.toir.core.navigation.Screen
import ru.mirea.toir.core.navigation.navigateOnResumed

fun NavController.navigateToDemoSecondScreen(
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(DemoSecondRoute) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) {
                inclusive = popUpInclusive
            }
        }
    }
}

fun NavGraphBuilder.composableDemoSecondScreen(
    onNavigateBack: () -> Unit,
) {
    composable<DemoSecondRoute> {
        DemoSecondScreen(onNavigateBack = onNavigateBack)
    }
}
