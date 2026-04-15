package ru.mirea.toir.feature.routes.list.impl.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import ru.mirea.toir.feature.routes.list.impl.data.mappers.RouteAssignmentMapper
import ru.mirea.toir.feature.routes.list.impl.data.repository.RoutesListRepositoryImpl
import ru.mirea.toir.feature.routes.list.impl.domain.RoutesListStoreFactory
import ru.mirea.toir.feature.routes.list.impl.domain.repository.RoutesListRepository

val featureRoutesListImplModule = module {
    factory { new(::RouteAssignmentMapper) }
    factory<RoutesListRepository> { new(::RoutesListRepositoryImpl) }

    factory<RoutesListStore> {
        RoutesListStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            repository = get()
        ).create()
    }
}
