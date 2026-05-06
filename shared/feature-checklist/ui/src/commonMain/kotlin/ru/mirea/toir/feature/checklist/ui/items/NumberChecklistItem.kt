package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.components.shared.textfield.ToirOutlinedTextField
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.res.MR

@Composable
internal fun NumberChecklistItem(
    item: UiChecklistItem,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var input by remember(item.id, item.valueNumber) { mutableStateOf(item.valueNumber) }
    val parsed = input.replace(',', '.').toDoubleOrNull()
    val min = item.numericMin?.replace(',', '.')?.toDoubleOrNull()
    val max = item.numericMax?.replace(',', '.')?.toDoubleOrNull()
    val isOutOfRange = parsed != null &&
        ((min != null && parsed < min) || (max != null && parsed > max))
    val isInvalid = isOutOfRange || item.showValidationError
    val rangeHint = rangeHint(item.numericMin, item.numericMax)
    val supportingText = when {
        isOutOfRange -> stringResource(MR.strings.checklist_number_error_out_of_range)
        item.showValidationError -> stringResource(MR.strings.checklist_validation_error_required)
        rangeHint != null -> rangeHint
        else -> null
    }

    ToirOutlinedTextField(
        value = input,
        onValueChange = { newValue ->
            input = newValue
            onValueChange(newValue)
        },
        modifier = modifier.fillMaxWidth(),
        label = item.title,
        isRequired = item.isRequired,
        isError = isInvalid,
        supportingText = supportingText,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
    )
}

@Composable
private fun rangeHint(min: String?, max: String?): String? = when {
    min != null && max != null -> stringResource(MR.strings.checklist_number_hint_range, min, max)
    min != null -> stringResource(MR.strings.checklist_number_hint_min, min)
    max != null -> stringResource(MR.strings.checklist_number_hint_max, max)
    else -> null
}
