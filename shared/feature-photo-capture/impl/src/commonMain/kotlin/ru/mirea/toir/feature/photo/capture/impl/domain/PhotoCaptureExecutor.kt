package ru.mirea.toir.feature.photo.capture.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
            Action.Load -> subscribeToPhotos()
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.OnPhotoTaken -> savePhoto(intent.fileUri)
            is Intent.OnPhotoDeleted -> deletePhoto(intent.fileUri)
            Intent.OnConfirm -> publish(Label.PhotoConfirmed)
        }
    }

    // Subscribe once on bootstrap; SQLDelight re-emits on every photos-table change so
    // the placeholder tile flips to the real image as soon as sync fills file_uri. The
    // photos list flows through this single channel — no manual AddPhoto/PhotoRemoved.
    private fun subscribeToPhotos() {
        val id = state().checklistItemResultId
        repository.observePhotos(id)
            .onEach { entries -> dispatch(Message.SetPhotos(entries)) }
            .launchIn(scope)
    }

    private suspend fun savePhoto(fileUri: String) {
        val resultId = state().checklistItemResultId
        dispatch(Message.SetLoading(true))
        repository.savePhoto(resultId, fileUri)
        dispatch(Message.SetLoading(false))
    }

    private suspend fun deletePhoto(fileUri: String) {
        val resultId = state().checklistItemResultId
        repository.deletePhoto(resultId, fileUri)
    }
}
