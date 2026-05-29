package ru.mirea.toir.feature.photo.capture.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The URI of the photo currently participating in the grid<->preview shared transition,
 * or null when no transition is in progress.
 *
 * Provided once at the PhotoCaptureScreen root so grid tiles can selectively opt into
 * the ContentScale interpolation only when they're the one being morphed. Without this
 * gate, every tile would animate Crop<->Fit on each preview open/close because they all
 * share the parent AnimatedContent's transition.
 */
@Suppress("CompositionLocalAllowlist")
internal val LocalPhotoSharedTransitionUri = staticCompositionLocalOf<String?> { null }

@Composable
internal fun rememberSharedTransitionUri(activePreviewUri: String?): String? {
    var lastNonNull by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(activePreviewUri) {
        if (activePreviewUri != null) lastNonNull = activePreviewUri
    }
    return activePreviewUri ?: lastNonNull
}
