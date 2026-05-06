package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.components.shared.button.ToirPrimaryButton
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.res.MR

@Composable
internal fun ConfirmChecklistItem(
    item: UiChecklistItem,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.titleWithRequiredMarker(),
            style = ToirTheme.typography.bodyLarge,
            color = ToirTheme.colors.textPrimary,
            modifier = Modifier.weight(weight = 1f),
        )
        Spacer(modifier = Modifier.width(width = 8.dp))
        ToirPrimaryButton(
            onClick = onConfirm,
            text = stringResource(MR.strings.checklist_button_confirm),
            enabled = !item.isConfirmed,
        )
    }
}
