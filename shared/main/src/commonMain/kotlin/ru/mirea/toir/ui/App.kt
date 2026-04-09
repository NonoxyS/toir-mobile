package ru.mirea.toir.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import ru.mirea.toir.feature.demo.first.ui.api.composableDemoFirstScreen
import ru.mirea.toir.feature.demo.second.ui.api.composableDemoSecondScreen
import ru.mirea.toir.feature.demo.second.ui.api.navigateToDemoSecondScreen
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.core.navigation.DemoFirstRoute
import ru.mirea.toir.core.navigation.popBackStackOnResumed

@Composable
fun App() {
    ToirTheme {
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
