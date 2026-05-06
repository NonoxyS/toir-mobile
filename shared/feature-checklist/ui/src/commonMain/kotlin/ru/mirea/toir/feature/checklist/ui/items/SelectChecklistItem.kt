package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.checklist.presentation.models.UiAnswerType
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem

@Composable
internal fun SelectChecklistItem(
    item: UiChecklistItem,
    onSelectOption: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectType = item.answerType as? UiAnswerType.Select ?: return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    ) {
        Text(
            text = item.titleWithRequiredMarker(),
            style = ToirTheme.typography.bodyLarge,
            color = ToirTheme.colors.textPrimary,
        )
        selectType.options.forEach { option ->
            val selected = item.valueSelect == option
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = selected, onClick = { onSelectOption(option) }),
            ) {
                RadioButton(
                    selected = selected,
                    onClick = { onSelectOption(option) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = ToirTheme.colors.success,
                        unselectedColor = ToirTheme.colors.border,
                        disabledSelectedColor = ToirTheme.colors.success.copy(alpha = 0.5f),
                        disabledUnselectedColor = ToirTheme.colors.border.copy(alpha = 0.5f),
                    ),
                )
                Text(
                    text = option,
                    style = ToirTheme.typography.bodyMedium,
                    color = ToirTheme.colors.textPrimary,
                )
            }
        }
    }
}
