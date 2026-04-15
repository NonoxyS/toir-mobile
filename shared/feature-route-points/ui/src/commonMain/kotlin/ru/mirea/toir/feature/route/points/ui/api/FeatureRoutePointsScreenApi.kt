package ru.mirea.toir.feature.route.points.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ru.mirea.toir.core.navigation.RoutePointsRoute
import ru.mirea.toir.feature.route.points.ui.RoutePointsScreen

fun NavGraphBuilder.composableRoutePointsScreen(
    onNavigateToEquipmentCard: (inspectionId: String, routePointId: String) -> Unit,
    onInspectionFinish: () -> Unit,
) {
    composable<RoutePointsRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<RoutePointsRoute>()
        RoutePointsScreen(
            inspectionId = route.inspectionId,
            onNavigateToEquipmentCard = onNavigateToEquipmentCard,
            onInspectionFinish = onInspectionFinish,
        )
    }
}
