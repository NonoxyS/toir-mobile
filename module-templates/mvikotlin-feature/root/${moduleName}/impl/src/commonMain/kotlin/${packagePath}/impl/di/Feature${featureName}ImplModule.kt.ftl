package ${packageName}.impl.di

import dev.nonoxy.kmmtemplate.common.coroutines.CoroutineDispatchers
import org.koin.dsl.module
import ${packageName}.api.${featureName}Store
import ${packageName}.impl.domain.${featureName}StoreFactory

val feature${featureName}ImplModule = module {

    factory<${featureName}Store> {
        ${featureName}StoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main
        ).create()
    }
}
