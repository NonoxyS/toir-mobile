package ru.mirea.toir.feature.photo.capture.ui

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.feature.photo.capture.presentation.PhotoCaptureViewModel
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoCaptureLabel
import ru.mirea.toir.feature.photo.capture.ui.components.PhotoCaptureContent
import ru.mirea.toir.feature.photo.capture.ui.components.PhotoCaptureFooter
import ru.mirea.toir.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoCaptureScreen(
    checklistItemResultId: String,
    onPhotoConfirm: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PhotoCaptureViewModel = koinViewModel { parametersOf(checklistItemResultId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiPhotoCaptureLabel.PhotoConfirmed -> onPhotoConfirm()
        }
    }

    val cameraLauncher = rememberCameraLauncher(onPhotoTaken = viewModel::onPhotoTaken)

    Scaffold(
        containerColor = ToirTheme.colors.background,
        topBar = {
            PhotoCaptureTopBar(
                photoCount = state.photos.size,
                maxPhotos = state.maxPhotos,
                onBack = onNavigateBack,
            )
        },
        bottomBar = {
            val isLimitReached = state.maxPhotos?.let { state.photos.size >= it } ?: false
            PhotoCaptureFooter(
                canTake = !state.isLoading && !isLimitReached,
                canConfirm = state.photos.isNotEmpty(),
                isLimitReached = isLimitReached,
                onTakePhoto = cameraLauncher,
                onConfirm = viewModel::onConfirm,
            )
        },
    ) { paddingValues ->
        PhotoCaptureContent(
            photos = state.photos,
            onPhotoLongPress = viewModel::onPhotoDeleted,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoCaptureTopBar(
    photoCount: Int,
    maxPhotos: Int?,
    onBack: () -> Unit,
) {
    val title = if (maxPhotos != null) {
        stringResource(MR.strings.photo_capture_title_with_progress, photoCount, maxPhotos)
    } else {
        stringResource(MR.strings.photo_capture_title)
    }
    TopAppBar(
        title = {
            Text(text = title, style = ToirTheme.typography.headline, color = ToirTheme.colors.textPrimary)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Image(
                    painter = painterResource(MR.images.ic_arrow_back),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(ToirTheme.colors.textSecondary),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ToirTheme.colors.background,
        ),
    )
}
