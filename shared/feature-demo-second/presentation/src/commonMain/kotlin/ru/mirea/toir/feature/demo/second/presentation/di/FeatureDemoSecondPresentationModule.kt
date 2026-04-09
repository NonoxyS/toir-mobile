package ru.mirea.toir.feature.demo.second.presentation.di

import ru.mirea.toir.feature.demo.second.presentation.DemoFeatureSecondViewModel
import ru.mirea.toir.feature.demo.second.presentation.mappers.UiDemoFeatureSecondLabelMapper
import ru.mirea.toir.feature.demo.second.presentation.mappers.UiDemoFeatureSecondLabelMapperImpl
import ru.mirea.toir.feature.demo.second.presentation.mappers.UiDemoFeatureSecondStateMapper
import ru.mirea.toir.feature.demo.second.presentation.mappers.UiDemoFeatureSecondStateMapperImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureDemoSecondPresentationModule = module {

    factoryOf<UiDemoFeatureSecondLabelMapper>(::UiDemoFeatureSecondLabelMapperImpl)

    factoryOf<UiDemoFeatureSecondStateMapper>(::UiDemoFeatureSecondStateMapperImpl)

    viewModelOf(::DemoFeatureSecondViewModel)
}
