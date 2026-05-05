package ru.mirea.toir.feature.equipment.card.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
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
        EquipmentCardField(
            label = stringResource(MR.strings.equipment_card_type),
            value = state.type,
        )
        if (state.locationName.isNotEmpty()) {
            EquipmentCardField(
                label = stringResource(MR.strings.equipment_card_location),
                value = state.locationName,
            )
        }
        EquipmentCardField(
            label = stringResource(MR.strings.equipment_card_status),
            value = stringResource(state.status.labelRes),
        )
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
