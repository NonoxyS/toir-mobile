package ru.mirea.toir.feature.photo.capture.presentation.mappers

import kotlinx.collections.immutable.toImmutableList
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.State
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoCaptureState
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoEntry

internal interface UiPhotoCaptureStateMapper {
    fun map(state: State): UiPhotoCaptureState?
}

internal class UiPhotoCaptureStateMapperImpl : UiPhotoCaptureStateMapper {
    override fun map(state: State): UiPhotoCaptureState {
        val isLimitReached = state.maxPhotos?.let { state.photos.size >= it } ?: false
        return UiPhotoCaptureState(
            photos = state.photos
                .map { UiPhotoEntry(id = it.id, fileUri = it.fileUri) }
                .toImmutableList(),
            maxPhotos = state.maxPhotos,
            isLoading = state.isLoading,
            isLimitReached = isLimitReached,
            canTakePhoto = !state.isLoading && !isLimitReached,
        )
    }
}
