package ru.mirea.toir.feature.bootstrap.impl.domain

import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapResult

internal class FakeBootstrapRepository(
    var nextResult: BootstrapResult = BootstrapResult.Success,
) : BootstrapRepository {
    var callCount: Int = 0
        private set

    override suspend fun loadAndSaveBootstrap(): BootstrapResult {
        callCount++
        return nextResult
    }
}
