package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
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
    val rangeHint = rangeHint(item.numericMin, item.numericMax)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    ) {
        Text(
            text = item.titleWithRequiredMarker(),
            style = ToirTheme.typography.bodyLarge,
            color = ToirTheme.colors.textPrimary,
        )
        OutlinedTextField(
            value = input,
            onValueChange = { newValue ->
                input = newValue
                onValueChange(newValue)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = isOutOfRange,
            supportingText = {
                when {
                    isOutOfRange -> Text(
                        text = stringResource(MR.strings.checklist_number_error_out_of_range),
                        style = ToirTheme.typography.caption,
                        color = ToirTheme.colors.error,
                    )
                    rangeHint != null -> Text(
                        text = rangeHint,
                        style = ToirTheme.typography.caption,
                        color = ToirTheme.colors.textSecondary,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun rangeHint(min: String?, max: String?): String? = when {
    min != null && max != null -> stringResource(MR.strings.checklist_number_hint_range, min, max)
    min != null -> stringResource(MR.strings.checklist_number_hint_min, min)
    max != null -> stringResource(MR.strings.checklist_number_hint_max, max)
    else -> null
}
