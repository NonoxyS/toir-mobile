@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.sync.fixtures

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.mirea.toir.core.network.ktor.KtorClientImpl
import ru.mirea.toir.sync.data.network.SyncApiClient
import ru.mirea.toir.sync.data.network.SyncApiClientImpl

internal class TestSyncApi {

    // Endpoint path constants (must match SyncApiClientImpl exactly)
    private val pushPath = "/api/v1/mobile/sync/push"
    private val photoPath = "/api/v1/mobile/photos/upload"
    private val configPath = "/api/v1/mobile/config/changes"

    // Photo download endpoint is /api/v1/mobile/photos/{id} — match the prefix and
    // exclude /upload by ordering (upload is checked first).
    private val photoDownloadPrefix = "/api/v1/mobile/photos/"

    private var pushHandler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        respondJson(
            """
            {
                "clientBatchId":"test-batch",
                "result":"accepted",
                "accepted":{
                    "inspections":[],
                    "inspectionEquipmentResults":[],
                    "checklistItemResults":[],
                    "actionLogs":[]
                },
                "rejected":[],
                "serverTime":"2026-05-13T00:00:00Z"
            }
            """.trimIndent()
        )
    }
    private var fetchHandler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        respondJson(
            """
            {
                "assignments":[],
                "routes":[],
                "routePoints":[],
                "equipment":[],
                "locations":[],
                "checklists":[],
                "checklistItems":[],
                "deletedIds":{
                    "assignments":[],
                    "routes":[],
                    "routePoints":[],
                    "equipment":[],
                    "locations":[],
                    "checklists":[],
                    "checklistItems":[]
                },
                "serverTime":"2026-05-13T00:00:00Z"
            }
            """.trimIndent()
        )
    }
    private var photoHandler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        respondJson("""{"storageKey":"k-1"}""")
    }

    private var photoDownloadHandler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData =
        { request ->
            // Default: return deterministic bytes derived from the photoId so tests can verify
            // the right bytes landed on disk.
            val id = request.url.encodedPath.substringAfterLast('/')
            respond(
                content = "photo-bytes-for-$id".encodeToByteArray(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "image/jpeg"),
            )
        }

    val capturedRequests = mutableListOf<HttpRequestData>()

    fun stubPush(h: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) {
        pushHandler = h
    }

    fun stubFetch(h: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) {
        fetchHandler = h
    }

    fun stubPhoto(h: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) {
        photoHandler = h
    }

    fun stubPhotoDownload(h: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) {
        photoDownloadHandler = h
    }

    fun build(): SyncApiClient {
        val testJson = Json {
            useAlternativeNames = false
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        val mockEngine = MockEngine { request ->
            capturedRequests += request
            val path = request.url.encodedPath
            when {
                path.contains(pushPath) -> pushHandler(request)
                path.contains(configPath) -> fetchHandler(request)
                path.contains(photoPath) -> photoHandler(request)
                path.startsWith(photoDownloadPrefix) -> photoDownloadHandler(request)
                else -> respond("Unknown endpoint: ${request.url}", HttpStatusCode.NotFound)
            }
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(testJson) }
        }
        val ktorClient = KtorClientImpl(httpClient = httpClient, json = testJson)
        return SyncApiClientImpl(ktorClient = ktorClient)
    }

    companion object {
        fun MockRequestHandleScope.respondJson(
            body: String,
            status: HttpStatusCode = HttpStatusCode.OK,
        ): HttpResponseData = respond(
            content = body,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    }
}
