package ru.mirea.toir.feature.routes.list.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.Spacer10
import ru.mirea.toir.common.ui.compose.utils.Spacer12
import ru.mirea.toir.common.ui.compose.utils.Spacer4
import ru.mirea.toir.common.ui.compose.utils.Spacer8
import ru.mirea.toir.feature.routes.list.presentation.models.UiRouteAssignment
import ru.mirea.toir.feature.routes.list.presentation.models.UiRouteStatus
import ru.mirea.toir.res.MR

@Composable
internal fun RouteAssignmentCard(
    item: UiRouteAssignment,
    onStartClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors
    val shapes = ToirTheme.shapes

    val cardBackground = if (item.status == UiRouteStatus.COMPLETED) colors.successSubtle else colors.surface
    val cardAlpha = if (item.status == UiRouteStatus.ASSIGNED) 0.7f else 1f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clip(shapes.md)
            .background(cardBackground),
    ) {
        if (item.hasPendingSync) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .matchParentSize()
                    .background(colors.sync),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (item.hasPendingSync) 20.dp else 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
        ) {
            RouteCardHeader(item = item)
            Spacer4()
            RouteCardSubtitle(routeNumber = item.routeNumber)
            Spacer10()
            RouteCardProgress(item = item)
            Spacer4()
            RouteCardProgressText(completedPoints = item.completedPoints, totalPoints = item.totalPoints)
            Spacer10()
            RouteCardMetadata(item = item)
            RouteCardAction(
                status = item.status,
                onStartClick = onStartClick,
                onContinueClick = onContinueClick,
            )
        }
    }
}

@Composable
private fun RouteCardHeader(
    item: UiRouteAssignment,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.routeName,
            style = ToirTheme.typography.headline,
            color = ToirTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Spacer8()
        RouteStatusBadge(status = item.status, hasPendingSync = item.hasPendingSync)
    }
}

@Composable
private fun RouteCardSubtitle(
    routeNumber: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(MR.strings.routes_list_route_number, routeNumber),
        style = ToirTheme.typography.bodyMedium,
        color = ToirTheme.colors.textSecondary,
        modifier = modifier,
    )
}

@Composable
private fun RouteCardProgress(
    item: UiRouteAssignment,
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors
    val progressColor = when (item.status) {
        UiRouteStatus.COMPLETED -> colors.success
        UiRouteStatus.IN_PROGRESS -> colors.warning
        UiRouteStatus.ASSIGNED -> colors.border
    }

    LinearProgressIndicator(
        progress = { item.progress },
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(ToirTheme.shapes.pill),
        color = progressColor,
        trackColor = colors.border,
    )
}

@Composable
private fun RouteCardProgressText(
    completedPoints: Int,
    totalPoints: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(MR.strings.routes_list_progress, completedPoints, totalPoints),
        style = ToirTheme.typography.caption,
        color = ToirTheme.colors.textSecondary,
        modifier = modifier,
    )
}

@Composable
private fun RouteCardMetadata(
    item: UiRouteAssignment,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(MR.images.ic_calendar_today),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = ToirTheme.colors.textSecondary,
        )
        Text(
            text = item.assignedAt.take(10),
            style = ToirTheme.typography.caption,
            color = ToirTheme.colors.textSecondary,
        )
        if (item.hasPendingSync) {
            Text(
                text = " · ",
                style = ToirTheme.typography.caption,
                color = ToirTheme.colors.textSecondary,
            )
            Icon(
                painter = painterResource(MR.images.ic_sync_alt),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = ToirTheme.colors.sync,
            )
        }
    }
}

@Composable
private fun ColumnScope.RouteCardAction(
    status: UiRouteStatus,
    onStartClick: () -> Unit,
    onContinueClick: () -> Unit,
) {
    when (status) {
        UiRouteStatus.ASSIGNED -> {
            Spacer12()
            OutlinedButton(
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(MR.strings.routes_list_button_start),
                    style = ToirTheme.typography.label,
                )
            }
        }

        UiRouteStatus.IN_PROGRESS -> {
            Spacer12()
            OutlinedButton(
                onClick = onContinueClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(MR.strings.routes_list_button_continue),
                    style = ToirTheme.typography.label,
                )
            }
        }

        UiRouteStatus.COMPLETED -> Unit
    }
}

@Composable
private fun RouteStatusBadge(
    status: UiRouteStatus,
    hasPendingSync: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors
    val shapes = ToirTheme.shapes

    val (badgeBackground, textColor, label) = when {
        hasPendingSync -> Triple(
            colors.syncSubtle,
            colors.sync,
            stringResource(MR.strings.routes_list_status_completed),
        )

        status == UiRouteStatus.ASSIGNED -> Triple(
            colors.surface2,
            colors.textSecondary,
            stringResource(status.stringResource)
        )

        status == UiRouteStatus.IN_PROGRESS -> Triple(
            colors.warningSubtle,
            colors.warning,
            stringResource(status.stringResource)
        )

        status == UiRouteStatus.COMPLETED -> Triple(
            colors.successSubtle,
            colors.success,
            stringResource(status.stringResource)
        )

        else -> Triple(colors.surface2, colors.textSecondary, stringResource(status.stringResource))
    }

    Box(
        modifier = modifier
            .clip(shapes.pill)
            .background(badgeBackground)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = ToirTheme.typography.caption,
            color = textColor,
        )
    }
}
