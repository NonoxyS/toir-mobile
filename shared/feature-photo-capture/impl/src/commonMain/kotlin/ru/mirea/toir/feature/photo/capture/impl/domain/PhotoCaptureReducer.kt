package ru.mirea.toir.feature.photo.capture.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import kotlinx.collections.immutable.toImmutableList
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.State
import ru.mirea.toir.feature.photo.capture.impl.domain.PhotoCaptureStoreFactory.Message

internal class PhotoCaptureReducer : Reducer<State, Message> {
    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetLoading -> copy(isLoading = msg.value)
        is Message.SetPhotos -> copy(photos = msg.photos)
        is Message.AddPhoto -> copy(photos = (photos + msg.uri).toImmutableList())
        is Message.SetResultId -> copy(checklistItemResultId = msg.id)
    }
}
