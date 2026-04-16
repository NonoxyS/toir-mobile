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
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.Spacer4
import ru.mirea.toir.common.ui.compose.utils.Spacer8
import ru.mirea.toir.feature.route.points.presentation.models.UiEquipmentResultStatus
import ru.mirea.toir.feature.route.points.presentation.models.UiRoutePoint

@Composable
internal fun RoutePointCard(
    item: UiRoutePoint,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors
    val shapes = ToirTheme.shapes

    val accentColor = when (item.status) {
        UiEquipmentResultStatus.NOT_STARTED -> colors.textDisabled
        UiEquipmentResultStatus.IN_PROGRESS -> colors.warning
        UiEquipmentResultStatus.COMPLETED -> colors.success
        UiEquipmentResultStatus.SKIPPED -> colors.error
    }
    val badgeBackground = when (item.status) {
        UiEquipmentResultStatus.NOT_STARTED -> colors.surface2
        UiEquipmentResultStatus.IN_PROGRESS -> colors.warningSubtle
        UiEquipmentResultStatus.COMPLETED -> colors.successSubtle
        UiEquipmentResultStatus.SKIPPED -> colors.errorSubtle
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
                label = stringResource(item.status.labelRes),
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

@Preview
@Composable
private fun PreviewRoutePointCardCompleted() {
    ToirTheme {
        RoutePointCard(
            item = UiRoutePoint(
                routePointId = "1",
                equipmentCode = "EQ-001",
                equipmentName = "Насос циркуляционный",
                locationName = "Котельная",
                status = UiEquipmentResultStatus.COMPLETED,
                hasIssues = false,
                equipmentResultId = "res-1",
            ),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewRoutePointCardInProgress() {
    ToirTheme {
        RoutePointCard(
            item = UiRoutePoint(
                routePointId = "2",
                equipmentCode = "EQ-002",
                equipmentName = "Вентилятор приточный",
                locationName = "Машинное отделение",
                status = UiEquipmentResultStatus.IN_PROGRESS,
                hasIssues = false,
                equipmentResultId = "res-2",
            ),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewRoutePointCardSkipped() {
    ToirTheme {
        RoutePointCard(
            item = UiRoutePoint(
                routePointId = "3",
                equipmentCode = "EQ-003",
                equipmentName = "Трансформатор ТМ-100",
                locationName = "",
                status = UiEquipmentResultStatus.SKIPPED,
                hasIssues = true,
                equipmentResultId = "res-3",
            ),
            onClick = {},
        )
    }
}
