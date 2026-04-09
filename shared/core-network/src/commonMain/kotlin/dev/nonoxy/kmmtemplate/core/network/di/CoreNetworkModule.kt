package dev.nonoxy.kmmtemplate.core.network.di

import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformCoreNetworkModule: Module

val coreNetworkModule = module {
    includes(platformCoreNetworkModule)
}
