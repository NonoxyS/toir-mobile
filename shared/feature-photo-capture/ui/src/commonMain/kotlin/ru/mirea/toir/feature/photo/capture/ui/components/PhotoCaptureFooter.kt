package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.components.shared.button.ToirPrimaryButton
import ru.mirea.toir.common.ui.compose.components.shared.button.ToirSecondaryButton
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@Composable
internal fun PhotoCaptureFooter(
    canTake: Boolean,
    canConfirm: Boolean,
    isLimitReached: Boolean,
    isLoading: Boolean,
    onTakePhoto: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ToirSecondaryButton(
            onClick = onTakePhoto,
            text = stringResource(MR.strings.photo_capture_button_take),
            modifier = Modifier.fillMaxWidth(),
            enabled = canTake,
            leadingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(TAKE_BUTTON_ICON_SIZE),
                        color = ToirTheme.colors.textPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        painter = painterResource(MR.images.ic_camera_alt),
                        contentDescription = null,
                        tint = ToirTheme.colors.textPrimary,
                    )
                }
            },
        )
        if (isLimitReached) {
            Text(
                text = stringResource(MR.strings.photo_capture_limit_reached),
                style = ToirTheme.typography.caption,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        ToirPrimaryButton(
            onClick = onConfirm,
            text = stringResource(MR.strings.photo_capture_button_confirm),
            modifier = Modifier.fillMaxWidth(),
            enabled = canConfirm,
        )
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

private val TAKE_BUTTON_ICON_SIZE = 20.dp
