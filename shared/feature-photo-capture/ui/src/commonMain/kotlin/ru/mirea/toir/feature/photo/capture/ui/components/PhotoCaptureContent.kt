package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.ImmutableList
import ru.mirea.toir.res.MR

@Composable
internal fun PhotoCaptureContent(
    photos: ImmutableList<String>,
    isLoading: Boolean,
    onTakePhoto: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (photos.isNotEmpty()) {
            PhotoCapturePhotoRow(photos = photos)
        }

        Button(
            onClick = onTakePhoto,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
        ) {
            Text(text = stringResource(MR.strings.photo_capture_button_take))
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            enabled = photos.isNotEmpty(),
        ) {
            Text(text = stringResource(MR.strings.photo_capture_button_confirm))
        }
    }
}
