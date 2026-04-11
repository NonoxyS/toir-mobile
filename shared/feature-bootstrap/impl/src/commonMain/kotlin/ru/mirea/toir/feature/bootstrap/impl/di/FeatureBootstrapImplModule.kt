package ru.mirea.toir.feature.bootstrap.impl.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.feature.bootstrap.impl.data.network.BootstrapApiClient
import ru.mirea.toir.feature.bootstrap.impl.data.network.BootstrapApiClientImpl
import ru.mirea.toir.feature.bootstrap.impl.data.repository.BootstrapRepositoryImpl
import ru.mirea.toir.feature.bootstrap.impl.domain.BootstrapStoreFactory
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository

val featureBootstrapImplModule = module {
    factory<BootstrapApiClient> { new(::BootstrapApiClientImpl) }
    factory<BootstrapRepository> { new(::BootstrapRepositoryImpl) }
    factory { new(::BootstrapStoreFactory) }
}
