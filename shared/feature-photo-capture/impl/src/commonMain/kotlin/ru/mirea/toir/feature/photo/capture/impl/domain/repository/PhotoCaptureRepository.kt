package ru.mirea.toir.feature.photo.capture.impl.domain.repository

internal interface PhotoCaptureRepository {
    suspend fun savePhoto(checklistItemResultId: String, fileUri: String): Result<Unit>
    suspend fun getPhotos(checklistItemResultId: String): Result<List<String>>
}
