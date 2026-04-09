package ru.mirea.toir.common.resources.di

import ru.mirea.toir.common.resources.IosStringConverter
import ru.mirea.toir.common.resources.StringConverter
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal actual val platformResourcesModule: Module = module {

    factoryOf<StringConverter>(::IosStringConverter)
}