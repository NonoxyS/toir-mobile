package ru.mirea.toir.feature.photo.capture.impl.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore

internal interface PhotoCaptureRepository {
    suspend fun savePhoto(checklistItemResultId: String, fileUri: String): Result<Unit>

    /**
     * Emits the photos attached to a checklist item result whenever the underlying table
     * changes. Includes restored photos whose file is not yet downloaded
     * (`fileUri == null`) — the UI renders the latter as placeholders. Reactive so the
     * placeholder tile flips to the real image as soon as the sync manager fills file_uri.
     */
    fun observePhotos(checklistItemResultId: String): Flow<List<PhotoCaptureStore.PhotoEntry>>

    suspend fun deletePhoto(checklistItemResultId: String, fileUri: String): Result<Unit>
}
