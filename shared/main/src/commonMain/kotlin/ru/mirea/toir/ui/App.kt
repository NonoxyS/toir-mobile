package ru.mirea.toir.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.core.navigation.AuthRoute
import ru.mirea.toir.core.navigation.BootstrapRoute
import ru.mirea.toir.core.navigation.ChecklistRoute
import ru.mirea.toir.core.navigation.EquipmentCardRoute
import ru.mirea.toir.core.navigation.RoutePointsRoute
import ru.mirea.toir.core.navigation.RoutesListRoute
import ru.mirea.toir.feature.auth.ui.api.composableAuthScreen
import ru.mirea.toir.feature.bootstrap.ui.api.composableBootstrapScreen
import ru.mirea.toir.feature.equipment.card.ui.api.composableEquipmentCardScreen
import ru.mirea.toir.feature.route.points.ui.api.composableRoutePointsScreen
import ru.mirea.toir.feature.routes.list.ui.api.composableRoutesListScreen

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
            composableRoutesListScreen(
                onNavigateToRoutePoints = { inspectionId ->
                    navController.navigate(RoutePointsRoute(inspectionId))
                },
            )
            composableRoutePointsScreen(
                onNavigateToEquipmentCard = { inspectionId, routePointId ->
                    navController.navigate(EquipmentCardRoute(inspectionId, routePointId))
                },
                onInspectionFinish = {
                    navController.popBackStack(RoutesListRoute, inclusive = false)
                },
            )
            composableEquipmentCardScreen(
                onNavigateToChecklist = { equipmentResultId ->
                    navController.navigate(ChecklistRoute(equipmentResultId))
                },
                onNavigateBack = navController::popBackStack,
            )
        }
    }
}
