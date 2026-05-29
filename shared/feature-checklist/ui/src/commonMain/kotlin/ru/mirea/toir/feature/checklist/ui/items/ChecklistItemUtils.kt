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

internal fun UiChecklistItem.titleWithRequiredMarker(): String =
    if (isRequired) "$title *" else title

/**
 * Заголовок пункта чеклиста с опциональной info-иконкой справа. Иконка появляется
 * только если у пункта есть description — тогда тап на ней открывает bottom sheet
 * с полным текстом описания. По умолчанию description не отображается прямо в
 * списке, чтобы не перегружать его шумом для опытных обходчиков.
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
        Text(
            text = item.titleWithRequiredMarker(),
            style = ToirTheme.typography.bodyLarge,
            color = if (item.showValidationError) {
                ToirTheme.colors.error
            } else {
                ToirTheme.colors.textPrimary
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (item.description != null) {
            ChecklistInfoIconButton(onClick = onOpenDescription)
        }
    }
}
