package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoEntry
import ru.mirea.toir.res.MR

private val TileSize = 120.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun PhotoCapturePhotoItem(
    entry: UiPhotoEntry,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
) {
    val colors = ToirTheme.colors
    val fileUri = entry.fileUri
    val tileShape = ToirTheme.shapes.md
    val baseModifier = modifier
        .size(TileSize)
        .clip(tileShape)
        .background(colors.surface2)
        .border(width = 1.dp, color = colors.borderSubtle, shape = tileShape)

    if (fileUri == null) {
        // Restored photo, file not yet on disk (sync manager will download in background).
        // Per DS spec (docs/design-system/pages/photo-capture.md): no shared element,
        // no taps, just a static placeholder so the user sees the count is preserved.
        val description = stringResource(MR.strings.photo_capture_not_downloaded_content_description)
        Box(
            modifier = baseModifier.semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(MR.images.ic_cloud_off),
                contentDescription = null,
                modifier = Modifier.size(PLACEHOLDER_ICON_SIZE),
                colorFilter = ColorFilter.tint(colors.textDisabled),
            )
        }
        return
    }

    var painterState by remember { mutableStateOf<AsyncImagePainter.State?>(null) }
    with(sharedTransitionScope) {
        Box(
            modifier = baseModifier
                .sharedElement(
                    sharedContentState = rememberSharedContentState(key = "photo-$fileUri"),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                .pointerInput(fileUri) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onLongPress = { onLongPress() },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = fileUri,
                contentDescription = null,
                modifier = Modifier.size(TileSize).clip(tileShape),
                onState = { painterState = it },
            )
            when (painterState) {
                is AsyncImagePainter.State.Loading -> CircularProgressIndicator(
                    modifier = Modifier.size(LOADING_INDICATOR_SIZE),
                    color = colors.textSecondary,
                )
                is AsyncImagePainter.State.Error -> Image(
                    painter = painterResource(MR.images.ic_broken_image),
                    contentDescription = null,
                    modifier = Modifier.size(BROKEN_ICON_SIZE),
                    colorFilter = ColorFilter.tint(colors.textDisabled),
                )
                else -> Unit
            }
        }
    }
}

private val PLACEHOLDER_ICON_SIZE = 24.dp
private val LOADING_INDICATOR_SIZE = 20.dp
private val BROKEN_ICON_SIZE = 24.dp
