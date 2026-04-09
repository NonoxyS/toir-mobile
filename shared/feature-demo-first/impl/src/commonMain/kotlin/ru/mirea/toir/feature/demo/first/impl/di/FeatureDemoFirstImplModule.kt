package ru.mirea.toir.feature.demo.first.impl.di

import ru.mirea.toir.feature.demo.first.api.store.DemoFeatureFirstStore
import ru.mirea.toir.feature.demo.first.impl.domain.DemoFeatureFirstStoreFactory
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import org.koin.dsl.module

val featureDemoFirstImplModule = module {

    factory<DemoFeatureFirstStore> {
        DemoFeatureFirstStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main
        ).create()
    }
}
