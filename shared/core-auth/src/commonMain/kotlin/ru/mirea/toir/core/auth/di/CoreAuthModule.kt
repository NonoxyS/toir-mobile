package ru.mirea.toir.core.auth.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.core.auth.token.TokenProvider
import ru.mirea.toir.core.auth.token.TokenProviderImpl

val coreAuthModule = module {
    factory<TokenProvider> { new(::TokenProviderImpl) }
}
