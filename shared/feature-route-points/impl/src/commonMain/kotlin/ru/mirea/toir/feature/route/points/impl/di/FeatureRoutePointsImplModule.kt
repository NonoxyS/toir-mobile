package ru.mirea.toir.feature.route.points.impl.di

import org.koin.dsl.module
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore
import ru.mirea.toir.feature.route.points.impl.data.repository.RoutePointsRepositoryImpl
import ru.mirea.toir.feature.route.points.impl.domain.RoutePointsStoreFactory
import ru.mirea.toir.feature.route.points.impl.domain.repository.RoutePointsRepository

val featureRoutePointsImplModule = module {
    factory<RoutePointsRepository> {
        RoutePointsRepositoryImpl(
            inspectionStorage = get(),
            routeStorage = get(),
            equipmentStorage = get(),
            coroutineDispatchers = get(),
        )
    }

    factory<RoutePointsStore> {
        RoutePointsStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            repository = get(),
        ).create()
    }
}
