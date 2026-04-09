package dev.nonoxy.kmmtemplate.common.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Suppress("ModifierMissing")
@Composable
fun KmmTemplateTheme(
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when {
        isDark -> getDarkColorScheme()
        else -> getLightColorScheme()
    }

    MaterialTheme {
        CompositionLocalProvider(
            LocalKmmTemplateThemeColors provides colors,
            LocalKmmTemplateThemeTypography provides defaultTypography(),
            LocalKmmTemplateThemeShapes provides KmmTemplateShapes(),
        ) {
            ProvideTextStyle(
                value = KmmTemplateTheme.typography.textMD.copy(color = KmmTemplateTheme.colors.black),
                content = content
            )
        }
    }
}

object KmmTemplateTheme {

    val colors: KmmTemplateColorScheme
        @Composable @ReadOnlyComposable
        get() = LocalKmmTemplateThemeColors.current

    val typography: KmmTemplateTypography
        @Composable @ReadOnlyComposable
        get() = LocalKmmTemplateThemeTypography.current

    val shapes: KmmTemplateShapes
        @Composable @ReadOnlyComposable
        get() = LocalKmmTemplateThemeShapes.current
}
