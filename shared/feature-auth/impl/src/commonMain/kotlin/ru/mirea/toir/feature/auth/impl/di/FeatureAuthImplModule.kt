package ru.mirea.toir.feature.auth.impl.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.core.network.auth.TokenProvider
import ru.mirea.toir.feature.auth.impl.data.mappers.AuthUserMapper
import ru.mirea.toir.feature.auth.impl.data.mappers.AuthUserMapperImpl
import ru.mirea.toir.feature.auth.impl.data.network.AuthApiClient
import ru.mirea.toir.feature.auth.impl.data.network.AuthApiClientImpl
import ru.mirea.toir.feature.auth.impl.data.repository.AuthRepositoryImpl
import ru.mirea.toir.feature.auth.impl.data.storage.TokenStorage
import ru.mirea.toir.feature.auth.impl.data.storage.TokenStorageImpl
import ru.mirea.toir.feature.auth.impl.domain.AuthStoreFactory
import ru.mirea.toir.feature.auth.impl.domain.repository.AuthRepository

val featureAuthImplModule = module {
    factory<AuthApiClient> { new(::AuthApiClientImpl) }
    factory<TokenStorage> { new(::TokenStorageImpl) }
    factory<AuthUserMapper> { new(::AuthUserMapperImpl) }
    factory<AuthRepository> { new(::AuthRepositoryImpl) }
    factory { new(::AuthStoreFactory) }

    factory<TokenProvider> {
        object : TokenProvider {
            private val storage = get<TokenStorage>()
            private val repository = get<AuthRepository>()
            override suspend fun getAccessToken(): String? = storage.getAccessToken()
            override suspend fun refreshAndGetAccessToken(): String? {
                repository.refreshAccessToken()
                return storage.getAccessToken()
            }
        }
    }
}
