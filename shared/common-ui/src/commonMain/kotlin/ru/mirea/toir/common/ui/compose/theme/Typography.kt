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
    val displayLarge: TextStyle,   // 24sp 600 — заголовок экрана
    val displayMedium: TextStyle,  // 20sp 600 — заголовок карточки
    val headline: TextStyle,       // 17sp 600 — подзаголовок секции
    val bodyLarge: TextStyle,      // 16sp 400 — основной текст, поля ввода
    val bodyMedium: TextStyle,     // 14sp 400 — вторичный текст
    val label: TextStyle,          // 13sp 500 — лейблы полей, кнопки
    val caption: TextStyle,        // 12sp 400 — метки, мета
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
