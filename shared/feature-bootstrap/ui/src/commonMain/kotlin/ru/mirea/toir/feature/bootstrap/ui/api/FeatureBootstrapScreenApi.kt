package ru.mirea.toir.feature.bootstrap.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import ru.mirea.toir.core.navigation.Screen
import ru.mirea.toir.core.navigation.navigateOnResumed
import ru.mirea.toir.feature.bootstrap.ui.BootstrapScreen

@Serializable
data object BootstrapRoute : Screen

fun NavController.navigateToBootstrapScreen(
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(BootstrapRoute) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) { inclusive = popUpInclusive }
        }
    }
}

fun NavGraphBuilder.composableBootstrapScreen(
    onNavigateToRoutesList: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    composable<BootstrapRoute> {
        BootstrapScreen(
            onNavigateToRoutesList = onNavigateToRoutesList,
            onNavigateToLogin = onNavigateToLogin,
        )
    }
}
