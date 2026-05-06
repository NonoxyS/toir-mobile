package ru.mirea.toir.common.ui.compose.components.shared.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

/**
 * Toggle chip for "done / acknowledge" semantics. Filled when selected, outlined otherwise.
 * Click toggles state via onSelectedChange. Border turns red when isError.
 */
@Composable
fun ToirToggleChip(
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    val borderColor = when {
        isError -> ToirTheme.colors.error
        selected -> ToirTheme.colors.success
        else -> ToirTheme.colors.border
    }
    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = {
            Text(
                text = text,
                style = ToirTheme.typography.bodyLarge,
            )
        },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = if (selected) {
            {
                Icon(
                    painter = painterResource(MR.images.ic_check_circle),
                    contentDescription = null,
                    tint = ToirTheme.colors.success,
                )
            }
        } else {
            null
        },
        border = BorderStroke(width = 1.dp, color = borderColor),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = ToirTheme.colors.surface2,
            labelColor = ToirTheme.colors.textPrimary,
            selectedContainerColor = ToirTheme.colors.surface2,
            selectedLabelColor = ToirTheme.colors.textPrimary,
            disabledContainerColor = ToirTheme.colors.surface,
            disabledLabelColor = ToirTheme.colors.textDisabled,
        ),
    )
}

@Preview
@Composable
private fun PreviewToirToggleChipUnselected() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirToggleChip(
                selected = false,
                onSelectedChange = {},
                text = "Выполнено",
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirToggleChipSelected() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirToggleChip(
                selected = true,
                onSelectedChange = {},
                text = "Выполнено",
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirToggleChipError() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirToggleChip(
                selected = false,
                onSelectedChange = {},
                text = "Выполнено",
                isError = true,
            )
        }
    }
}
