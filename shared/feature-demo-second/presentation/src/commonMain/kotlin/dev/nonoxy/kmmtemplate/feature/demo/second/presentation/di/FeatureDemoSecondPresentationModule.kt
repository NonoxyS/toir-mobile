package dev.nonoxy.kmmtemplate.feature.demo.second.presentation.di

import dev.nonoxy.kmmtemplate.feature.demo.second.presentation.DemoFeatureSecondViewModel
import dev.nonoxy.kmmtemplate.feature.demo.second.presentation.mappers.UiDemoFeatureSecondLabelMapper
import dev.nonoxy.kmmtemplate.feature.demo.second.presentation.mappers.UiDemoFeatureSecondLabelMapperImpl
import dev.nonoxy.kmmtemplate.feature.demo.second.presentation.mappers.UiDemoFeatureSecondStateMapper
import dev.nonoxy.kmmtemplate.feature.demo.second.presentation.mappers.UiDemoFeatureSecondStateMapperImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureDemoSecondPresentationModule = module {

    factoryOf<UiDemoFeatureSecondLabelMapper>(::UiDemoFeatureSecondLabelMapperImpl)

    factoryOf<UiDemoFeatureSecondStateMapper>(::UiDemoFeatureSecondStateMapperImpl)

    viewModelOf(::DemoFeatureSecondViewModel)
}
