package ru.mirea.toir.feature.bootstrap.impl.domain.repository

internal interface BootstrapRepository {
    suspend fun loadAndSaveBootstrap(): Result<Unit>
}
