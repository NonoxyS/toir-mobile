package dev.nonoxy.kmmtemplate.core.network.ktor

import dev.nonoxy.kmmtemplate.common.extensions.coRunCatching
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.http.content.PartData
import io.ktor.http.isSuccess
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

internal class KtorClientImpl(
    private val httpClient: HttpClient,
    private val json: Json,
) : KtorClient {

    override suspend fun <R, D> executeQuery(
        query: suspend () -> HttpResponse,
        deserializer: DeserializationStrategy<D>?,
        success: (D) -> Result<R>,
        loggingErrorMessage: String,
    ): Result<R> {

        fun <R> responseFailedWithLog(loggingErrorMessage: String, statusCode: Int): Result<R> {
            val exception = Exception(loggingErrorMessage)
            Napier.e(message = loggingErrorMessage)
            return Result.failure(exception)
        }

        return coRunCatching(
            tryBlock = {
                val httpResponse = query()

                if (httpResponse.status.isSuccess()) {
                    val body = deserializer?.let {
                        json.decodeFromString(
                            deserializer = it,
                            string = httpResponse.bodyAsText(),
                        )
                    }

                    when (body) {
                        null -> responseFailedWithLog(
                            loggingErrorMessage = "$loggingErrorMessage: body=$body",
                            statusCode = httpResponse.status.value
                        )

                        else -> success(body)
                    }
                } else {
                    responseFailedWithLog(
                        loggingErrorMessage = "$loggingErrorMessage: status=${httpResponse.status}",
                        statusCode = httpResponse.status.value
                    )
                }
            },
            catchBlock = { throwable ->
                Napier.e(message = loggingErrorMessage, throwable = throwable)
                Result.failure(throwable)
            }
        )
    }

    override suspend fun get(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit
    ): HttpResponse {
        return httpClient.get(urlString = urlString, block = block)
    }

    override suspend fun post(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit
    ): HttpResponse {
        return httpClient.post(urlString = urlString, block = block)
    }

    override suspend fun submitForm(
        urlString: String,
        formParameters: Parameters,
        block: HttpRequestBuilder.() -> Unit
    ): HttpResponse {
        return httpClient.submitForm(
            url = urlString,
            formParameters = formParameters,
            block = block
        )
    }

    override suspend fun submitFormWithBinaryData(
        urlString: String,
        formData: List<PartData>,
        block: HttpRequestBuilder.() -> Unit
    ): HttpResponse {
        return httpClient.submitFormWithBinaryData(
            url = urlString,
            formData = formData,
            block = block
        )
    }
}
