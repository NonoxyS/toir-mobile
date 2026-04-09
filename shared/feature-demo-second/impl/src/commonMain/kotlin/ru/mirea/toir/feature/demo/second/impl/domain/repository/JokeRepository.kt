package ru.mirea.toir.feature.demo.second.impl.domain.repository

internal interface JokeRepository {

    suspend fun fetchJoke(): Result<String>
}
