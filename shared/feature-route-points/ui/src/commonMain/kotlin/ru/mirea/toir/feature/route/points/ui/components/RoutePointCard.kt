package ru.mirea.toir.feature.route.points.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.Spacer4
import ru.mirea.toir.common.ui.compose.utils.Spacer8
import ru.mirea.toir.feature.route.points.presentation.models.UiRoutePoint

@Composable
internal fun RoutePointCard(
    item: UiRoutePoint,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors
    val shapes = ToirTheme.shapes

    val accentColor = when (item.statusColor) {
        "success" -> colors.success
        "warning" -> colors.warning
        "error" -> colors.error
        else -> colors.textDisabled
    }
    val badgeBackground = when (item.statusColor) {
        "success" -> colors.successSubtle
        "warning" -> colors.warningSubtle
        "error" -> colors.errorSubtle
        else -> colors.surface2
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shapes.md)
            .background(colors.surface)
            .clickable(onClick = onClick),
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .matchParentSize()
                .background(accentColor),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 14.dp, end = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.equipmentName,
                    style = ToirTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                )
                Spacer4()
                Text(
                    text = buildString {
                        append(item.equipmentCode)
                        if (item.locationName.isNotEmpty()) append(" · ${item.locationName}")
                    },
                    style = ToirTheme.typography.caption,
                    color = colors.textSecondary,
                )
            }
            Spacer8()
            StatusBadge(
                label = item.statusLabel,
                background = badgeBackground,
                textColor = accentColor,
            )
        }
    }
}

@Composable
private fun StatusBadge(
    label: String,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(ToirTheme.shapes.pill)
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = ToirTheme.typography.caption,
            color = textColor,
        )
    }
}
