package dev.nonoxy.kmmtemplate.common.resources.di

import dev.nonoxy.kmmtemplate.common.resources.AndroidStringConverter
import dev.nonoxy.kmmtemplate.common.resources.StringConverter
import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.dsl.module

internal actual val platformResourcesModule: Module = module {

    factory<StringConverter> { new(::AndroidStringConverter) }
}