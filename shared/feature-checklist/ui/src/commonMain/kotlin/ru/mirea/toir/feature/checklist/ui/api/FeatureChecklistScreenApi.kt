package ru.mirea.toir.feature.checklist.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import ru.mirea.toir.core.navigation.Screen
import ru.mirea.toir.core.navigation.navigateOnResumed
import ru.mirea.toir.feature.checklist.ui.ChecklistScreen

@Serializable
data class ChecklistRoute(val equipmentResultId: String) : Screen

fun NavController.navigateToChecklistScreen(
    equipmentResultId: String,
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(ChecklistRoute(equipmentResultId)) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) { inclusive = popUpInclusive }
        }
    }
}

fun NavGraphBuilder.composableChecklistScreen(
    onNavigateToPhotoCapture: (checklistItemResultId: String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<ChecklistRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ChecklistRoute>()
        ChecklistScreen(
            equipmentResultId = route.equipmentResultId,
            onNavigateToPhotoCapture = onNavigateToPhotoCapture,
            onNavigateBack = onNavigateBack,
        )
    }
}
