package ru.mirea.toir.feature.photo.capture.ui

import androidx.compose.runtime.Composable

@Composable
expect fun rememberCameraLauncher(onPhotoTaken: (uri: String) -> Unit): () -> Unit
