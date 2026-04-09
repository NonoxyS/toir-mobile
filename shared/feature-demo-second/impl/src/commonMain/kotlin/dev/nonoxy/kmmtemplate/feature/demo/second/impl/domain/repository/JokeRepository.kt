package dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.repository

internal interface JokeRepository {

    suspend fun fetchJoke(): Result<String>
}
