package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.persistentListOf
import ru.mirea.toir.common.ui.compose.components.shared.segmented.ToirSegmentedControl
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.res.MR

@Composable
internal fun BooleanChecklistItem(
    item: UiChecklistItem.BooleanItem,
    onValueChange: (kotlin.Boolean?) -> Unit,
    onOpenDescription: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = remember { persistentListOf(true, false) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    ) {
        ChecklistItemTitle(item = item, onOpenDescription = onOpenDescription)
        ToirSegmentedControl(
            options = options,
            selected = item.value,
            onSelectedChange = onValueChange,
            isError = item.showValidationError,
            optionLabel = { value ->
                stringResource(
                    if (value) MR.strings.checklist_button_yes else MR.strings.checklist_button_no,
                )
            },
        )
    }
}
