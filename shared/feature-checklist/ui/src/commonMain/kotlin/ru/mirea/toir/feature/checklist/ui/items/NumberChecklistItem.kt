package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.ResourceFormattedStringDesc
import ru.mirea.toir.common.ui.compose.components.shared.textfield.ToirOutlinedTextField
import ru.mirea.toir.common.ui.compose.components.shared.textfield.filters.NumberInputFilter
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.res.MR

@Composable
internal fun NumberChecklistItem(
    item: UiChecklistItem.NumberItem,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportingText = when {
        item.isOutOfRange -> stringResource(MR.strings.checklist_number_error_out_of_range)
        item.showValidationError -> stringResource(MR.strings.checklist_validation_error_required)
        else -> when (val hint = item.rangeHint) {
            is ResourceFormattedStringDesc -> stringResource(hint.stringRes, *hint.args.toTypedArray())
            else -> null
        }
    }
    val isError = item.isOutOfRange || item.showValidationError

    ToirOutlinedTextField(
        value = item.value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = item.title,
        isRequired = item.isRequired,
        isError = isError,
        supportingText = supportingText,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        inputFilter = NumberInputFilter,
        singleLine = true,
    )
}

