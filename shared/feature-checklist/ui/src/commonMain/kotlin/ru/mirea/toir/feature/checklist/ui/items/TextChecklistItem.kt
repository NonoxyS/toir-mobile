package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem

@Composable
internal fun TextChecklistItem(
    item: UiChecklistItem,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var input by remember(item.id, item.valueText) { mutableStateOf(item.valueText) }

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
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
