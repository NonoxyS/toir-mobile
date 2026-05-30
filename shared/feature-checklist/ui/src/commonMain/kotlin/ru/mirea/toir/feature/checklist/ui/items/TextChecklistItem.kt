package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.components.shared.textfield.ToirOutlinedTextField
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.res.MR

@Composable
internal fun TextChecklistItem(
    item: UiChecklistItem.TextItem,
    onValueChange: (String) -> Unit,
    onOpenDescription: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var input by remember(item.id) { mutableStateOf(item.value) }

    ToirOutlinedTextField(
        value = input,
        onValueChange = { newValue ->
            input = newValue
            onValueChange(newValue)
        },
        modifier = modifier.fillMaxWidth(),
        label = item.title,
        isRequired = item.isRequired,
        isError = item.showValidationError,
        supportingText = if (item.showValidationError) {
            stringResource(MR.strings.checklist_validation_error_required)
        } else {
            null
        },
        singleLine = false,
        minLines = 1,
        maxLines = 5,
        labelTrailingContent = if (item.description != null) {
            { ChecklistInfoIconButton(onClick = onOpenDescription) }
        } else {
            null
        },
    )
}
