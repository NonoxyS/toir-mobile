package ru.mirea.toir.di

import ru.mirea.toir.core.network.di.EnvironmentDi
import ru.mirea.toir.core.network.ktor.NetworkEnvironment

fun initKoinIos(environment: String) {
    initKoin {}

    val networkEnvironment = when (environment) {
        "dev" -> NetworkEnvironment.Dev
        else -> NetworkEnvironment.Prod
    }
    EnvironmentDi().insertKoin(networkEnvironment)
}
