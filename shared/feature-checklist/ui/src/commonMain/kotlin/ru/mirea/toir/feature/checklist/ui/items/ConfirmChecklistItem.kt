package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.components.shared.chip.ToirToggleChip
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.res.MR

@Composable
internal fun ConfirmChecklistItem(
    item: UiChecklistItem.ConfirmItem,
    onConfirmChange: (Boolean) -> Unit,
    onOpenDescription: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    ) {
        ChecklistItemTitle(item = item, onOpenDescription = onOpenDescription)
        ToirToggleChip(
            selected = item.value,
            onSelectedChange = onConfirmChange,
            text = stringResource(MR.strings.checklist_button_done),
            isError = item.showValidationError,
        )
    }
}
