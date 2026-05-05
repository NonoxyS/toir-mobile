package ru.mirea.toir.feature.photo.capture.ui.preview

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 4f
private const val DOUBLE_TAP_SCALE = 2.5f
private const val ZOOM_ANIM_MS = 200

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun PhotoPreviewScreen(
    photoUri: String,
    photoIndex: Int,
    totalCount: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClose: () -> Unit,
) {
    Scaffold(
        containerColor = ToirTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            MR.strings.photo_capture_preview_title,
                            photoIndex,
                            totalCount,
                        ),
                        style = ToirTheme.typography.displayMedium,
                        color = ToirTheme.colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Image(
                            painter = painterResource(MR.images.ic_close),
                            contentDescription = stringResource(
                                MR.strings.photo_capture_preview_close_content_description,
                            ),
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(ToirTheme.colors.textPrimary),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ToirTheme.colors.background,
                ),
            )
        },
    ) { paddingValues ->
        ZoomableImage(
            photoUri = photoUri,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ToirTheme.colors.background),
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ZoomableImage(
    photoUri: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = ZOOM_ANIM_MS),
        label = "photo-zoom",
    )

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(MIN_SCALE, MAX_SCALE)
        offset = if (scale > 1f) offset + panChange else Offset.Zero
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(photoUri) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > MIN_SCALE) {
                            scale = MIN_SCALE
                            offset = Offset.Zero
                        } else {
                            scale = DOUBLE_TAP_SCALE
                        }
                    },
                )
            }
            .transformable(state = transformableState),
        contentAlignment = Alignment.Center,
    ) {
        with(sharedTransitionScope) {
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "photo-$photoUri"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        translationX = offset.x
                        translationY = offset.y
                    },
            )
        }
    }
}
