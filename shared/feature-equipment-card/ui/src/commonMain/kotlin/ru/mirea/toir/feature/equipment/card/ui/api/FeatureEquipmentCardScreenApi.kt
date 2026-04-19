package ru.mirea.toir.feature.equipment.card.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ru.mirea.toir.core.navigation.EquipmentCardRoute
import ru.mirea.toir.feature.equipment.card.ui.EquipmentCardScreen

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
