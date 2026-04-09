package ru.mirea.toir.core.storage.di

import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformCoreStorageModule: Module

val coreStorageModule = module {
    includes(platformCoreStorageModule)
}
