package ru.mirea.toir.feature.photo.capture.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ru.mirea.toir.core.navigation.PhotoCaptureRoute
import ru.mirea.toir.feature.photo.capture.ui.PhotoCaptureScreen

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
