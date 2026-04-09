package dev.nonoxy.kmmtemplate.common.ui.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dev.icerock.moko.resources.compose.asFont
import dev.nonoxy.kmmtemplate.res.MR

val fontRoboto: FontFamily
    @Composable get() = FontFamily(
        MR.fonts.roboto_regular.asFont(weight = FontWeight.Normal).requireNotNull(),
        MR.fonts.roboto_medium.asFont(weight = FontWeight.Medium).requireNotNull(),
        MR.fonts.roboto_thin.asFont(weight = FontWeight.Thin).requireNotNull(),
        MR.fonts.roboto_bold.asFont(weight = FontWeight.Bold).requireNotNull(),
    )

private fun Font?.requireNotNull(): Font = requireNotNull(this)
