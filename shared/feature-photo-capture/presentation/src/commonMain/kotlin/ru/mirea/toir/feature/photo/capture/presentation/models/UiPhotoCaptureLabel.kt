package ru.mirea.toir.feature.photo.capture.presentation.models

sealed interface UiPhotoCaptureLabel {
    data object PhotoConfirmed : UiPhotoCaptureLabel
}
