package ru.mirea.toir.feature.bootstrap.presentation.di

import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.mirea.toir.feature.bootstrap.presentation.BootstrapViewModel
import ru.mirea.toir.feature.bootstrap.presentation.mappers.UiBootstrapLabelMapper
import ru.mirea.toir.feature.bootstrap.presentation.mappers.UiBootstrapLabelMapperImpl
import ru.mirea.toir.feature.bootstrap.presentation.mappers.UiBootstrapStateMapper
import ru.mirea.toir.feature.bootstrap.presentation.mappers.UiBootstrapStateMapperImpl

val featureBootstrapPresentationModule = module {
    factory<UiBootstrapStateMapper> { new(::UiBootstrapStateMapperImpl) }
    factory<UiBootstrapLabelMapper> { new(::UiBootstrapLabelMapperImpl) }
    viewModelOf(::BootstrapViewModel)
}
