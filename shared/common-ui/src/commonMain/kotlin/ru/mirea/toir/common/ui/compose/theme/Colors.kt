package ru.mirea.toir.common.ui.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Suppress("CompositionLocalAllowlist")
internal val LocalToirThemeColors = staticCompositionLocalOf<ToirColorScheme> {
    error("CompositionLocal LocalToirThemeColors was not provided")
}

@Immutable
data class ToirColorScheme(
    // Background
    val background: Color,
    val surface: Color,
    val surface2: Color,
    val surfacePressed: Color,
    // Borders
    val border: Color,
    val borderSubtle: Color,
    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val textOnAccent: Color,
    // CTA
    val ctaPrimary: Color,
    val ctaSecondary: Color,
    // Semantic
    val success: Color,
    val successSubtle: Color,
    val warning: Color,
    val warningSubtle: Color,
    val error: Color,
    val errorSubtle: Color,
    val sync: Color,
    val syncSubtle: Color,
    val destructive: Color,
    // States
    val focusBorder: Color,
    val pressedOverlay: Color,
    val selectedBackground: Color,
)

private val darkColorScheme = ToirColorScheme(
    background = Color(0xFF1A1D22),
    surface = Color(0xFF242830),
    surface2 = Color(0xFF2D3240),
    surfacePressed = Color(0xFF313744),
    border = Color(0xFF3D4455),
    borderSubtle = Color(0xFF2A2F3C),
    textPrimary = Color(0xFFE8EAF0),
    textSecondary = Color(0xFF9499A8),
    textDisabled = Color(0xFF55596A),
    textOnAccent = Color(0xFF1A1D22),
    ctaPrimary = Color(0xFFD8DBE6),
    ctaSecondary = Color(0xFF2D3240),
    success = Color(0xFF3D9E72),
    successSubtle = Color(0xFF1F3D2D),
    warning = Color(0xFFC4872A),
    warningSubtle = Color(0xFF3A2B10),
    error = Color(0xFFB84040),
    errorSubtle = Color(0xFF3A1A1A),
    sync = Color(0xFFB07830),
    syncSubtle = Color(0xFF362410),
    destructive = Color(0xFFB84040),
    focusBorder = Color(0xFF8A90A0),
    pressedOverlay = Color(0x0FFFFFFF),
    selectedBackground = Color(0x1AD8DBE6),
)

// Light theme is a stub — only dark theme is currently designed per DS MASTER.md
internal fun getLightColorScheme(): ToirColorScheme = darkColorScheme

internal fun getDarkColorScheme(): ToirColorScheme = darkColorScheme
