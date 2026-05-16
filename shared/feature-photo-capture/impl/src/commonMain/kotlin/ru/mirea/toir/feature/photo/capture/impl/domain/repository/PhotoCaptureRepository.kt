package ru.mirea.toir.feature.photo.capture.impl.domain.repository

import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore

internal interface PhotoCaptureRepository {
    suspend fun savePhoto(checklistItemResultId: String, fileUri: String): Result<Unit>

    /**
     * Returns all photos attached to a checklist item result, including restored photos
     * whose file is not yet downloaded (`fileUri == null`). The UI renders the latter as
     * placeholders — see `PhotoCaptureStore.PhotoEntry`.
     */
    suspend fun getPhotos(checklistItemResultId: String): Result<List<PhotoCaptureStore.PhotoEntry>>
    suspend fun deletePhoto(checklistItemResultId: String, fileUri: String): Result<Unit>
}
