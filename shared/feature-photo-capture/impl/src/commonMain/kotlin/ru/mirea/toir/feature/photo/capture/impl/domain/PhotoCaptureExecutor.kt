package ru.mirea.toir.feature.photo.capture.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Intent
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Label
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.State
import ru.mirea.toir.feature.photo.capture.impl.domain.PhotoCaptureStoreFactory.Action
import ru.mirea.toir.feature.photo.capture.impl.domain.PhotoCaptureStoreFactory.Message
import ru.mirea.toir.feature.photo.capture.impl.domain.repository.PhotoCaptureRepository

internal class PhotoCaptureExecutor(
    private val repository: PhotoCaptureRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<Intent, Action, State, Message, Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteAction(action: Action) {
        when (action) {
            Action.Load -> loadPhotos()
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.OnPhotoTaken -> savePhoto(intent.fileUri)
            is Intent.OnPhotoDeleted -> dispatch(Message.PhotoRemoved(intent.fileUri))
            Intent.OnConfirm -> publish(Label.PhotoConfirmed)
        }
    }

    private suspend fun loadPhotos() {
        val checklistItemResultId = state().checklistItemResultId
        repository.getPhotos(checklistItemResultId).fold(
            onSuccess = { uris -> dispatch(Message.SetPhotos(uris)) },
            onFailure = { /* silent */ },
        )
    }

    private suspend fun savePhoto(fileUri: String) {
        val resultId = state().checklistItemResultId
        dispatch(Message.SetLoading(true))
        repository.savePhoto(resultId, fileUri).fold(
            onSuccess = {
                dispatch(Message.AddPhoto(fileUri))
                dispatch(Message.SetLoading(false))
            },
            onFailure = { dispatch(Message.SetLoading(false)) },
        )
    }
}
