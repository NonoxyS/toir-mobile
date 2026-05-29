package ru.mirea.toir.feature.photo.capture.ui.preview

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlin.math.abs
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.core.navigation.LocalAnimatedVisibilityScope
import ru.mirea.toir.core.navigation.rememberSharedContentState
import ru.mirea.toir.core.navigation.toirSharedElement
import ru.mirea.toir.res.MR

@OptIn(ExperimentalSharedTransitionApi::class)
internal val PreviewBoundsTransform: BoundsTransform = BoundsTransform { _, _ ->
    spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = Rect.VisibilityThreshold,
    )
}

private const val UnzoomedEpsilon = 1.01f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoPreviewScreen(
    photoUri: String,
    photoIndex: Int,
    totalCount: Int,
    onClose: () -> Unit,
) {
    val dragState = rememberPhotoPreviewDragState()
    val zoomState = rememberZoomState(maxScale = PhotoPreviewDefaults.MaxZoom)
    val avScope = LocalAnimatedVisibilityScope.current
    val scope = rememberCoroutineScope()
    val transitionAlpha by avScope.transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )
        },
        label = "preview-transition-alpha",
    ) { enterExit -> if (enterExit == EnterExitState.Visible) 1f else 0f }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val containerAlpha: () -> Float = {
            val dragFade = (1f - abs(dragState.dragFraction) * PhotoPreviewDefaults.FadeRate)
                .coerceIn(0f, 1f)
            dragFade * transitionAlpha
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = containerAlpha() }
                .background(ToirTheme.colors.background),
        )

        PhotoPreviewImage(
            photoUri = photoUri,
            dragState = dragState,
            zoomState = zoomState,
            onClose = onClose,
            onDoubleTap = { position ->
                val target = if (zoomState.scale > UnzoomedEpsilon) 1f else PhotoPreviewDefaults.DoubleTapZoom
                scope.launch { zoomState.changeScale(target, position) }
            },
            modifier = Modifier.fillMaxSize(),
        )

        PhotoPreviewChrome(
            photoIndex = photoIndex,
            totalCount = totalCount,
            alpha = containerAlpha,
            onClose = onClose,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PhotoPreviewImage(
    photoUri: String,
    dragState: PhotoPreviewDragState,
    zoomState: net.engawapg.lib.zoomable.ZoomState,
    onClose: () -> Unit,
    onDoubleTap: (androidx.compose.ui.geometry.Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val avScope = LocalAnimatedVisibilityScope.current
    val animatedContentScale = avScope.animateContentScale(
        visible = ContentScale.Fit,
        hidden = ContentScale.Crop,
        label = "preview-contentScale",
    )

    Box(
        modifier = modifier
            .onSizeChanged { dragState.setLayoutSize(Size(it.width.toFloat(), it.height.toFloat())) }
            .photoDismissDrag(
                state = dragState,
                isZoomed = { zoomState.scale > UnzoomedEpsilon },
                onClose = onClose,
                onDoubleTap = onDoubleTap,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, dragState.dragOffsetY.toInt()) }
                .toirSharedElement(
                    sharedContentState = rememberSharedContentState(key = "photo-$photoUri"),
                    boundsTransform = PreviewBoundsTransform,
                )
                .graphicsLayer {
                    val dragShrink = 1f - abs(dragState.dragFraction).coerceIn(0f, 1f) *
                        PhotoPreviewDefaults.DragScaleAmplitude
                    scaleX = dragShrink
                    scaleY = dragShrink
                },
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(photoUri)
                    .crossfade(true)
                    .placeholderMemoryCacheKey(key = "photo-$photoUri")
                    .memoryCacheKey(key = "photo-$photoUri")
                    .build(),
                contentDescription = null,
                contentScale = animatedContentScale,
                onSuccess = { result ->
                    val image = result.result.image
                    zoomState.setContentSize(Size(image.width.toFloat(), image.height.toFloat()))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(
                        zoomState = zoomState,
                        enableOneFingerZoom = false,
                        onDoubleTap = null,
                    ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxScope.PhotoPreviewChrome(
    photoIndex: Int,
    totalCount: Int,
    alpha: () -> Float,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .height(PhotoPreviewDefaults.TopScrimHeight)
            .graphicsLayer { this.alpha = alpha() }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.45f),
                        Color.Transparent,
                    ),
                ),
            ),
    )

    TopAppBar(
        title = {
            Text(
                text = stringResource(MR.strings.photo_capture_preview_title, photoIndex, totalCount),
                style = ToirTheme.typography.displayMedium,
                color = ToirTheme.colors.textPrimary,
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Image(
                    painter = painterResource(MR.images.ic_close),
                    contentDescription = stringResource(MR.strings.photo_capture_preview_close_content_description),
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(ToirTheme.colors.textPrimary),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .graphicsLayer { this.alpha = alpha() },
    )
}
