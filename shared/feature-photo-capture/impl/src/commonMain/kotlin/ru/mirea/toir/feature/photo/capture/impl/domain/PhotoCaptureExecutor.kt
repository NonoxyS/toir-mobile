package ru.mirea.toir.feature.photo.capture.impl.domain

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Intent
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Label
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.State
import ru.mirea.toir.feature.photo.capture.impl.domain.PhotoCaptureStoreFactory.Message
import ru.mirea.toir.feature.photo.capture.impl.domain.repository.PhotoCaptureRepository

internal class PhotoCaptureExecutor(
    private val repository: PhotoCaptureRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<Intent, Nothing, State, Message, Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.Init -> {
                dispatch(Message.SetResultId(intent.checklistItemResultId))
                loadPhotos(intent.checklistItemResultId)
            }
            is Intent.OnPhotoTaken -> {
                val resultId = state().checklistItemResultId
                dispatch(Message.SetLoading(true))
                repository.savePhoto(resultId, intent.fileUri).fold(
                    onSuccess = {
                        dispatch(Message.AddPhoto(intent.fileUri))
                        dispatch(Message.SetLoading(false))
                    },
                    onFailure = {
                        dispatch(Message.SetLoading(false))
                    },
                )
            }
            Intent.OnConfirm -> publish(Label.PhotoConfirmed)
        }
    }

    private suspend fun loadPhotos(checklistItemResultId: String) {
        repository.getPhotos(checklistItemResultId).fold(
            onSuccess = { uris ->
                dispatch(Message.SetPhotos(uris.toImmutableList()))
            },
            onFailure = { /* silent */ },
        )
    }
}
