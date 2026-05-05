package ru.mirea.toir.feature.routes.list.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import ru.mirea.toir.core.navigation.Screen
import ru.mirea.toir.core.navigation.navigateOnResumed
import ru.mirea.toir.feature.routes.list.ui.RoutesListScreen

@Serializable
data object RoutesListRoute : Screen

fun NavController.navigateToRoutesListScreen(
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(RoutesListRoute) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) { inclusive = popUpInclusive }
        }
    }
}

fun NavGraphBuilder.composableRoutesListScreen(
    onNavigateToRoutePoints: (inspectionId: String) -> Unit,
) {
    composable<RoutesListRoute> {
        RoutesListScreen(onNavigateToRoutePoints = onNavigateToRoutePoints)
    }
}
