package ru.mirea.toir.common.ui.compose.components.shared.badge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

/**
 * Pill-shaped status indicator per MASTER §8.4.
 *
 * Icon is treated as decorative — `text` carries the accessibility label, and the row merges
 * descendants so screen readers announce the badge as a single element.
 *
 * Caller is expected to pair semantic colors from [ToirTheme.colors]:
 * - successSubtle / success
 * - warningSubtle / warning
 * - errorSubtle / error
 * - syncSubtle / sync
 * - surface2 / textSecondary (neutral / not-started)
 */
@Composable
fun StatusBadge(
    text: String,
    icon: Painter,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .wrapContentSize()
            .clip(ToirTheme.shapes.pill)
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            colorFilter = ColorFilter.tint(contentColor),
        )
        Text(
            text = text,
            style = ToirTheme.typography.caption,
            color = contentColor,
        )
    }
}

@Preview
@Composable
private fun PreviewStatusBadgeSuccess() {
    ToirTheme {
        StatusBadge(
            text = "Выполнено",
            icon = painterResource(MR.images.ic_check_circle),
            backgroundColor = ToirTheme.colors.successSubtle,
            contentColor = ToirTheme.colors.success,
        )
    }
}
