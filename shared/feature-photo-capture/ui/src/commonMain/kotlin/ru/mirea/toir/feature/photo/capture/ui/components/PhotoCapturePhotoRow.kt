package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoEntry

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun PhotoCapturePhotoRow(
    photos: ImmutableList<UiPhotoEntry>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onPhotoTap: (uri: String) -> Unit,
    onPhotoLongPress: (uri: String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(items = photos, key = { it.id }) { entry ->
            PhotoCapturePhotoItem(
                entry = entry,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                // Placeholder tiles (fileUri == null) ignore taps per DS spec — the sync
                // manager will fill them in on the next cycle.
                onTap = { entry.fileUri?.let(onPhotoTap) },
                onLongPress = { entry.fileUri?.let(onPhotoLongPress) },
            )
        }
    }
}
