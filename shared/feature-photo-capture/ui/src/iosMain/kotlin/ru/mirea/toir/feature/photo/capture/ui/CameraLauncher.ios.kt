package ru.mirea.toir.feature.photo.capture.ui

import androidx.compose.runtime.Composable

@Composable
actual fun rememberCameraLauncher(onPhotoTaken: (uri: String) -> Unit): () -> Unit {
    // TODO: implement using UIImagePickerController via interop
    return {}
}
