package ru.mirea.toir.feature.route.points.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import ru.mirea.toir.core.navigation.Screen
import ru.mirea.toir.core.navigation.navigateOnResumed
import ru.mirea.toir.feature.route.points.ui.RoutePointsScreen

@Serializable
data class RoutePointsRoute(val inspectionId: String) : Screen

fun NavController.navigateToRoutePointsScreen(
    inspectionId: String,
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(RoutePointsRoute(inspectionId)) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) { inclusive = popUpInclusive }
        }
    }
}

fun NavGraphBuilder.composableRoutePointsScreen(
    onNavigateToEquipmentCard: (inspectionId: String, routePointId: String) -> Unit,
    onInspectionFinish: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<RoutePointsRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<RoutePointsRoute>()
        RoutePointsScreen(
            inspectionId = route.inspectionId,
            onNavigateToEquipmentCard = onNavigateToEquipmentCard,
            onInspectionFinish = onInspectionFinish,
            onNavigateBack = onNavigateBack,
        )
    }
}
