package ru.mirea.toir.feature.photo.capture.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.mapNotNull
import ru.mirea.toir.core.presentation.viewmodel.BaseViewModel
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Intent
import ru.mirea.toir.feature.photo.capture.presentation.mappers.UiPhotoCaptureLabelMapper
import ru.mirea.toir.feature.photo.capture.presentation.mappers.UiPhotoCaptureStateMapper
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoCaptureLabel
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoCaptureState

class PhotoCaptureViewModel internal constructor(
    private val store: PhotoCaptureStore,
    stateMapper: UiPhotoCaptureStateMapper,
    labelMapper: UiPhotoCaptureLabelMapper,
) : BaseViewModel<UiPhotoCaptureState, UiPhotoCaptureLabel>(initialState = UiPhotoCaptureState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun init(checklistItemResultId: String) = store.accept(Intent.Init(checklistItemResultId))
    fun onPhotoTaken(fileUri: String) = store.accept(Intent.OnPhotoTaken(fileUri))
    fun onConfirm() = store.accept(Intent.OnConfirm)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
