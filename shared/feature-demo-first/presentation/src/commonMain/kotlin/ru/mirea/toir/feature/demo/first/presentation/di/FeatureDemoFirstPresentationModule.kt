package ru.mirea.toir.feature.demo.first.presentation.di

import ru.mirea.toir.feature.demo.first.presentation.DemoFeatureFirstViewModel
import ru.mirea.toir.feature.demo.first.presentation.mappers.UiDemoFeatureFirstLabelMapper
import ru.mirea.toir.feature.demo.first.presentation.mappers.UiDemoFeatureFirstLabelMapperImpl
import ru.mirea.toir.feature.demo.first.presentation.mappers.UiDemoFeatureFirstStateMapper
import ru.mirea.toir.feature.demo.first.presentation.mappers.UiDemoFeatureFirstStateMapperImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureDemoFirstPresentationModule = module {

    factoryOf<UiDemoFeatureFirstLabelMapper>(::UiDemoFeatureFirstLabelMapperImpl)

    factoryOf<UiDemoFeatureFirstStateMapper>(::UiDemoFeatureFirstStateMapperImpl)

    viewModelOf(::DemoFeatureFirstViewModel)
}
