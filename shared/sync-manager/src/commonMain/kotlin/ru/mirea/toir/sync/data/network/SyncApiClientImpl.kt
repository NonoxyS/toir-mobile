package ru.mirea.toir.sync.data.network

import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.network.ktor.KtorClient
import ru.mirea.toir.sync.data.network.models.RemoteConfigChangesResponse
import ru.mirea.toir.sync.data.network.models.RemotePhotoUploadResponse
import ru.mirea.toir.sync.data.network.models.RemoteSyncPushRequest
import ru.mirea.toir.sync.data.network.models.RemoteSyncPushResponse

internal class SyncApiClientImpl(
    private val ktorClient: KtorClient,
) : SyncApiClient {

    override suspend fun pushSync(request: RemoteSyncPushRequest): Result<RemoteSyncPushResponse> =
        ktorClient.executeQuery(
            query = {
                ktorClient.post("/api/v1/mobile/sync/push") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            },
            deserializer = RemoteSyncPushResponse.serializer(),
            success = { it.wrapResultSuccess() },
            loggingErrorMessage = "pushSync failed",
        )

    override suspend fun uploadPhoto(
        photoId: String,
        checklistItemResultId: String,
        fileBytes: ByteArray,
    ): Result<RemotePhotoUploadResponse> = coRunCatching(
        tryBlock = {
            val response = ktorClient.submitFormWithBinaryData(
                urlString = "/api/v1/mobile/photos/upload",
                formData = formData {
                    append("photoId", photoId)
                    append("checklistItemResultId", checklistItemResultId)
                    append(
                        key = "file",
                        value = fileBytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"photo.jpg\"")
                        },
                    )
                },
            )
            val parsed = response.body<RemotePhotoUploadResponse>()
            parsed.wrapResultSuccess()
        },
        catchBlock = { throwable ->
            Napier.e(message = "uploadPhoto failed", throwable = throwable)
            throwable.wrapResultFailure()
        },
    )

    override suspend fun fetchConfigChanges(since: String): Result<RemoteConfigChangesResponse> =
        ktorClient.executeQuery(
            query = {
                ktorClient.get("/api/v1/mobile/config/changes?since=$since")
            },
            deserializer = RemoteConfigChangesResponse.serializer(),
            success = { it.wrapResultSuccess() },
            loggingErrorMessage = "fetchConfigChanges failed",
        )
}
