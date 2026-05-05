package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun PhotoCaptureContent(
    photos: ImmutableList<String>,
    onPhotoTap: (uri: String) -> Unit,
    onPhotoLongPress: (uri: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (photos.isEmpty()) {
        PhotoCaptureEmptyState(modifier = modifier)
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            PhotoCapturePhotoRow(
                photos = photos,
                onPhotoTap = onPhotoTap,
                onPhotoLongPress = onPhotoLongPress,
            )
        }
    }
}
