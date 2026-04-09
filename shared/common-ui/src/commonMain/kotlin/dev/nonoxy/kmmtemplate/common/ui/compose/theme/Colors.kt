package dev.nonoxy.kmmtemplate.common.ui.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.nonoxy.kmmtemplate.common.ui.compose.utils.asCompose
import dev.nonoxy.kmmtemplate.res.MR

@Suppress("CompositionLocalAllowlist")
internal val LocalKmmTemplateThemeColors = staticCompositionLocalOf<KmmTemplateColorScheme> {
    error("CompositionLocal LocalKmmTemplateThemeColors was not provided")
}

@Immutable
data class KmmTemplateColorScheme(
    val white: Color,
    val black: Color,
)

@Composable
internal fun getLightColorScheme(): KmmTemplateColorScheme {
    return KmmTemplateColorScheme(
        white = MR.colors.white.asCompose(),
        black = MR.colors.black.asCompose(),
    )
}

@Composable
internal fun getDarkColorScheme(): KmmTemplateColorScheme {
    return KmmTemplateColorScheme(
        white = MR.colors.white.asCompose(),
        black = MR.colors.black.asCompose(),
    )
}