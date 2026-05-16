package ru.mirea.toir.feature.photo.capture.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Intent
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Label
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.PhotoEntry
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
            is Intent.OnPhotoDeleted -> deletePhoto(intent.fileUri)
            Intent.OnConfirm -> publish(Label.PhotoConfirmed)
        }
    }

    private suspend fun loadPhotos() {
        val checklistItemResultId = state().checklistItemResultId
        repository.getPhotos(checklistItemResultId).fold(
            onSuccess = { entries -> dispatch(Message.SetPhotos(entries)) },
            onFailure = { /* silent */ },
        )
    }

    private suspend fun savePhoto(fileUri: String) {
        val resultId = state().checklistItemResultId
        dispatch(Message.SetLoading(true))
        repository.savePhoto(resultId, fileUri).fold(
            onSuccess = {
                // Freshly captured photo: id is unknown to the executor (the repository
                // generates it). For UI rendering we only need a stable key — we use the
                // fileUri itself as the id, since it's unique per shot (UUID-based) and
                // pending photos never have null fileUri.
                dispatch(Message.AddPhoto(PhotoEntry(id = fileUri, fileUri = fileUri)))
                dispatch(Message.SetLoading(false))
            },
            onFailure = { dispatch(Message.SetLoading(false)) },
        )
    }

    private suspend fun deletePhoto(fileUri: String) {
        val resultId = state().checklistItemResultId
        repository.deletePhoto(resultId, fileUri).fold(
            onSuccess = { dispatch(Message.PhotoRemoved(fileUri)) },
            onFailure = { /* keep state unchanged so UI doesn't lie; logged in repo */ },
        )
    }
}
