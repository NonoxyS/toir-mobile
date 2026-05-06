package ru.mirea.toir.common.ui.compose.components.shared.segmented

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.theme.ToirTheme

/**
 * Tristate segmented control. Click on already-selected option → onSelectedChange(null).
 *
 * Uses ToirTheme tokens via SegmentedButtonDefaults.colors(...). Border turns red when isError.
 * The [optionLabel] function maps each option T to its display String.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ToirSegmentedControl(
    options: ImmutableList<T>,
    selected: T?,
    onSelectedChange: (T?) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    optionLabel: @Composable (T) -> String,
) {
    val borderColor = if (isError) ToirTheme.colors.error else ToirTheme.colors.border
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            val isSelected = selected == option
            SegmentedButton(
                selected = isSelected,
                onClick = {
                    onSelectedChange(if (isSelected) null else option)
                },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                enabled = enabled,
                border = SegmentedButtonDefaults.borderStroke(borderColor),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = ToirTheme.colors.surface2,
                    activeContentColor = ToirTheme.colors.textPrimary,
                    activeBorderColor = borderColor,
                    inactiveContainerColor = Color.Transparent,
                    inactiveContentColor = ToirTheme.colors.textSecondary,
                    inactiveBorderColor = borderColor,
                    disabledActiveContainerColor = ToirTheme.colors.surface,
                    disabledActiveContentColor = ToirTheme.colors.textDisabled,
                    disabledInactiveContainerColor = Color.Transparent,
                    disabledInactiveContentColor = ToirTheme.colors.textDisabled,
                ),
                icon = {},
            ) {
                Text(
                    text = optionLabel(option),
                    style = ToirTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewToirSegmentedControlNoneSelected() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSegmentedControl(
                options = persistentListOf(true, false),
                selected = null,
                onSelectedChange = {},
                optionLabel = { value -> if (value) "Да" else "Нет" },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirSegmentedControlYesSelected() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSegmentedControl(
                options = persistentListOf(true, false),
                selected = true,
                onSelectedChange = {},
                optionLabel = { value -> if (value) "Да" else "Нет" },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirSegmentedControlError() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSegmentedControl(
                options = persistentListOf(true, false),
                selected = null,
                onSelectedChange = {},
                optionLabel = { value -> if (value) "Да" else "Нет" },
                isError = true,
            )
        }
    }
}
