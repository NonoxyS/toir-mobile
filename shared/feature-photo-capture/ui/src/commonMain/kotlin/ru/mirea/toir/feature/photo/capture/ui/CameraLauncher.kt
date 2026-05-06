package ru.mirea.toir.feature.photo.capture.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch

@Composable
fun rememberCameraLauncher(
    onPhotoTaken: (uri: String) -> Unit,
    onPermissionDenial: (permanent: Boolean) -> Unit,
): () -> Unit {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)

    val triggerCapture = rememberPlatformCameraTrigger(onPhotoTaken)
    val currentOnDenial = rememberUpdatedState(onPermissionDenial)
    val scope = rememberCoroutineScope()

    return remember(controller, triggerCapture) {
        {
            scope.launch {
                try {
                    controller.providePermission(Permission.CAMERA)
                    triggerCapture()
                } catch (_: DeniedAlwaysException) {
                    currentOnDenial.value(true)
                } catch (_: DeniedException) {
                    currentOnDenial.value(false)
                } catch (_: RequestCanceledException) {
                    // user cancelled before responding — treat as no-op
                }
            }
        }
    }
}

@Composable
internal expect fun rememberPlatformCameraTrigger(
    onPhotoTaken: (uri: String) -> Unit,
): () -> Unit
