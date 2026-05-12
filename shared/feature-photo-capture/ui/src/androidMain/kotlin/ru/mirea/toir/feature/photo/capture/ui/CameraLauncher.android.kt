package ru.mirea.toir.feature.photo.capture.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

@Composable
internal actual fun rememberPlatformCameraTrigger(
    onPhotoTaken: (uri: String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val currentOnPhotoTaken = rememberUpdatedState(onPhotoTaken)
    val pendingUri = remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val captured = pendingUri.value
        pendingUri.value = null
        if (success && captured != null) {
            currentOnPhotoTaken.value(captured.toString())
        }
    }

    return remember(launcher) {
        {
            val uri = createPhotoUri(context)
            pendingUri.value = uri
            launcher.launch(uri)
        }
    }
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
