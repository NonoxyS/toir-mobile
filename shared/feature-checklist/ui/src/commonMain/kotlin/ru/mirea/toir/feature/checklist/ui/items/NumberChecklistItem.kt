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
    // Локальный input — источник истины для TextField. Без него ломались два сценария:
    // 1) ввод дробных чисел — executor сохраняет "1." как 1.0, mapper форматирует
    //    обратно в "1", из-за чего точка тут же исчезала и нельзя было дописать "1.5";
    // 2) очистка поля — executor возвращается ранним return на пустой строке,
    //    item.value остаётся прежним → TextField перерисовывал старое число,
    //    то есть последний символ "не удалялся" при удалении ввода.
    // Ключ только по item.id, чтобы эхо из store не сбрасывало буфер во время ввода.
    var input by remember(item.id) { mutableStateOf(item.value) }
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
    )
}

