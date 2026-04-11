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
    val xs: CornerBasedShape = RoundedCornerShape(4.dp),
    val sm: CornerBasedShape = RoundedCornerShape(6.dp),
    val md: CornerBasedShape = RoundedCornerShape(10.dp),
    val lg: CornerBasedShape = RoundedCornerShape(14.dp),
    val pill: CornerBasedShape = RoundedCornerShape(999.dp),
)
