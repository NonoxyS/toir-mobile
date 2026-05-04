package ru.mirea.toir.feature.photo.capture.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import ru.mirea.toir.core.navigation.Screen
import ru.mirea.toir.core.navigation.navigateOnResumed
import ru.mirea.toir.feature.photo.capture.ui.PhotoCaptureScreen

@Serializable
data class PhotoCaptureRoute(val checklistItemResultId: String) : Screen

fun NavController.navigateToPhotoCaptureScreen(
    checklistItemResultId: String,
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(PhotoCaptureRoute(checklistItemResultId)) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) { inclusive = popUpInclusive }
        }
    }
}

fun NavGraphBuilder.composablePhotoCaptureScreen(
    onPhotoConfirm: () -> Unit,
) {
    composable<PhotoCaptureRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<PhotoCaptureRoute>()
        PhotoCaptureScreen(
            checklistItemResultId = route.checklistItemResultId,
            onPhotoConfirm = onPhotoConfirm,
        )
    }
}
