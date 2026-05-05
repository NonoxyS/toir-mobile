package ru.mirea.toir.feature.photo.capture.ui.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoPreviewScreen(
    photoUri: String,
    photoIndex: Int,
    totalCount: Int,
    onClose: () -> Unit,
) {
    Scaffold(
        containerColor = ToirTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            MR.strings.photo_capture_preview_title,
                            photoIndex,
                            totalCount,
                        ),
                        style = ToirTheme.typography.displayMedium,
                        color = ToirTheme.colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Image(
                            painter = painterResource(MR.images.ic_close),
                            contentDescription = stringResource(
                                MR.strings.photo_capture_preview_close_content_description,
                            ),
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(ToirTheme.colors.textPrimary),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ToirTheme.colors.background,
                ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ToirTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(16.dp),
            )
        }
    }
}
