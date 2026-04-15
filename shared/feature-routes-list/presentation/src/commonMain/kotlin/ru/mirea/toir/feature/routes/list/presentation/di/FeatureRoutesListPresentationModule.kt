package ru.mirea.toir.feature.routes.list.presentation.di

import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.mirea.toir.feature.routes.list.presentation.RoutesListViewModel
import ru.mirea.toir.feature.routes.list.presentation.mappers.UiRoutesListLabelMapper
import ru.mirea.toir.feature.routes.list.presentation.mappers.UiRoutesListLabelMapperImpl
import ru.mirea.toir.feature.routes.list.presentation.mappers.UiRoutesListStateMapper
import ru.mirea.toir.feature.routes.list.presentation.mappers.UiRoutesListStateMapperImpl

val featureRoutesListPresentationModule = module {
    factory<UiRoutesListStateMapper> { new(::UiRoutesListStateMapperImpl) }
    factory<UiRoutesListLabelMapper> { new(::UiRoutesListLabelMapperImpl) }
    viewModelOf(::RoutesListViewModel)
}
