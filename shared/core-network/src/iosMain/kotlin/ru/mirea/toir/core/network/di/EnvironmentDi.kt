package ru.mirea.toir.core.network.di

import ru.mirea.toir.core.network.ktor.NetworkEnvironment
import org.koin.core.component.KoinComponent

class EnvironmentDi : KoinComponent {

    fun insertKoin(environment: NetworkEnvironment) {
        getKoin().declare<NetworkEnvironment>(environment)
    }
}
