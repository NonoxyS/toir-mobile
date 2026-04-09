package dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.repository

import dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.JokeApiClient
import dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.repository.JokeRepository
import dev.nonoxy.kmmtemplate.common.coroutines.CoroutineDispatchers
import dev.nonoxy.kmmtemplate.common.extensions.coRunCatching
import dev.nonoxy.kmmtemplate.common.extensions.wrapResultFailure
import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext

internal class JokeRepositoryImpl(
    private val apiClient: dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.JokeApiClient,
    private val coroutineDispatchers: CoroutineDispatchers,
) : dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.repository.JokeRepository {

    override suspend fun fetchJoke(): Result<String> = withContext(coroutineDispatchers.io) {
        coRunCatching(
            tryBlock = {
                apiClient.fetchJoke().map { it.joke }
            },
            catchBlock = { throwable ->
                Napier.e(message = "Error occur during fetch joke")
                throwable.wrapResultFailure()
            }
        )
    }
}
