package ru.mirea.toir.common.ui.compose.components.shared.toggle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.theme.ToirTheme

/**
 * Switch wrapper that injects [ToirTheme] tokens into Material3's [Switch]
 * per MASTER §8.12 (Boolean checklist item).
 *
 * The wrapper renders only the switch itself — the optional label that appears
 * to its left in MASTER §8.12 is the responsibility of the calling row layout.
 */
@Composable
fun ToirSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = ToirTheme.colors.textPrimary,
            checkedTrackColor = ToirTheme.colors.success,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = ToirTheme.colors.textSecondary,
            uncheckedTrackColor = ToirTheme.colors.surface2,
            uncheckedBorderColor = ToirTheme.colors.border,
            disabledCheckedThumbColor = ToirTheme.colors.textPrimary.copy(alpha = 0.5f),
            disabledCheckedTrackColor = ToirTheme.colors.surface,
            disabledCheckedBorderColor = Color.Transparent,
            disabledUncheckedThumbColor = ToirTheme.colors.textSecondary.copy(alpha = 0.5f),
            disabledUncheckedTrackColor = ToirTheme.colors.surface,
            disabledUncheckedBorderColor = ToirTheme.colors.borderSubtle,
        ),
    )
}

@Preview
@Composable
private fun PreviewToirSwitchOn() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSwitch(
                checked = true,
                onCheckedChange = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirSwitchOff() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSwitch(
                checked = false,
                onCheckedChange = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirSwitchDisabledOn() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSwitch(
                checked = true,
                onCheckedChange = null,
                enabled = false,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirSwitchDisabledOff() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSwitch(
                checked = false,
                onCheckedChange = null,
                enabled = false,
            )
        }
    }
}
