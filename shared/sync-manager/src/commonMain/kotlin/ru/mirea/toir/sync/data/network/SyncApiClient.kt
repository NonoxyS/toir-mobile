package ru.mirea.toir.sync.data.network

import ru.mirea.toir.sync.data.network.models.DownloadedPhoto
import ru.mirea.toir.sync.data.network.models.RemoteConfigChangesResponse
import ru.mirea.toir.sync.data.network.models.RemotePhotoUploadResponse
import ru.mirea.toir.sync.data.network.models.RemoteSyncPushRequest
import ru.mirea.toir.sync.data.network.models.RemoteSyncPushResponse

internal interface SyncApiClient {
    suspend fun pushSync(request: RemoteSyncPushRequest): Result<RemoteSyncPushResponse>
    suspend fun uploadPhoto(
        photoId: String,
        checklistItemResultId: String,
        fileBytes: ByteArray,
    ): Result<RemotePhotoUploadResponse>

    suspend fun fetchConfigChanges(since: String): Result<RemoteConfigChangesResponse>

    /**
     * GET /api/v1/mobile/photos/{photoId} → raw image bytes.
     * The server returns the original Content-Type (e.g. `image/jpeg`); we expose it via
     * [DownloadedPhoto.mimeType] so the writer can pick a sensible file extension.
     */
    suspend fun downloadPhoto(photoId: String): Result<DownloadedPhoto>
}
