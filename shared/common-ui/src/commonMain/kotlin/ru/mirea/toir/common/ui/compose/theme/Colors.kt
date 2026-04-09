package ru.mirea.toir.common.ui.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import ru.mirea.toir.common.ui.compose.utils.asCompose
import ru.mirea.toir.res.MR

@Suppress("CompositionLocalAllowlist")
internal val LocalToirThemeColors = staticCompositionLocalOf<ToirColorScheme> {
    error("CompositionLocal LocalToirThemeColors was not provided")
}

@Immutable
data class ToirColorScheme(
    val white: Color,
    val black: Color,
)

@Composable
internal fun getLightColorScheme(): ToirColorScheme {
    return ToirColorScheme(
        white = MR.colors.white.asCompose(),
        black = MR.colors.black.asCompose(),
    )
}

@Composable
internal fun getDarkColorScheme(): ToirColorScheme {
    return ToirColorScheme(
        white = MR.colors.white.asCompose(),
        black = MR.colors.black.asCompose(),
    )
}