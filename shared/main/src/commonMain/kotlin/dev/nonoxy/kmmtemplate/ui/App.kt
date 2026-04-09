package dev.nonoxy.kmmtemplate.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.nonoxy.kmmtemplate.feature.demo.first.ui.api.composableDemoFirstScreen
import dev.nonoxy.kmmtemplate.feature.demo.second.ui.api.composableDemoSecondScreen
import dev.nonoxy.kmmtemplate.feature.demo.second.ui.api.navigateToDemoSecondScreen
import dev.nonoxy.kmmtemplate.common.ui.compose.theme.KmmTemplateTheme
import dev.nonoxy.kmmtemplate.core.navigation.DemoFirstRoute
import dev.nonoxy.kmmtemplate.core.navigation.popBackStackOnResumed

@Composable
fun App() {
    KmmTemplateTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = DemoFirstRoute,
        ) {
            composableDemoFirstScreen(onNavigateToSecond = navController::navigateToDemoSecondScreen)
            composableDemoSecondScreen(onNavigateBack = navController::popBackStackOnResumed)
        }
    }
}
