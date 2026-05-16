package ru.mirea.toir.feature.routes.list.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.routes.list.presentation.models.UiPendingInspection
import ru.mirea.toir.feature.routes.list.presentation.models.UiRejectionReason
import ru.mirea.toir.feature.routes.list.presentation.models.UiSyncIndicator
import ru.mirea.toir.res.MR

@Composable
internal fun PendingInspectionsSection(
    indicator: UiSyncIndicator,
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors
    if (indicator.pendingInspections.isEmpty()) {
        Text(
            text = stringResource(MR.strings.sync_status_pending_in_background),
            style = ToirTheme.typography.bodyMedium,
            color = colors.textSecondary,
            modifier = modifier,
        )
        return
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(MR.strings.sync_status_pending_list_title),
            style = ToirTheme.typography.bodyLarge,
            color = colors.textPrimary,
        )
        indicator.pendingInspections.forEach { item ->
            PendingInspectionCard(item)
        }
    }
}

@Composable
private fun PendingInspectionCard(item: UiPendingInspection) {
    val colors = ToirTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ToirTheme.shapes.md)
            .background(colors.surface2)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = item.routeName ?: item.inspectionId,
            style = ToirTheme.typography.bodyLarge,
            color = colors.textPrimary,
        )
        item.rejectionReason?.let { reason ->
            val reasonText = stringResource(reason.toMessageRes())
            val prefix = stringResource(MR.strings.sync_status_pending_rejected_prefix)
            Text(
                text = "$prefix: $reasonText",
                color = colors.error,
                style = ToirTheme.typography.caption,
            )
        }
    }
}

private fun UiRejectionReason.toMessageRes() = when (this) {
    UiRejectionReason.INVALID_ASSIGNMENT_ID -> MR.strings.sync_rejection_invalid_assignment_id
    UiRejectionReason.INVALID_ROUTE_ID -> MR.strings.sync_rejection_invalid_route_id
    UiRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN ->
        MR.strings.sync_rejection_route_assignment_not_found

    UiRejectionReason.ROUTE_ID_MISMATCH -> MR.strings.sync_rejection_route_id_mismatch
    UiRejectionReason.INSPECTION_NOT_FOUND -> MR.strings.sync_rejection_inspection_not_found
    UiRejectionReason.ROUTE_POINT_NOT_FOUND -> MR.strings.sync_rejection_route_point_not_found
    UiRejectionReason.EQUIPMENT_MISMATCH -> MR.strings.sync_rejection_equipment_mismatch
    UiRejectionReason.UNKNOWN -> MR.strings.sync_rejection_unknown
}
