package ru.mirea.toir.feature.routes.list.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.mirea.toir.core.navigation.RoutesListRoute
import ru.mirea.toir.feature.routes.list.ui.RoutesListScreen

fun NavGraphBuilder.composableRoutesListScreen(
    onNavigateToRoutePoints: (inspectionId: String) -> Unit,
) {
    composable<RoutesListRoute> {
        RoutesListScreen(onNavigateToRoutePoints = onNavigateToRoutePoints)
    }
}

fun NavController.navigateToRoutesList() {
    navigate(RoutesListRoute)
}
