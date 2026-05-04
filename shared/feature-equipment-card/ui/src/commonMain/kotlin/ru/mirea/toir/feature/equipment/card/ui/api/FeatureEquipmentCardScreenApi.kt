package ru.mirea.toir.feature.equipment.card.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import ru.mirea.toir.core.navigation.Screen
import ru.mirea.toir.core.navigation.navigateOnResumed
import ru.mirea.toir.feature.equipment.card.ui.EquipmentCardScreen

@Serializable
data class EquipmentCardRoute(
    val inspectionId: String,
    val routePointId: String,
) : Screen

fun NavController.navigateToEquipmentCardScreen(
    inspectionId: String,
    routePointId: String,
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(EquipmentCardRoute(inspectionId, routePointId)) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) { inclusive = popUpInclusive }
        }
    }
}

fun NavGraphBuilder.composableEquipmentCardScreen(
    onNavigateToChecklist: (equipmentResultId: String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<EquipmentCardRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<EquipmentCardRoute>()
        EquipmentCardScreen(
            inspectionId = route.inspectionId,
            routePointId = route.routePointId,
            onNavigateToChecklist = onNavigateToChecklist,
            onNavigateBack = onNavigateBack,
        )
    }
}
