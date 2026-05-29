package ru.mirea.toir.feature.photo.capture.ui.preview

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size

@Stable
internal class PhotoPreviewDragState(
    private val dragResistance: Float,
) {
    private var dragOffsetYState by mutableFloatStateOf(0f)
    val dragOffsetY: Float get() = dragOffsetYState

    private var layoutSizeState by mutableStateOf(Size.Zero)

    val dragFraction: Float
        get() = if (layoutSizeState.height <= 0f) 0f else dragOffsetYState / layoutSizeState.height

    fun setLayoutSize(size: Size) {
        layoutSizeState = size
    }

    fun applyDragDelta(deltaY: Float) {
        dragOffsetYState += deltaY * dragResistance
    }

    suspend fun snapBackDrag() {
        animate(dragOffsetYState, 0f, animationSpec = SnapBackSpec) { value, _ ->
            dragOffsetYState = value
        }
    }

    private companion object {
        val SnapBackSpec = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        )
    }
}

@Composable
internal fun rememberPhotoPreviewDragState(
    dragResistance: Float = PhotoPreviewDefaults.DragResistanceFactor,
): PhotoPreviewDragState = remember(dragResistance) {
    PhotoPreviewDragState(dragResistance = dragResistance)
}
