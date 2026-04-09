package ru.mirea.toir.common.ui.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp

@Suppress("CompositionLocalAllowlist")
internal val LocalToirThemeTypography = staticCompositionLocalOf<ToirTypography> {
    error("CompositionLocal LocalToirThemeTypography was not provided")
}

@Immutable
class ToirTypography internal constructor(
    val captionMD: TextStyle,
    val textMD: TextStyle,
    val bodyLG: TextStyle,
    val headlineMD: TextStyle,
)

@Composable
internal fun defaultTypography(): ToirTypography {
    return ToirTypography(
        captionMD = TextStyle(
            fontFamily = fontRoboto,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            letterSpacing = TextUnit(0F, TextUnitType.Sp),
        ),
        textMD = TextStyle(
            fontFamily = fontRoboto,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = TextUnit(0F, TextUnitType.Sp),
        ),
        bodyLG = TextStyle(
            fontFamily = fontRoboto,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = TextUnit(0.5F, TextUnitType.Sp),
        ),
        headlineMD = TextStyle(
            fontFamily = fontRoboto,
            fontWeight = FontWeight.Medium,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = TextUnit(0F, TextUnitType.Sp),
        ),
    )
}
