package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem

/**
 * Заголовок пункта чеклиста с info-иконкой справа (если у пункта есть description)
 * и звёздочкой обязательности отдельным Text красным цветом — сигнал обязательности
 * не зависит от состояния ошибки.
 */
@Composable
internal fun ChecklistItemTitle(
    item: UiChecklistItem,
    onOpenDescription: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.title,
                style = ToirTheme.typography.bodyLarge,
                color = if (item.showValidationError) {
                    ToirTheme.colors.error
                } else {
                    ToirTheme.colors.textPrimary
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (item.isRequired) {
                Text(
                    text = "*",
                    style = ToirTheme.typography.bodyLarge,
                    color = ToirTheme.colors.error,
                )
            }
        }
        if (item.description != null) {
            ChecklistInfoIconButton(onClick = onOpenDescription)
        }
    }
}
