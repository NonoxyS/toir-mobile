@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.mirea.toir.core.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize.Companion.ContentSize
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

@Suppress("CompositionLocalAllowlist")
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope> {
    error("CompositionLocal LocalSharedTransitionScope was not provided")
}

@Suppress("CompositionLocalAllowlist")
val LocalAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope> {
    error("CompositionLocal LocalAnimatedVisibilityScope was not provided")
}

fun Modifier.toirSharedElement(
    sharedContentState: SharedContentState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
    placeholderSize: PlaceholderSize = ContentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: OverlayClip = ParentClip,
): Modifier = with(sharedTransitionScope) {
    this@toirSharedElement
        .sharedElement(
            sharedContentState = sharedContentState,
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = boundsTransform,
            placeholderSize = placeholderSize,
            renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            zIndexInOverlay = zIndexInOverlay,
            clipInOverlayDuringTransition = clipInOverlayDuringTransition
        )
}

fun Modifier.toirSharedElement(
    sharedContentState: SharedContentState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
    placeholderSize: PlaceholderSize = ContentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: OverlayClip = ParentClip,
): Modifier = composed {
    with(LocalSharedTransitionScope.current) {
        this@toirSharedElement
            .sharedElement(
                sharedContentState = sharedContentState,
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = boundsTransform,
                placeholderSize = placeholderSize,
                renderInOverlayDuringTransition = renderInOverlayDuringTransition,
                zIndexInOverlay = zIndexInOverlay,
                clipInOverlayDuringTransition = clipInOverlayDuringTransition
            )
    }
}

fun Modifier.toirSharedElement(
    sharedContentState: SharedContentState,
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
    placeholderSize: PlaceholderSize = ContentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: OverlayClip = ParentClip,
): Modifier = composed {
    with(LocalSharedTransitionScope.current) {
        this@toirSharedElement
            .sharedElement(
                sharedContentState = sharedContentState,
                animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
                boundsTransform = boundsTransform,
                placeholderSize = placeholderSize,
                renderInOverlayDuringTransition = renderInOverlayDuringTransition,
                zIndexInOverlay = zIndexInOverlay,
                clipInOverlayDuringTransition = clipInOverlayDuringTransition
            )
    }
}

@Composable
fun rememberSharedContentState(
    key: String,
    sharedTransitionScope: SharedTransitionScope = LocalSharedTransitionScope.current,
): SharedContentState = with(sharedTransitionScope) { rememberSharedContentState(key = key) }

private val DefaultSpring =
    spring(stiffness = StiffnessMediumLow, visibilityThreshold = Rect.VisibilityThreshold)

@ExperimentalSharedTransitionApi
private val ParentClip: OverlayClip =
    object : OverlayClip {
        override fun getClipPath(
            sharedContentState: SharedContentState,
            bounds: Rect,
            layoutDirection: LayoutDirection,
            density: Density,
        ): Path? {
            return sharedContentState.parentSharedContentState?.clipPathInOverlay
        }
    }

private val DefaultBoundsTransform = BoundsTransform { _, _ ->
    spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = StiffnessMediumLow,
        visibilityThreshold = Rect.VisibilityThreshold,
    )
}
