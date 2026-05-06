package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.persistentListOf
import ru.mirea.toir.common.ui.compose.components.shared.segmented.ToirSegmentedControl
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.res.MR

@Composable
internal fun BooleanChecklistItem(
    item: UiChecklistItem,
    onValueChange: (Boolean?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = remember { persistentListOf(true, false) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    ) {
        Text(
            text = item.titleWithRequiredMarker(),
            style = ToirTheme.typography.bodyLarge,
            color = if (item.showValidationError) ToirTheme.colors.error else ToirTheme.colors.textPrimary,
        )
        ToirSegmentedControl(
            options = options,
            selected = item.valueBoolean,
            onSelectedChange = onValueChange,
            isError = item.showValidationError,
            optionLabel = { value ->
                stringResource(
                    if (value) MR.strings.checklist_button_yes else MR.strings.checklist_button_no,
                )
            },
        )
    }
}
