package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.core.navigation.LocalAnimatedVisibilityScope
import ru.mirea.toir.core.navigation.rememberSharedContentState
import ru.mirea.toir.core.navigation.toirSharedElement
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoEntry
import ru.mirea.toir.feature.photo.capture.ui.LocalPhotoSharedTransitionUri
import ru.mirea.toir.feature.photo.capture.ui.preview.PreviewBoundsTransform
import ru.mirea.toir.feature.photo.capture.ui.preview.animateContentScale
import ru.mirea.toir.res.MR

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun PhotoCapturePhotoItem(
    entry: UiPhotoEntry,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
) {
    val fileUri = entry.fileUri
    if (fileUri == null) {
        PhotoTilePlaceholder(modifier = modifier)
    } else {
        PhotoTile(
            fileUri = fileUri,
            isInSharedTransition = fileUri == LocalPhotoSharedTransitionUri.current,
            onTap = onTap,
            modifier = modifier,
            onLongPress = onLongPress,
        )
    }
}

@Composable
private fun PhotoTilePlaceholder(modifier: Modifier = Modifier) {
    val colors = ToirTheme.colors
    val tileShape = ToirTheme.shapes.md
    val description = stringResource(MR.strings.photo_capture_not_downloaded_content_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(tileShape)
            .background(colors.surface2)
            .border(width = 1.dp, color = colors.borderSubtle, shape = tileShape)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(MR.images.ic_cloud_off),
            contentDescription = null,
            modifier = Modifier.size(PLACEHOLDER_ICON_SIZE),
            colorFilter = ColorFilter.tint(colors.textDisabled),
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PhotoTile(
    fileUri: String,
    isInSharedTransition: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: () -> Unit,
) {
    val colors = ToirTheme.colors
    val tileShape = ToirTheme.shapes.md
    val haptic = LocalHapticFeedback.current
    var painterState by remember { mutableStateOf<AsyncImagePainter.State?>(null) }
    val contentScale = if (isInSharedTransition) {
        LocalAnimatedVisibilityScope.current.animateContentScale(
            visible = ContentScale.Crop,
            hidden = ContentScale.Fit,
            label = "tile-contentScale",
        )
    } else {
        ContentScale.Crop
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(tileShape)
            .background(colors.surface2)
            .border(width = 1.dp, color = colors.borderSubtle, shape = tileShape)
            .toirSharedElement(
                sharedContentState = rememberSharedContentState(key = "photo-$fileUri"),
                boundsTransform = PreviewBoundsTransform,
            )
            .pointerInput(fileUri) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(fileUri)
                .crossfade(true)
                .placeholderMemoryCacheKey(key = "photo-$fileUri")
                .memoryCacheKey(key = "photo-$fileUri")
                .build(),
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize().clip(tileShape),
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

private val PLACEHOLDER_ICON_SIZE = 24.dp
private val LOADING_INDICATOR_SIZE = 20.dp
private val BROKEN_ICON_SIZE = 24.dp
