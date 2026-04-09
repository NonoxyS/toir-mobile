package ru.mirea.toir.feature.demo.second.impl.data.repository

import ru.mirea.toir.feature.demo.second.impl.data.network.JokeApiClient
import ru.mirea.toir.feature.demo.second.impl.domain.repository.JokeRepository
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext

internal class JokeRepositoryImpl(
    private val apiClient: ru.mirea.toir.feature.demo.second.impl.data.network.JokeApiClient,
    private val coroutineDispatchers: CoroutineDispatchers,
) : ru.mirea.toir.feature.demo.second.impl.domain.repository.JokeRepository {

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
