package ru.mirea.toir.common.ui.compose.components.shared.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

/**
 * Secondary button per MASTER §8.1.
 *
 * Renders a Material3 [OutlinedButton] with [ToirTheme.colors.surface2] background,
 * [ToirTheme.colors.textPrimary] content and a 1 dp [ToirTheme.colors.border] outline.
 * Corner radius is sm (6 dp) and the touch target is constrained to a minimum height
 * of 48 dp so callers can compose [Modifier.fillMaxWidth] without losing the spec.
 *
 * When [isLoading] is true the button auto-disables and renders a 20 dp
 * [CircularProgressIndicator] in place of [text]; the optional [leadingIcon] is also
 * hidden during loading.
 */
@Composable
fun ToirSecondaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled && !isLoading,
        shape = ToirTheme.shapes.sm,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = ToirTheme.colors.surface2,
            contentColor = ToirTheme.colors.textPrimary,
            disabledContainerColor = ToirTheme.colors.surface2.copy(alpha = 0.5f),
            disabledContentColor = ToirTheme.colors.textPrimary.copy(alpha = 0.25f),
        ),
        border = BorderStroke(1.dp, ToirTheme.colors.border),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = ToirTheme.colors.textPrimary,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
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
}

@Preview
@Composable
private fun PreviewToirSecondaryButtonDefault() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSecondaryButton(
                onClick = {},
                text = "Отмена",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirSecondaryButtonWithIcon() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSecondaryButton(
                onClick = {},
                text = "Сканировать QR",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(MR.images.ic_qr_code_scanner),
                        contentDescription = null,
                        tint = ToirTheme.colors.textPrimary,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirSecondaryButtonLoading() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSecondaryButton(
                onClick = {},
                text = "Отмена",
                modifier = Modifier.fillMaxWidth(),
                isLoading = true,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirSecondaryButtonDisabled() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSecondaryButton(
                onClick = {},
                text = "Отмена",
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )
        }
    }
}
