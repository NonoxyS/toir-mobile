package ru.mirea.toir.common.ui.compose.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

@Suppress("CompositionLocalAllowlist")
internal val LocalToirThemeShapes = staticCompositionLocalOf<ToirShapes> {
    error("CompositionLocal LocalToirThemeShapes was not provided")
}

@Immutable
class ToirShapes internal constructor(
    val cornerRadius4: CornerBasedShape = RoundedCornerShape(4.dp),
    val cornerRadius8: CornerBasedShape = RoundedCornerShape(8.dp),
    val cornerRadius12: CornerBasedShape = RoundedCornerShape(12.dp),
    val cornerRadius16: CornerBasedShape = RoundedCornerShape(16.dp),
)
