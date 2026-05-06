package ru.mirea.toir.common.ui.compose.components.shared.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

/**
 * Ghost button per MASTER §8.1.
 *
 * Renders a Material3 [TextButton] with a transparent background and
 * [ToirTheme.colors.textSecondary] content. Used for tertiary actions such as
 * cancel buttons in dialogs and inline navigation links.
 *
 * Material3 [TextButton] enforces a minimum width of 64 dp which can leave too much
 * side-padding around short inline labels. We override that default with
 * [Modifier.defaultMinSize] (minWidth = 1 dp) so callers control width via their own
 * `widthIn` / `fillMaxWidth`. Touch target height is preserved at 48 dp.
 *
 * Unlike Primary/Secondary/Destructive variants, the Ghost button does not expose an
 * `isLoading` state — tertiary actions are not expected to gate on async work.
 */
@Composable
fun ToirGhostButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minWidth = 1.dp)
            .heightIn(min = 48.dp),
        enabled = enabled,
        shape = ToirTheme.shapes.sm,
        colors = ButtonDefaults.textButtonColors(
            contentColor = ToirTheme.colors.textSecondary,
            disabledContentColor = ToirTheme.colors.textSecondary.copy(alpha = 0.25f),
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (leadingIcon != null) {
                Box(Modifier.size(20.dp)) {
                    leadingIcon()
                }
            }
            Text(
                text = text,
                style = ToirTheme.typography.label,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirGhostButtonDefault() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirGhostButton(
                onClick = {},
                text = "Отмена",
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirGhostButtonWithIcon() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirGhostButton(
                onClick = {},
                text = "Назад",
                leadingIcon = {
                    Icon(
                        painter = painterResource(MR.images.ic_arrow_back),
                        contentDescription = null,
                        tint = ToirTheme.colors.textSecondary,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirGhostButtonDisabled() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirGhostButton(
                onClick = {},
                text = "Отмена",
                enabled = false,
            )
        }
    }
}
