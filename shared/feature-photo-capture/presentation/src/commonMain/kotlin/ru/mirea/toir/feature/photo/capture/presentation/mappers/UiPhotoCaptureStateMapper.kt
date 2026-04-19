package ru.mirea.toir.feature.photo.capture.presentation.mappers

import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.State
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoCaptureState

internal interface UiPhotoCaptureStateMapper {
    fun map(state: State): UiPhotoCaptureState?
}

internal class UiPhotoCaptureStateMapperImpl : UiPhotoCaptureStateMapper {
    override fun map(state: State): UiPhotoCaptureState = UiPhotoCaptureState(
        photos = state.photos,
        isLoading = state.isLoading,
    )
}
