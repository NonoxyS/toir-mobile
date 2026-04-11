package ru.mirea.toir.common.ui.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Suppress("CompositionLocalAllowlist")
internal val LocalToirThemeTypography = staticCompositionLocalOf<ToirTypography> {
    error("CompositionLocal LocalToirThemeTypography was not provided")
}

@Immutable
class ToirTypography internal constructor(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val headline: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
)

@Composable
internal fun defaultTypography(): ToirTypography {
    return ToirTypography(
        displayLarge = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
        ),
        displayMedium = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 27.sp,
        ),
        headline = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = 24.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 21.sp,
        ),
        label = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 17.sp,
        ),
        caption = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),
    )
}
