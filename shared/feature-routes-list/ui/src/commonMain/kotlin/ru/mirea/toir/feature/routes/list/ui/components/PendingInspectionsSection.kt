package ru.mirea.toir.feature.routes.list.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import kotlin.time.ExperimentalTime
import ru.mirea.toir.res.MR
import ru.mirea.toir.feature.routes.list.presentation.models.UiPendingInspection
import ru.mirea.toir.feature.routes.list.presentation.models.UiRejectionReason
import ru.mirea.toir.feature.routes.list.presentation.models.UiSyncIndicator

@OptIn(ExperimentalTime::class)
@Composable
internal fun PendingInspectionsSection(
    indicator: UiSyncIndicator,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(MR.strings.sync_status_pending_list_title),
            style = MaterialTheme.typography.titleMedium,
        )
        when {
            indicator.pendingInspections.isEmpty() && !indicator.hasPending ->
                Text(stringResource(MR.strings.sync_status_pending_list_empty))
            indicator.pendingInspections.isEmpty() && indicator.hasPending ->
                Text(stringResource(MR.strings.sync_status_pending_in_background))
            else -> indicator.pendingInspections.forEach { item ->
                PendingInspectionCard(item)
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun PendingInspectionCard(item: UiPendingInspection) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(PaddingValues(12.dp)),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.routeName ?: item.inspectionId,
                style = MaterialTheme.typography.bodyLarge,
            )
            item.completedAt?.let { completedAt ->
                Text(
                    text = completedAt.toString(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            val rejectionReason = item.rejectionReason
            when {
                rejectionReason != null -> {
                    val reasonText = stringResource(rejectionReason.toMessageRes())
                    val prefix = stringResource(MR.strings.sync_status_pending_rejected_prefix)
                    Text(
                        text = "$prefix: $reasonText",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                item.attemptCount > 0 -> Text(
                    text = stringResource(MR.strings.sync_status_pending_attempts, item.attemptCount),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
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
