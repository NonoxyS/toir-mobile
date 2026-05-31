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
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.components.shared.textfield.ToirOutlinedTextField
import ru.mirea.toir.common.ui.compose.components.shared.textfield.filters.NumberInputFilter
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.res.MR

@Composable
internal fun NumberChecklistItem(
    item: UiChecklistItem.NumberItem,
    onValueChange: (String) -> Unit,
    onOpenDescription: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Локальный input — источник истины для TextField, чтобы отрисовка не дожидалась
    // round-trip executor → store → mapper. Подсветка ошибки (item.isInvalidNumber,
    // item.isOutOfRange) приходит из store: парсинг и решение «валидно / OOR» делает
    // executor, UI только рендерит. Ключ remember только по item.id — иначе эхо
    // переформатированного значения сбрасывало бы буфер во время быстрого ввода.
    var input by remember(item.id) { mutableStateOf(item.value) }
    val supportingText = when {
        item.isInvalidNumber -> stringResource(MR.strings.checklist_number_error_invalid)
        item.isOutOfRange -> stringResource(MR.strings.checklist_number_error_out_of_range)
        item.showValidationError -> stringResource(MR.strings.checklist_validation_error_required)
        else -> item.rangeHint?.localized()
    }
    val isError = item.isInvalidNumber || item.isOutOfRange || item.showValidationError

    ToirOutlinedTextField(
        value = input,
        onValueChange = { newValue ->
            input = newValue
            onValueChange(newValue)
        },
        modifier = modifier.fillMaxWidth(),
        label = item.title,
        isRequired = item.isRequired,
        isError = isError,
        supportingText = supportingText,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        inputFilter = NumberInputFilter,
        singleLine = true,
        labelTrailingContent = if (item.description != null) {
            { ChecklistInfoIconButton(onClick = onOpenDescription) }
        } else {
            null
        },
    )
}
