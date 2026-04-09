package ru.mirea.toir.common.resources.di

import ru.mirea.toir.common.resources.AndroidStringConverter
import ru.mirea.toir.common.resources.StringConverter
import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.dsl.module

internal actual val platformResourcesModule: Module = module {

    factory<StringConverter> { new(::AndroidStringConverter) }
}