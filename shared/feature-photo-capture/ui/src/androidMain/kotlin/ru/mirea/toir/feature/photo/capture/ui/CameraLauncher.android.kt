package ru.mirea.toir.feature.photo.capture.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

@Composable
actual fun rememberCameraLauncher(onPhotoTaken: (uri: String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnPhotoTaken = rememberUpdatedState(onPhotoTaken)
    val photoUri = remember { createPhotoUri(context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            currentOnPhotoTaken.value(photoUri.toString())
        }
    }

    return remember(launcher) { { launcher.launch(photoUri) } }
}

private fun createPhotoUri(context: Context): Uri {
    val photosDir = File(context.filesDir, "photos").also { it.mkdirs() }
    val file = File(photosDir, "${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}
