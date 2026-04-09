package dev.nonoxy.kmmtemplate.common.resources.di

import dev.nonoxy.kmmtemplate.common.resources.IosStringConverter
import dev.nonoxy.kmmtemplate.common.resources.StringConverter
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal actual val platformResourcesModule: Module = module {

    factoryOf<StringConverter>(::IosStringConverter)
}