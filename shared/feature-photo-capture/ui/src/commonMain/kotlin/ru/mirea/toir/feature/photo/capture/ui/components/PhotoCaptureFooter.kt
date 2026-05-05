package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@Composable
internal fun PhotoCaptureFooter(
    canTake: Boolean,
    canConfirm: Boolean,
    isLimitReached: Boolean,
    onTakePhoto: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = ToirTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onTakePhoto,
            enabled = canTake,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.surface2,
                contentColor = colors.textPrimary,
                disabledContainerColor = colors.surface2,
                disabledContentColor = colors.textDisabled,
            ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(MR.images.ic_camera_alt),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(
                        if (canTake) colors.textPrimary else colors.textDisabled,
                    ),
                )
                Text(
                    text = stringResource(MR.strings.photo_capture_button_take),
                    style = ToirTheme.typography.label,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        if (isLimitReached) {
            Text(
                text = stringResource(MR.strings.photo_capture_limit_reached),
                style = ToirTheme.typography.caption,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Button(
            onClick = onConfirm,
            enabled = canConfirm,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.ctaPrimary,
                contentColor = colors.textOnAccent,
            ),
        ) {
            Text(
                text = stringResource(MR.strings.photo_capture_button_confirm),
                style = ToirTheme.typography.label,
            )
        }
        if (!canConfirm) {
            Text(
                text = stringResource(MR.strings.photo_capture_confirm_disabled_hint),
                style = ToirTheme.typography.caption,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
