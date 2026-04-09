package dev.nonoxy.kmmtemplate.core.network.di

import dev.nonoxy.kmmtemplate.core.network.ktor.NetworkEnvironment
import org.koin.core.component.KoinComponent

class EnvironmentDi : KoinComponent {

    fun insertKoin(environment: NetworkEnvironment) {
        getKoin().declare<NetworkEnvironment>(environment)
    }
}
