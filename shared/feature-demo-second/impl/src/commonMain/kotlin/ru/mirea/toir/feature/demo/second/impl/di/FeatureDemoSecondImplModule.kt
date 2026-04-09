package ru.mirea.toir.feature.demo.second.impl.di

import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore
import ru.mirea.toir.feature.demo.second.impl.data.network.JokeApiClient
import ru.mirea.toir.feature.demo.second.impl.data.network.JokeApiClientImpl
import ru.mirea.toir.feature.demo.second.impl.data.repository.JokeRepositoryImpl
import ru.mirea.toir.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory
import ru.mirea.toir.feature.demo.second.impl.domain.repository.JokeRepository
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import org.koin.dsl.module

val featureDemoSecondImplModule = module {

    factory<ru.mirea.toir.feature.demo.second.impl.data.network.JokeApiClient> {
        _root_ide_package_.ru.mirea.toir.feature.demo.second.impl.data.network.JokeApiClientImpl(ktorClient = get())
    }

    factory<ru.mirea.toir.feature.demo.second.impl.domain.repository.JokeRepository> {
        _root_ide_package_.ru.mirea.toir.feature.demo.second.impl.data.repository.JokeRepositoryImpl(
            apiClient = get(),
            coroutineDispatchers = get(),
        )
    }

    factory<DemoFeatureSecondStore> {
        _root_ide_package_.ru.mirea.toir.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            jokeRepository = get(),
        ).create()
    }
}
