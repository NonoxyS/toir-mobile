package ${packageName}.presentation.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ${packageName}.presentation.${featureName}ViewModel
import ${packageName}.presentation.mappers.Ui${featureName}LabelMapper
import ${packageName}.presentation.mappers.Ui${featureName}LabelMapperImpl
import ${packageName}.presentation.mappers.Ui${featureName}StateMapper
import ${packageName}.presentation.mappers.Ui${featureName}StateMapperImpl

val feature${featureName}PresentationModule = module {

    factoryOf<Ui${featureName}LabelMapper>(::Ui${featureName}LabelMapperImpl)

    factoryOf<Ui${featureName}StateMapper>(::Ui${featureName}StateMapperImpl)

    viewModelOf(::${featureName}ViewModel)
}
