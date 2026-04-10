package ru.mirea.toir.feature.auth.presentation.di

import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.mirea.toir.feature.auth.presentation.AuthViewModel
import ru.mirea.toir.feature.auth.presentation.mappers.UiAuthLabelMapper
import ru.mirea.toir.feature.auth.presentation.mappers.UiAuthLabelMapperImpl
import ru.mirea.toir.feature.auth.presentation.mappers.UiAuthStateMapper
import ru.mirea.toir.feature.auth.presentation.mappers.UiAuthStateMapperImpl

val featureAuthPresentationModule = module {
    factory<UiAuthStateMapper> { new(::UiAuthStateMapperImpl) }
    factory<UiAuthLabelMapper> { new(::UiAuthLabelMapperImpl) }
    viewModelOf(::AuthViewModel)
}
