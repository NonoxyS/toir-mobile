package ru.mirea.toir.common.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Suppress("ModifierMissing")
@Composable
fun ToirTheme(
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when {
        isDark -> getDarkColorScheme()
        else -> getLightColorScheme()
    }

    MaterialTheme {
        CompositionLocalProvider(
            LocalToirThemeColors provides colors,
            LocalToirThemeTypography provides defaultTypography(),
            LocalToirThemeShapes provides ToirShapes(),
        ) {
            ProvideTextStyle(
                value = ToirTheme.typography.bodyMedium.copy(color = ToirTheme.colors.textPrimary),
                content = content
            )
        }
    }
}

object ToirTheme {

    val colors: ToirColorScheme
        @Composable @ReadOnlyComposable
        get() = LocalToirThemeColors.current

    val typography: ToirTypography
        @Composable @ReadOnlyComposable
        get() = LocalToirThemeTypography.current

    val shapes: ToirShapes
        @Composable @ReadOnlyComposable
        get() = LocalToirThemeShapes.current
}
