package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoEntry

@Composable
internal fun PhotoCaptureContent(
    photos: ImmutableList<UiPhotoEntry>,
    gridState: LazyGridState,
    onPhotoTap: (uri: String) -> Unit,
    onPhotoLongPress: (uri: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (photos.isEmpty()) {
        PhotoCaptureEmptyState(modifier = modifier)
    } else {
        PhotoCapturePhotoGrid(
            photos = photos,
            state = gridState,
            onPhotoTap = onPhotoTap,
            onPhotoLongPress = onPhotoLongPress,
            modifier = modifier,
        )
    }
}
