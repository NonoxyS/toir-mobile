package ru.mirea.toir.core.network.di

import ru.mirea.toir.common.di.AppEnvironmentQualifiers
import ru.mirea.toir.core.network.ktor.NetworkEnvironment
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal actual val platformCoreNetworkModule = module {

    factory<NetworkEnvironment> {
        val flavor: String = get(named(AppEnvironmentQualifiers.FLAVOR))

        when (flavor.lowercase()) {
            "prod" -> NetworkEnvironment.Prod
            "dev" -> NetworkEnvironment.Dev
            else -> error("Unknown flavor type! Do you forget include new in platformCoreNetworkModule?")
        }
    }
}