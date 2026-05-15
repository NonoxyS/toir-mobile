package ru.mirea.toir.feature.bootstrap.impl.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import ru.mirea.toir.feature.bootstrap.impl.data.network.BootstrapApiClient
import ru.mirea.toir.feature.bootstrap.impl.data.network.BootstrapApiClientImpl
import ru.mirea.toir.feature.bootstrap.impl.data.repository.BootstrapRepositoryImpl
import ru.mirea.toir.feature.bootstrap.impl.domain.BootstrapStoreFactory
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository
import ru.mirea.toir.sync.domain.SyncManager
import ru.mirea.toir.sync.domain.SyncTrigger

val featureBootstrapImplModule = module {
    factory<BootstrapApiClient> { new(::BootstrapApiClientImpl) }
    factory<BootstrapRepository> { new(::BootstrapRepositoryImpl) }

    factory<BootstrapStore> {
        val syncManager = get<SyncManager>()
        BootstrapStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            bootstrapRepository = get(),
            authRepository = get(),
            triggerBackgroundSync = { syncManager.syncNow(SyncTrigger.Bootstrap) },
        ).create()
    }
}
