package dev.nonoxy.kmmtemplate.common.ui.compose.components.shared.space

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Space(
    modifier: Modifier = Modifier,
    height: Dp = 0.dp,
    width: Dp = 0.dp
) {
    Spacer(
        modifier = modifier
            .height(height)
            .width(width)
    )
}

@Composable
fun ColumnScope.Space(
    height: Dp,
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier.height(height = height)
    )
}

@Composable
fun RowScope.Space(
    width: Dp,
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier.width(width = width)
    )
}
