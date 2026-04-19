package ru.mirea.toir.feature.photo.capture.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.feature.photo.capture.presentation.PhotoCaptureViewModel
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoCaptureLabel
import ru.mirea.toir.feature.photo.capture.ui.components.PhotoCaptureContent
import ru.mirea.toir.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoCaptureScreen(
    checklistItemResultId: String,
    onPhotoConfirm: () -> Unit,
    viewModel: PhotoCaptureViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(checklistItemResultId) {
        viewModel.init(checklistItemResultId)
    }

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiPhotoCaptureLabel.PhotoConfirmed -> onPhotoConfirm()
        }
    }

    val cameraLauncher = rememberCameraLauncher(onPhotoTaken = viewModel::onPhotoTaken)

    Scaffold(
        topBar = { PhotoCaptureTopBar() },
    ) { paddingValues ->
        PhotoCaptureContent(
            photos = state.photos,
            isLoading = state.isLoading,
            onTakePhoto = cameraLauncher,
            onConfirm = viewModel::onConfirm,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoCaptureTopBar() {
    TopAppBar(
        title = {
            Text(
                text = stringResource(MR.strings.photo_capture_title),
                style = ToirTheme.typography.headline,
                color = ToirTheme.colors.textPrimary,
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewPhotoCaptureTopBar() {
    ToirTheme {
        PhotoCaptureTopBar()
    }
}
