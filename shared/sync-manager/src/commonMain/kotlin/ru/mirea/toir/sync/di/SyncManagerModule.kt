package ru.mirea.toir.sync.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.sync.domain.SyncManager
import ru.mirea.toir.sync.domain.repository.SyncRepository
import ru.mirea.toir.sync.data.repository.SyncRepositoryImpl
import ru.mirea.toir.sync.data.network.SyncApiClient
import ru.mirea.toir.sync.data.network.SyncApiClientImpl

val syncManagerModule = module {
    factory<SyncApiClient> { new(::SyncApiClientImpl) }
    factory<SyncRepository> { new(::SyncRepositoryImpl) }
    single { new(::SyncManager) }
}
