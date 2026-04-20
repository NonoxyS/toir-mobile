package ru.mirea.toir.sync.data.network

import ru.mirea.toir.sync.data.network.models.RemoteSyncPushRequest
import ru.mirea.toir.sync.data.network.models.RemoteSyncPushResponse

internal interface SyncApiClient {
    suspend fun pushSync(request: RemoteSyncPushRequest): Result<RemoteSyncPushResponse>
    suspend fun uploadPhoto(photoId: String, checklistItemResultId: String, fileBytes: ByteArray): Result<Unit>
    suspend fun fetchConfigChanges(since: String): Result<Unit>
}
