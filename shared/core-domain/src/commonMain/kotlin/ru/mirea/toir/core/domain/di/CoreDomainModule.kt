package ru.mirea.toir.core.domain.di

import kotlinx.serialization.json.Json
import org.koin.dsl.module

val coreDomainModule = module {

    factory {
        Json { ignoreUnknownKeys = true }
    }
}
