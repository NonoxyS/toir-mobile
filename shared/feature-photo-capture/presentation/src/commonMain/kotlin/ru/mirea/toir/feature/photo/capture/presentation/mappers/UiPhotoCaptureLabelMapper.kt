package ru.mirea.toir.feature.photo.capture.presentation.mappers

import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Label
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoCaptureLabel

internal interface UiPhotoCaptureLabelMapper {
    fun map(label: Label): UiPhotoCaptureLabel?
}

internal class UiPhotoCaptureLabelMapperImpl : UiPhotoCaptureLabelMapper {
    override fun map(label: Label): UiPhotoCaptureLabel = when (label) {
        Label.PhotoConfirmed -> UiPhotoCaptureLabel.PhotoConfirmed
    }
}
