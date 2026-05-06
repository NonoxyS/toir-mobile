package ru.mirea.toir.feature.equipment.card.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.components.shared.button.ToirPrimaryButton
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@Composable
internal fun EquipmentCardOpenChecklistButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ToirTheme.colors.background)
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        ToirPrimaryButton(
            onClick = onClick,
            text = stringResource(MR.strings.equipment_card_button_open_checklist),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
