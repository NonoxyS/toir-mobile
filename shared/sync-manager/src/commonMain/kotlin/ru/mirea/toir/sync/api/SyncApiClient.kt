package ru.mirea.toir.sync.api

import ru.mirea.toir.sync.api.models.RemoteSyncPushRequest
import ru.mirea.toir.sync.api.models.RemoteSyncPushResponse

internal interface SyncApiClient {
    suspend fun pushSync(request: RemoteSyncPushRequest): Result<RemoteSyncPushResponse>
    suspend fun uploadPhoto(photoId: String, checklistItemResultId: String, fileBytes: ByteArray): Result<Unit>
    suspend fun fetchConfigChanges(since: String): Result<Unit>
}
