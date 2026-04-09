package dev.nonoxy.kmmtemplate.feature.demo.second.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.nonoxy.kmmtemplate.feature.demo.second.ui.DemoSecondScreen
import dev.nonoxy.kmmtemplate.core.navigation.DemoSecondRoute
import dev.nonoxy.kmmtemplate.core.navigation.Screen
import dev.nonoxy.kmmtemplate.core.navigation.navigateOnResumed

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
