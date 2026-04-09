package dev.nonoxy.kmmtemplate.feature.demo.first.impl.di

import dev.nonoxy.kmmtemplate.feature.demo.first.api.store.DemoFeatureFirstStore
import dev.nonoxy.kmmtemplate.feature.demo.first.impl.domain.DemoFeatureFirstStoreFactory
import dev.nonoxy.kmmtemplate.common.coroutines.CoroutineDispatchers
import org.koin.dsl.module

val featureDemoFirstImplModule = module {

    factory<DemoFeatureFirstStore> {
        DemoFeatureFirstStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main
        ).create()
    }
}
