package ru.mirea.toir.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.core.navigation.AuthRoute
import ru.mirea.toir.core.navigation.BootstrapRoute
import ru.mirea.toir.core.navigation.RoutesListRoute
import ru.mirea.toir.feature.auth.ui.api.composableAuthScreen
import ru.mirea.toir.feature.bootstrap.ui.api.composableBootstrapScreen

@Composable
fun App() {
    ToirTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = AuthRoute,
        ) {
            composableAuthScreen(
                onNavigateToMain = {
                    navController.navigate(BootstrapRoute) {
                        popUpTo(AuthRoute) { inclusive = true }
                    }
                },
            )
            composableBootstrapScreen(
                onNavigateToRoutesList = {
                    navController.navigate(RoutesListRoute) {
                        popUpTo(BootstrapRoute) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AuthRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
            // composableRoutesListScreen(...) — добавить в Waypoint 04
        }
    }
}
