package ru.mirea.toir.sync.data.network

import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.network.ktor.KtorClient
import ru.mirea.toir.sync.data.network.models.DownloadedPhoto
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

    override suspend fun downloadPhoto(photoId: String): Result<DownloadedPhoto> = coRunCatching(
        tryBlock = {
            // GET /api/v1/mobile/photos/{photoId} returns raw bytes. Auth (JWT bearer) is
            // wired in the shared Ktor client config — mirrors uploadPhoto exactly.
            val response = ktorClient.get("/api/v1/mobile/photos/$photoId")
            if (!response.status.isSuccess()) {
                error("downloadPhoto failed: status=${response.status}")
            }
            val bytes = response.bodyAsBytes()
            val mime = response.headers[HttpHeaders.ContentType]
            DownloadedPhoto(bytes = bytes, mimeType = mime).wrapResultSuccess()
        },
        catchBlock = { throwable ->
            Napier.e(message = "downloadPhoto failed for id=$photoId", throwable = throwable)
            throwable.wrapResultFailure()
        },
    )
}
