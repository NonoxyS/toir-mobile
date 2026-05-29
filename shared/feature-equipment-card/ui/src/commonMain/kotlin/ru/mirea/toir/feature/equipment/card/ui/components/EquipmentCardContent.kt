package ru.mirea.toir.feature.equipment.card.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.components.shared.badge.StatusBadge
import ru.mirea.toir.common.ui.compose.theme.ToirColorScheme
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentCardState
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentResultStatus
import ru.mirea.toir.res.MR

@Composable
internal fun EquipmentCardContent(
    state: UiEquipmentCardState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EquipmentCardField(
            label = stringResource(MR.strings.equipment_card_code),
            value = state.code,
        )
        EquipmentCardField(
            label = stringResource(MR.strings.equipment_card_name),
            value = state.name,
        )
        if (state.locationName.isNotEmpty()) {
            EquipmentCardField(
                label = stringResource(MR.strings.equipment_card_location),
                value = state.locationName,
            )
        }
        EquipmentStatusField(status = state.status)
    }
}

@Composable
private fun EquipmentCardField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = ToirTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        Text(
            text = value.ifEmpty { "—" },
            style = ToirTheme.typography.bodyLarge,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun EquipmentStatusField(status: UiEquipmentResultStatus) {
    val colors = ToirTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(MR.strings.equipment_card_status),
            style = ToirTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        StatusBadge(
            text = stringResource(status.labelRes),
            icon = painterResource(statusIconFor(status)),
            backgroundColor = statusBackgroundFor(status, colors),
            contentColor = statusContentFor(status, colors),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

private fun statusIconFor(status: UiEquipmentResultStatus): ImageResource = when (status) {
    UiEquipmentResultStatus.NOT_STARTED -> MR.images.ic_clipboard
    UiEquipmentResultStatus.IN_PROGRESS -> MR.images.ic_sync_alt
    UiEquipmentResultStatus.COMPLETED -> MR.images.ic_check_circle
    UiEquipmentResultStatus.SKIPPED -> MR.images.ic_close
}

private fun statusBackgroundFor(
    status: UiEquipmentResultStatus,
    colors: ToirColorScheme,
): Color = when (status) {
    UiEquipmentResultStatus.NOT_STARTED -> colors.surface2
    UiEquipmentResultStatus.IN_PROGRESS -> colors.warningSubtle
    UiEquipmentResultStatus.COMPLETED -> colors.successSubtle
    UiEquipmentResultStatus.SKIPPED -> colors.errorSubtle
}

private fun statusContentFor(
    status: UiEquipmentResultStatus,
    colors: ToirColorScheme,
): Color = when (status) {
    UiEquipmentResultStatus.NOT_STARTED -> colors.textSecondary
    UiEquipmentResultStatus.IN_PROGRESS -> colors.warning
    UiEquipmentResultStatus.COMPLETED -> colors.success
    UiEquipmentResultStatus.SKIPPED -> colors.error
}

@Preview
@Composable
private fun Preview() {
    ToirTheme {
        EquipmentCardContent(
            state = UiEquipmentCardState(
                code = "EQ-001",
                name = "Насос циркуляционный",
                status = UiEquipmentResultStatus.IN_PROGRESS,
            )
        )
    }
}
