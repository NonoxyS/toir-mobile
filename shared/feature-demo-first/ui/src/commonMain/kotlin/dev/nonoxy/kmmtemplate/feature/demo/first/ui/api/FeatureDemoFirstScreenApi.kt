package dev.nonoxy.kmmtemplate.feature.demo.first.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.nonoxy.kmmtemplate.feature.demo.first.ui.DemoFirstScreen
import dev.nonoxy.kmmtemplate.core.navigation.DemoFirstRoute
import dev.nonoxy.kmmtemplate.core.navigation.Screen
import dev.nonoxy.kmmtemplate.core.navigation.navigateOnResumed

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
