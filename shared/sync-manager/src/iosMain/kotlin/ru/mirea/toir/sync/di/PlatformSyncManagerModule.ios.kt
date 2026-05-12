package ru.mirea.toir.sync.di

import org.koin.dsl.module
import ru.mirea.toir.sync.data.network.IosNetworkMonitor
import ru.mirea.toir.sync.domain.network.NetworkMonitor

internal actual val platformSyncManagerModule = module {
    single<NetworkMonitor> { IosNetworkMonitor() }
}
