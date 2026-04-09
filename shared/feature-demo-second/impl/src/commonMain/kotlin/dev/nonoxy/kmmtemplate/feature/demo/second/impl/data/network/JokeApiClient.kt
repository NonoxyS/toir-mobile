package dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network

import dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.models.RemoteJokeResponse

internal interface JokeApiClient {

    suspend fun fetchJoke(): Result<dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.models.RemoteJokeResponse>

    companion object {
        internal const val URL_JOKE = "https://v2.jokeapi.dev/joke/Any?type=single"
    }
}
