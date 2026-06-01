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

private val lightColorScheme = ToirColorScheme(
    background = Color(0xFFEDEFF2),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF6F7F9),
    surfacePressed = Color(0xFFE4E7EC),
    border = Color(0xFFC9CED8),
    borderSubtle = Color(0xFFE3E6EC),
    textPrimary = Color(0xFF1A1D22),
    textSecondary = Color(0xFF5A6172),
    textDisabled = Color(0xFF8F95A4),
    textOnAccent = Color(0xFFFFFFFF),
    ctaPrimary = Color(0xFF2A2F3C),
    ctaSecondary = Color(0xFFEAECF1),
    success = Color(0xFF27704D),
    successSubtle = Color(0xFFE2F1EA),
    warning = Color(0xFF8C5A12),
    warningSubtle = Color(0xFFFBF0DC),
    error = Color(0xFFB83232),
    errorSubtle = Color(0xFFFBE6E4),
    sync = Color(0xFF8A5614),
    syncSubtle = Color(0xFFF7ECDA),
    destructive = Color(0xFFB83232),
    focusBorder = Color(0xFF3D4455),
    pressedOverlay = Color(0x0F000000),
    selectedBackground = Color(0x142A2F3C),
)

internal fun getLightColorScheme(): ToirColorScheme = lightColorScheme

internal fun getDarkColorScheme(): ToirColorScheme = darkColorScheme
