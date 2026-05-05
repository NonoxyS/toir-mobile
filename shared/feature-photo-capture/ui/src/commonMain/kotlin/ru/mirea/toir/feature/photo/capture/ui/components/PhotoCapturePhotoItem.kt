package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import dev.icerock.moko.resources.compose.painterResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@Composable
internal fun PhotoCapturePhotoItem(
    uri: String,
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors
    var painterState by remember { mutableStateOf<AsyncImagePainter.State?>(null) }

    Box(
        modifier = modifier
            .size(120.dp)
            .clip(ToirTheme.shapes.md)
            .background(colors.surface2)
            .border(width = 1.dp, color = colors.borderSubtle, shape = ToirTheme.shapes.md)
            .pointerInput(uri) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.size(120.dp).clip(ToirTheme.shapes.md),
            onState = { painterState = it },
        )
        when (painterState) {
            is AsyncImagePainter.State.Loading -> CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = colors.textSecondary,
            )
            is AsyncImagePainter.State.Error -> Image(
                painter = painterResource(MR.images.ic_broken_image),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(colors.textDisabled),
            )
            else -> Unit
        }
    }
}
