package ru.mirea.toir.feature.routes.list.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.components.shared.button.ToirSecondaryButton
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
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors
    val shapes = ToirTheme.shapes

    val cardBackground = if (item.status == UiRouteStatus.COMPLETED) colors.successSubtle else colors.surface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shapes.md)
            .background(cardBackground),
    ) {
        if (item.hasPendingSync) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(colors.sync),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            RouteCardHeader(item = item)
            Spacer4()
            RouteCardSubtitle(routeNumber = item.routeNumber)
            if (item.status == UiRouteStatus.SYNC_REQUIRED) {
                Spacer10()
                RouteCardSyncRequiredHint()
            } else {
                Spacer10()
                RouteCardProgress(item = item)
                Spacer4()
                RouteCardProgressText(
                    completedPoints = item.completedPoints,
                    totalPoints = item.totalPoints,
                )
                Spacer10()
                RouteCardMetadata(item = item)
            }
            RouteCardAction(
                status = item.status,
                onStartClick = onStartClick,
                onContinueClick = onContinueClick,
                onSyncClick = onSyncClick,
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
        UiRouteStatus.PARTIALLY_COMPLETED -> colors.warning
        UiRouteStatus.IN_PROGRESS -> colors.warning
        UiRouteStatus.ASSIGNED -> colors.border
        UiRouteStatus.CANCELLED -> colors.border
        UiRouteStatus.SYNC_REQUIRED -> colors.sync
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
    onSyncClick: () -> Unit,
) {
    when (status) {
        UiRouteStatus.ASSIGNED -> {
            Spacer12()
            ToirSecondaryButton(
                onClick = onStartClick,
                text = stringResource(MR.strings.routes_list_button_start),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        UiRouteStatus.IN_PROGRESS,
        UiRouteStatus.PARTIALLY_COMPLETED -> {
            Spacer12()
            ToirSecondaryButton(
                onClick = onContinueClick,
                text = stringResource(MR.strings.routes_list_button_continue),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        UiRouteStatus.SYNC_REQUIRED -> {
            Spacer12()
            ToirSecondaryButton(
                onClick = onSyncClick,
                text = stringResource(MR.strings.routes_list_button_sync),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        UiRouteStatus.COMPLETED,
        UiRouteStatus.CANCELLED -> Unit
    }
}

@Composable
private fun RouteCardSyncRequiredHint(modifier: Modifier = Modifier) {
    val colors = ToirTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ToirTheme.shapes.sm)
            .background(colors.syncSubtle)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(MR.images.ic_sync_alt),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = colors.sync,
        )
        Text(
            text = stringResource(MR.strings.routes_list_sync_required_hint),
            style = ToirTheme.typography.caption,
            color = colors.sync,
        )
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

        status == UiRouteStatus.SYNC_REQUIRED -> Triple(
            colors.syncSubtle,
            colors.sync,
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
