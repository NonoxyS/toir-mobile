package ru.mirea.toir.core.network.ktor.di

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.core.auth.domain.models.BearerTokens as DomainBearerTokens
import ru.mirea.toir.core.auth.domain.repository.AuthRepository
import ru.mirea.toir.core.domain.exception.NetworkUnavailableException
import ru.mirea.toir.core.network.ktor.KtorClient
import ru.mirea.toir.core.network.ktor.KtorClientImpl
import ru.mirea.toir.core.network.ktor.NetworkEnvironment
import ru.mirea.toir.core.network.ktor.certificates.configureCertificates

val coreNetworkKtorModule = module {

    single<HttpClient> {
        httpClient(environment = get()) {
            initBaseHttpConfig()
            installBearerAuth(authRepository = get())
            defaultRequest { setTemplateApiHost(environment = get()) }
        }
    }

    factory<KtorClient> {
        new(::KtorClientImpl)
    }
}

private fun HttpClientConfig<*>.initBaseHttpConfig() {
    install(Logging) {
        level = LogLevel.ALL
        logger = object : Logger {
            override fun log(message: String) {
                Napier.d(tag = "HTTP", message = message)
            }
        }
    }

    install(ContentNegotiation) {
        val json = Json {
            useAlternativeNames = false
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        json(json)
    }

    HttpResponseValidator {
        handleResponseExceptionWithRequest { throwable, request ->
            when (throwable) {
                is IOException -> throw NetworkUnavailableException(throwable.message.orEmpty())
                else -> throw throwable
            }
        }
    }
}

private fun HttpClientConfig<*>.installBearerAuth(authRepository: AuthRepository) {
    install(Auth) {
        bearer {
            loadTokens {
                authRepository.getBearerTokens().getOrNull()?.toKtor()
            }
            refreshTokens {
                authRepository.refreshBearerTokens().fold(
                    onSuccess = { it.toKtor() },
                    onFailure = { null },
                )
            }
        }
    }
}

private fun DomainBearerTokens.toKtor() = BearerTokens(
    accessToken = accessToken.value,
    refreshToken = refreshToken.value,
)

private fun httpClient(
    environment: NetworkEnvironment,
    config: HttpClientConfig<*>.() -> Unit,
): HttpClient = HttpClient {
    config(this)

    if (environment == NetworkEnvironment.Dev) {
        configureCertificates()
    }
}

private fun DefaultRequest.DefaultRequestBuilder.setTemplateApiHost(environment: NetworkEnvironment) {
    url.protocol = environment.protocol
    url.host = environment.apiHost
}
