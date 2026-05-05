package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@Composable
internal fun PhotoExitConfirmDialog(
    onContinueCapture: () -> Unit,
    onDiscard: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onContinueCapture,
        containerColor = ToirTheme.colors.surface2,
        title = {
            Text(
                text = stringResource(MR.strings.photo_exit_title),
                style = ToirTheme.typography.headline,
                color = ToirTheme.colors.textPrimary,
            )
        },
        text = {
            Text(
                text = stringResource(MR.strings.photo_exit_message),
                style = ToirTheme.typography.bodyMedium,
                color = ToirTheme.colors.textSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text(
                    text = stringResource(MR.strings.common_button_delete),
                    color = ToirTheme.colors.error,
                    style = ToirTheme.typography.label,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onContinueCapture) {
                Text(
                    text = stringResource(MR.strings.common_button_continue_capture),
                    color = ToirTheme.colors.textSecondary,
                    style = ToirTheme.typography.label,
                )
            }
        },
    )
}
