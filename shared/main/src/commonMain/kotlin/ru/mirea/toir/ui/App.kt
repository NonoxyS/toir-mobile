package ru.mirea.toir.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.auth.ui.api.AuthRoute
import ru.mirea.toir.feature.auth.ui.api.composableAuthScreen
import ru.mirea.toir.feature.auth.ui.api.navigateToAuthScreen
import ru.mirea.toir.feature.bootstrap.ui.api.BootstrapRoute
import ru.mirea.toir.feature.bootstrap.ui.api.composableBootstrapScreen
import ru.mirea.toir.feature.bootstrap.ui.api.navigateToBootstrapScreen
import ru.mirea.toir.feature.checklist.ui.api.composableChecklistScreen
import ru.mirea.toir.feature.checklist.ui.api.navigateToChecklistScreen
import ru.mirea.toir.feature.equipment.card.ui.api.composableEquipmentCardScreen
import ru.mirea.toir.feature.equipment.card.ui.api.navigateToEquipmentCardScreen
import ru.mirea.toir.feature.photo.capture.ui.api.composablePhotoCaptureScreen
import ru.mirea.toir.feature.photo.capture.ui.api.navigateToPhotoCaptureScreen
import ru.mirea.toir.feature.route.points.ui.api.composableRoutePointsScreen
import ru.mirea.toir.feature.route.points.ui.api.navigateToRoutePointsScreen
import ru.mirea.toir.feature.routes.list.ui.api.RoutesListRoute
import ru.mirea.toir.feature.routes.list.ui.api.composableRoutesListScreen
import ru.mirea.toir.feature.routes.list.ui.api.navigateToRoutesListScreen

@Composable
fun App() {
    ToirTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = BootstrapRoute,
        ) {
            composableAuthScreen(
                onNavigateToMain = {
                    navController.navigateToBootstrapScreen(popUpToScreen = AuthRoute)
                },
            )
            composableBootstrapScreen(
                onNavigateToRoutesList = {
                    navController.navigateToRoutesListScreen(popUpToScreen = BootstrapRoute)
                },
                onNavigateToLogin = {
                    navController.navigateToAuthScreen(popUpToScreen = BootstrapRoute)
                },
            )
            composableRoutesListScreen(
                onNavigateToRoutePoints = { inspectionId ->
                    navController.navigateToRoutePointsScreen(inspectionId)
                },
            )
            composableRoutePointsScreen(
                onNavigateToEquipmentCard = { inspectionId, routePointId ->
                    navController.navigateToEquipmentCardScreen(inspectionId, routePointId)
                },
                onInspectionFinish = {
                    navController.popBackStack(RoutesListRoute, inclusive = false)
                },
                onNavigateBack = { navController.popBackStack() },
            )
            composableEquipmentCardScreen(
                onNavigateToChecklist = { equipmentResultId ->
                    navController.navigateToChecklistScreen(equipmentResultId)
                },
                onNavigateBack = { navController.popBackStack() },
            )
            composableChecklistScreen(
                onNavigateToPhotoCapture = { checklistItemResultId ->
                    navController.navigateToPhotoCaptureScreen(checklistItemResultId)
                },
                onNavigateBack = { navController.popBackStack() },
            )
            composablePhotoCaptureScreen(
                onPhotoConfirm = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
