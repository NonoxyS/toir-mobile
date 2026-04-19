package ru.mirea.toir.feature.photo.capture.impl.domain

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Intent
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Label
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.State
import ru.mirea.toir.feature.photo.capture.impl.domain.repository.PhotoCaptureRepository

internal class PhotoCaptureStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: PhotoCaptureRepository,
    private val mainDispatcher: CoroutineDispatcher,
) {
    fun create(): PhotoCaptureStore =
        object :
            PhotoCaptureStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = PhotoCaptureStore::class.simpleName,
                initialState = State(),
                bootstrapper = null,
                executorFactory = { PhotoCaptureExecutor(repository, mainDispatcher) },
                reducer = PhotoCaptureReducer(),
            ) {}

    internal sealed interface Message {
        data class SetLoading(val value: Boolean) : Message
        data class SetPhotos(val photos: ImmutableList<String>) : Message
        data class AddPhoto(val uri: String) : Message
        data class SetResultId(val id: String) : Message
    }
}
