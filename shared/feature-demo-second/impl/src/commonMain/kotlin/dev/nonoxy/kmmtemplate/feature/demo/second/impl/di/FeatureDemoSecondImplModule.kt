package dev.nonoxy.kmmtemplate.feature.demo.second.impl.di

import dev.nonoxy.kmmtemplate.feature.demo.second.api.store.DemoFeatureSecondStore
import dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.JokeApiClient
import dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.JokeApiClientImpl
import dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.repository.JokeRepositoryImpl
import dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory
import dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.repository.JokeRepository
import dev.nonoxy.kmmtemplate.common.coroutines.CoroutineDispatchers
import org.koin.dsl.module

val featureDemoSecondImplModule = module {

    factory<dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.JokeApiClient> {
        _root_ide_package_.dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.JokeApiClientImpl(ktorClient = get())
    }

    factory<dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.repository.JokeRepository> {
        _root_ide_package_.dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.repository.JokeRepositoryImpl(
            apiClient = get(),
            coroutineDispatchers = get(),
        )
    }

    factory<DemoFeatureSecondStore> {
        _root_ide_package_.dev.nonoxy.kmmtemplate.feature.demo.second.impl.domain.DemoFeatureSecondStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            jokeRepository = get(),
        ).create()
    }
}
