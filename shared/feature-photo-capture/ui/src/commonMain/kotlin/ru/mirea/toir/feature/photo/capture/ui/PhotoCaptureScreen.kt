package ru.mirea.toir.feature.photo.capture.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
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
import ru.mirea.toir.feature.photo.capture.ui.components.PhotoDeleteConfirmDialog
import ru.mirea.toir.feature.photo.capture.ui.components.PhotoExitConfirmDialog
import ru.mirea.toir.feature.photo.capture.ui.preview.PhotoPreviewScreen
import ru.mirea.toir.res.MR

private const val PREVIEW_TRANSITION_MS = 250

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalSharedTransitionApi::class,
)
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

    var pendingDeleteUri by remember { mutableStateOf<String?>(null) }
    var previewUri by remember { mutableStateOf<String?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    val handleBack: () -> Unit = {
        if (state.photos.isNotEmpty()) showExitDialog = true else onNavigateBack()
    }

    BackHandler(enabled = state.photos.isNotEmpty() && previewUri == null) {
        showExitDialog = true
    }
    BackHandler(enabled = previewUri != null) {
        previewUri = null
    }

    SharedTransitionLayout {
        AnimatedContent(
            targetState = previewUri,
            transitionSpec = {
                fadeIn(tween(PREVIEW_TRANSITION_MS)) togetherWith
                    fadeOut(tween(PREVIEW_TRANSITION_MS))
            },
            label = "photo-preview-content",
        ) { currentPreview ->
            if (currentPreview == null) {
                Scaffold(
                    containerColor = ToirTheme.colors.background,
                    topBar = {
                        PhotoCaptureTopBar(
                            photoCount = state.photos.size,
                            maxPhotos = state.maxPhotos,
                            onBack = handleBack,
                        )
                    },
                    bottomBar = {
                        val isLimitReached = state.maxPhotos
                            ?.let { state.photos.size >= it }
                            ?: false
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
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent,
                        onPhotoTap = { uri -> previewUri = uri },
                        onPhotoLongPress = { uri -> pendingDeleteUri = uri },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    )
                }
            } else {
                val index = state.photos.indexOf(currentPreview).takeIf { it >= 0 } ?: 0
                PhotoPreviewScreen(
                    photoUri = currentPreview,
                    photoIndex = index + 1,
                    totalCount = state.photos.size,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent,
                    onClose = { previewUri = null },
                )
            }
        }
    }

    pendingDeleteUri?.let { uri ->
        PhotoDeleteConfirmDialog(
            onDismiss = { pendingDeleteUri = null },
            onConfirm = {
                viewModel.onPhotoDeleted(uri)
                pendingDeleteUri = null
            },
        )
    }

    if (showExitDialog) {
        PhotoExitConfirmDialog(
            onContinueCapture = { showExitDialog = false },
            onDiscard = {
                showExitDialog = false
                onNavigateBack()
            },
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
            Text(
                text = title,
                style = ToirTheme.typography.headline,
                color = ToirTheme.colors.textPrimary,
            )
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
