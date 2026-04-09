package ru.mirea.toir.feature.demo.second.impl.data.network

import ru.mirea.toir.feature.demo.second.impl.data.network.models.RemoteJokeResponse

internal interface JokeApiClient {

    suspend fun fetchJoke(): Result<ru.mirea.toir.feature.demo.second.impl.data.network.models.RemoteJokeResponse>

    companion object {
        internal const val URL_JOKE = "https://v2.jokeapi.dev/joke/Any?type=single"
    }
}
