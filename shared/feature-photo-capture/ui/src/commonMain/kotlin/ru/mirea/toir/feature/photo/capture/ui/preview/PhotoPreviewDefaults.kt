package ru.mirea.toir.feature.photo.capture.ui.preview

import androidx.compose.ui.unit.dp

internal object PhotoPreviewDefaults {
    const val MaxZoom: Float = 4f
    const val DoubleTapZoom: Float = 2.5f
    const val DismissThresholdFraction: Float = 0.25f
    const val DismissVelocityThreshold: Float = 600f
    const val DragResistanceFactor: Float = 0.5f
    const val DragScaleAmplitude: Float = 0.18f
    const val FadeRate: Float = 2f
    val TopScrimHeight = 120.dp
}
