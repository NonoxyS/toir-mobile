package ru.mirea.toir.sync.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.sync.SyncManager
import ru.mirea.toir.sync.SyncRepository
import ru.mirea.toir.sync.SyncRepositoryImpl
import ru.mirea.toir.sync.api.SyncApiClient
import ru.mirea.toir.sync.api.SyncApiClientImpl

val syncManagerModule = module {
    factory<SyncApiClient> { new(::SyncApiClientImpl) }
    factory<SyncRepository> { new(::SyncRepositoryImpl) }
    single { new(::SyncManager) }
}
