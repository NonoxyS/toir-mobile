package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
internal fun PhotoCapturePhotoItem(
    uri: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = uri,
        contentDescription = null,
        modifier = modifier.size(120.dp),
    )
}
