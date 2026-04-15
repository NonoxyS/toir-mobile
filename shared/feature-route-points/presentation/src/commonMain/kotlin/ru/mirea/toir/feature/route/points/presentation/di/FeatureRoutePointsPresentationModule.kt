package ru.mirea.toir.feature.route.points.presentation.di

import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.mirea.toir.feature.route.points.presentation.RoutePointsViewModel
import ru.mirea.toir.feature.route.points.presentation.mappers.UiRoutePointsLabelMapper
import ru.mirea.toir.feature.route.points.presentation.mappers.UiRoutePointsLabelMapperImpl
import ru.mirea.toir.feature.route.points.presentation.mappers.UiRoutePointsStateMapper
import ru.mirea.toir.feature.route.points.presentation.mappers.UiRoutePointsStateMapperImpl

val featureRoutePointsPresentationModule = module {
    factory<UiRoutePointsStateMapper> { new(::UiRoutePointsStateMapperImpl) }
    factory<UiRoutePointsLabelMapper> { new(::UiRoutePointsLabelMapperImpl) }
    viewModelOf(::RoutePointsViewModel)
}
