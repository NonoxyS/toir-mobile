package ru.mirea.toir.feature.photo.capture.api.store

import com.arkivanov.mvikotlin.core.store.Store
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Intent
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.Label
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.State

interface PhotoCaptureStore : Store<Intent, State, Label> {

    /**
     * One photo entry in the row above the camera button. `fileUri == null` means the
     * photo's metadata was restored from the server but the binary file is not yet on
     * disk — the sync manager downloads it in the background. The UI shows a placeholder
     * tile for these entries (see `docs/design-system/pages/photo-capture.md`).
     */
    data class PhotoEntry(
        val id: String,
        val fileUri: String?,
    )

    data class State(
        val checklistItemResultId: String = "",
        val photos: List<PhotoEntry> = emptyList(),
        val maxPhotos: Int? = DEFAULT_MAX_PHOTOS,
        val isLoading: Boolean = false,
    )

    sealed interface Intent {
        data class OnPhotoTaken(val fileUri: String) : Intent
        data class OnPhotoDeleted(val fileUri: String) : Intent
        data object OnConfirm : Intent
    }

    sealed interface Label {
        data object PhotoConfirmed : Label
    }

    companion object {
        const val DEFAULT_MAX_PHOTOS: Int = 5
    }
}
