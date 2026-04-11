package ru.mirea.toir.feature.auth.impl.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.core.auth.data.storage.TokenStorage
import ru.mirea.toir.core.auth.domain.repository.AuthRepository
import ru.mirea.toir.feature.auth.api.store.AuthStore
import ru.mirea.toir.feature.auth.impl.data.mappers.AuthUserMapper
import ru.mirea.toir.feature.auth.impl.data.mappers.AuthUserMapperImpl
import ru.mirea.toir.feature.auth.impl.data.network.AuthApiClient
import ru.mirea.toir.feature.auth.impl.data.network.AuthApiClientImpl
import ru.mirea.toir.feature.auth.impl.data.repository.AuthRepositoryImpl
import ru.mirea.toir.feature.auth.impl.data.storage.TokenStorageImpl
import ru.mirea.toir.feature.auth.impl.domain.AuthStoreFactory

val featureAuthImplModule = module {
    factory<AuthApiClient> { new(::AuthApiClientImpl) }
    factory<TokenStorage> { new(::TokenStorageImpl) }
    factory<AuthUserMapper> { new(::AuthUserMapperImpl) }
    factory<AuthRepository> { new(::AuthRepositoryImpl) }

    factory<AuthStore> {
        AuthStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            authRepository = get()
        ).create()
    }
}
