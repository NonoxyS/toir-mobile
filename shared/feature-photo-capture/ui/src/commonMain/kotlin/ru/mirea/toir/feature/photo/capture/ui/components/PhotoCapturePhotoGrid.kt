package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoEntry

@Composable
internal fun PhotoCapturePhotoGrid(
    photos: ImmutableList<UiPhotoEntry>,
    state: LazyGridState,
    onPhotoTap: (uri: String) -> Unit,
    onPhotoLongPress: (uri: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        state = state,
        columns = GridCells.Adaptive(minSize = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier,
    ) {
        items(items = photos, key = { it.id }) { entry ->
            PhotoCapturePhotoItem(
                entry = entry,
                modifier = Modifier.animateItem(),
                onTap = { entry.fileUri?.let(onPhotoTap) },
                onLongPress = { entry.fileUri?.let(onPhotoLongPress) },
            )
        }
    }
}
