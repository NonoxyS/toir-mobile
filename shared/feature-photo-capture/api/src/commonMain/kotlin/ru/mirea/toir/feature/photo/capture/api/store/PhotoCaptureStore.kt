package ru.mirea.toir.feature.photo.capture.api.store

import com.arkivanov.mvikotlin.core.store.Store
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Intent
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Label
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.State

interface PhotoCaptureStore : Store<Intent, State, Label> {

    data class State(
        val checklistItemResultId: String = "",
        val photos: List<String> = emptyList(),
        val isLoading: Boolean = false,
    )

    sealed interface Intent {
        data class Init(val checklistItemResultId: String) : Intent
        data class OnPhotoTaken(val fileUri: String) : Intent
        data object OnConfirm : Intent
    }

    sealed interface Label {
        data object PhotoConfirmed : Label
    }
}
