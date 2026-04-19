package ru.mirea.toir.sync.api

import io.github.aakira.napier.Napier
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.network.ktor.KtorClient
import ru.mirea.toir.sync.api.models.RemoteSyncPushRequest
import ru.mirea.toir.sync.api.models.RemoteSyncPushResponse

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
    ): Result<Unit> = coRunCatching(
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
            if (response.status.isSuccess()) {
                Unit.wrapResultSuccess()
            } else {
                Result.failure(Exception("uploadPhoto failed: ${response.status}"))
            }
        },
        catchBlock = { throwable ->
            Napier.e(message = "uploadPhoto failed", throwable = throwable)
            throwable.wrapResultFailure()
        },
    )

    override suspend fun fetchConfigChanges(since: String): Result<Unit> = coRunCatching(
        tryBlock = {
            val response = ktorClient.get("/api/v1/mobile/config/changes?since=$since")
            if (response.status.isSuccess()) {
                Unit.wrapResultSuccess()
            } else {
                Result.failure(Exception("fetchConfigChanges failed: ${response.status}"))
            }
        },
        catchBlock = { throwable ->
            Napier.e(message = "fetchConfigChanges failed", throwable = throwable)
            throwable.wrapResultFailure()
        },
    )
}
