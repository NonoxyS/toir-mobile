package dev.nonoxy.kmmtemplate.feature.demo.first.presentation.di

import dev.nonoxy.kmmtemplate.feature.demo.first.presentation.DemoFeatureFirstViewModel
import dev.nonoxy.kmmtemplate.feature.demo.first.presentation.mappers.UiDemoFeatureFirstLabelMapper
import dev.nonoxy.kmmtemplate.feature.demo.first.presentation.mappers.UiDemoFeatureFirstLabelMapperImpl
import dev.nonoxy.kmmtemplate.feature.demo.first.presentation.mappers.UiDemoFeatureFirstStateMapper
import dev.nonoxy.kmmtemplate.feature.demo.first.presentation.mappers.UiDemoFeatureFirstStateMapperImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureDemoFirstPresentationModule = module {

    factoryOf<UiDemoFeatureFirstLabelMapper>(::UiDemoFeatureFirstLabelMapperImpl)

    factoryOf<UiDemoFeatureFirstStateMapper>(::UiDemoFeatureFirstStateMapperImpl)

    viewModelOf(::DemoFeatureFirstViewModel)
}
