package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PhotoCapturePhotoItem(
    uri: String,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = uri,
        contentDescription = null,
        modifier = modifier
            .size(120.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress,
            ),
    )
}
