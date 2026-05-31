package ru.mirea.toir.feature.routes.list.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.mirea.toir.common.ui.compose.components.shared.button.ToirPrimaryButton
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.Spacer12
import ru.mirea.toir.common.ui.compose.utils.Spacer16
import ru.mirea.toir.common.ui.compose.utils.Spacer4
import ru.mirea.toir.feature.routes.list.presentation.models.UiSyncFailure
import ru.mirea.toir.feature.routes.list.presentation.models.UiSyncIndicator
import ru.mirea.toir.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SyncStatusBottomSheet(
    indicator: UiSyncIndicator,
    lastSuccessAt: String?,
    lastFailedAt: String?,
    onSyncNow: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ToirTheme.colors.surface,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(MR.strings.sync_status_title),
                style = ToirTheme.typography.displayMedium,
                color = ToirTheme.colors.textPrimary,
            )
            Spacer16()
            LastSyncCard(indicator = indicator, lastSuccessAt = lastSuccessAt, lastFailedAt = lastFailedAt)
            if (indicator.hasPending || indicator.pendingInspections.isNotEmpty()) {
                Spacer12()
                PendingInspectionsSection(indicator = indicator)
            }
            Spacer16()
            ToirPrimaryButton(
                onClick = onSyncNow,
                text = stringResource(MR.strings.sync_action_manual),
                enabled = !indicator.isRunning,
                isLoading = indicator.isRunning,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer16()
        }
    }
}

@Composable
private fun LastSyncCard(
    indicator: UiSyncIndicator,
    lastSuccessAt: String?,
    lastFailedAt: String?,
) {
    val colors = ToirTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ToirTheme.shapes.md)
            .background(colors.surface2)
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(MR.strings.sync_status_last_label),
            style = ToirTheme.typography.caption,
            color = colors.textSecondary,
        )
        Spacer4()
        val hasError = indicator.lastError != null
        val timeText = when {
            hasError -> lastFailedAt?.let(::formatInstant)
            else -> lastSuccessAt?.let(::formatInstant)
        }
        if (timeText == null && !indicator.isRunning) {
            Text(
                text = stringResource(MR.strings.sync_status_never),
                style = ToirTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(
                        if (hasError) MR.images.ic_error_outline else MR.images.ic_check_circle
                    ),
                    contentDescription = null,
                    tint = if (hasError) colors.error else colors.success,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = when {
                        indicator.isRunning -> stringResource(MR.strings.sync_status_running)
                        hasError -> stringResource(
                            MR.strings.sync_status_last_failed_full,
                            timeText.orEmpty(),
                            stringResource(indicator.lastError!!.toMessageRes()),
                        )

                        else -> stringResource(
                            MR.strings.sync_status_last_success_full,
                            timeText.orEmpty(),
                        )
                    },
                    style = ToirTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

private fun UiSyncFailure.toMessageRes() = when (this) {
    UiSyncFailure.NETWORK -> MR.strings.sync_error_network
    UiSyncFailure.AUTH -> MR.strings.sync_error_auth
    UiSyncFailure.SERVER -> MR.strings.sync_error_server
    UiSyncFailure.UNKNOWN -> MR.strings.sync_error_unknown
}

/** Конвертирует ISO-Instant (UTC) в локальное `HH:MM` по таймзоне устройства. */
private fun formatInstant(iso: String): String = runCatching {
    val local = Instant.parse(iso).toLocalDateTime(TimeZone.currentSystemDefault())
    val hh = local.hour.toString().padStart(2, '0')
    val mm = local.minute.toString().padStart(2, '0')
    "$hh:$mm"
}.getOrElse { iso }
