package ru.mirea.toir.feature.bootstrap.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.mirea.toir.core.navigation.BootstrapRoute
import ru.mirea.toir.feature.bootstrap.ui.BootstrapScreen

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
