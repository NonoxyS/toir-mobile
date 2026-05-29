package ru.mirea.toir.feature.photo.capture.ui.preview

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.layout.lerp

@Composable
internal fun AnimatedVisibilityScope.animateContentScale(
    visible: ContentScale,
    hidden: ContentScale,
    animationSpec: FiniteAnimationSpec<Float> = DefaultContentScaleSpec,
    label: String = "contentScale",
): ContentScale {
    val fraction by transition.animateFloat(
        transitionSpec = { animationSpec },
        label = label,
    ) { state -> if (state == EnterExitState.Visible) 1f else 0f }

    return remember(visible, hidden) {
        object : ContentScale {
            override fun computeScaleFactor(
                srcSize: Size,
                dstSize: Size,
            ): ScaleFactor = lerp(
                start = hidden.computeScaleFactor(srcSize, dstSize),
                stop = visible.computeScaleFactor(srcSize, dstSize),
                fraction = fraction.coerceIn(0f, 1f),
            )
        }
    }
}

private val DefaultContentScaleSpec: FiniteAnimationSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = StiffnessMediumLow,
)
