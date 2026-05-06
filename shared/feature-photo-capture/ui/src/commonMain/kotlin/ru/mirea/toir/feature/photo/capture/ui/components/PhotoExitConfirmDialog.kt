package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.components.shared.button.ToirDestructiveButton
import ru.mirea.toir.common.ui.compose.components.shared.button.ToirGhostButton
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
            ToirDestructiveButton(
                onClick = onDiscard,
                text = stringResource(MR.strings.common_button_delete),
            )
        },
        dismissButton = {
            ToirGhostButton(
                onClick = onContinueCapture,
                text = stringResource(MR.strings.common_button_continue_capture),
            )
        },
    )
}
