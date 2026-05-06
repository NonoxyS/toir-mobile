package ru.mirea.toir.common.ui.compose.components.shared.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
 * Primary CTA button.
 *
 * Renders a Material3 [Button] tinted with [ToirTheme.colors.ctaPrimary] and a 6dp
 * (sm) corner radius. The button is constrained to a minimum height of 48 dp so callers
 * can compose [Modifier.fillMaxWidth] without losing the spec touch target.
 *
 * When [isLoading] is true the button auto-disables and renders a 20 dp
 * [CircularProgressIndicator] in place of [text]; the optional [leadingIcon] is also
 * hidden during loading.
 */
@Composable
fun ToirPrimaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled && !isLoading,
        shape = ToirTheme.shapes.sm,
        colors = ButtonDefaults.buttonColors(
            containerColor = ToirTheme.colors.ctaPrimary,
            contentColor = ToirTheme.colors.textOnAccent,
            disabledContainerColor = ToirTheme.colors.ctaPrimary.copy(alpha = 0.5f),
            disabledContentColor = ToirTheme.colors.textOnAccent.copy(alpha = 0.25f),
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = ToirTheme.colors.textOnAccent,
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
private fun PreviewToirPrimaryButtonDefault() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirPrimaryButton(
                onClick = {},
                text = "Сохранить",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirPrimaryButtonWithIcon() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirPrimaryButton(
                onClick = {},
                text = "Подтвердить",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(MR.images.ic_check_circle),
                        contentDescription = null,
                        tint = ToirTheme.colors.textOnAccent,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirPrimaryButtonLoading() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirPrimaryButton(
                onClick = {},
                text = "Сохранить",
                modifier = Modifier.fillMaxWidth(),
                isLoading = true,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirPrimaryButtonDisabled() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirPrimaryButton(
                onClick = {},
                text = "Сохранить",
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )
        }
    }
}
