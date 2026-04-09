package dev.nonoxy.kmmtemplate.common.ui.compose.components.shared.text

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.TextAutoSizeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    color: Color? = null,
    maxFontSize: TextUnit = style.fontSize,
    minFontSize: TextUnit = TextAutoSizeDefaults.MinFontSize,
    stepSize: TextUnit = 0.25.sp
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style,
        onTextLayout = onTextLayout,
        softWrap = softWrap,
        overflow = overflow,
        color = color?.let { { color } },
        maxLines = maxLines,
        minLines = minLines,
        autoSize = TextAutoSize.StepBased(
            maxFontSize = maxFontSize,
            minFontSize = minFontSize,
            stepSize = stepSize
        )
    )
}
