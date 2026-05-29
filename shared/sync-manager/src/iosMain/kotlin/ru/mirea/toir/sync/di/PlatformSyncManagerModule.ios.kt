package ru.mirea.toir.sync.di

import org.koin.dsl.module
import ru.mirea.toir.sync.data.FileReader
import ru.mirea.toir.sync.data.IosFileReader
import ru.mirea.toir.sync.data.IosPhotoFileWriter
import ru.mirea.toir.sync.data.PhotoFileWriter
import ru.mirea.toir.sync.data.network.IosNetworkMonitor
import ru.mirea.toir.sync.domain.network.NetworkMonitor

internal actual val platformSyncManagerModule = module {
    single<NetworkMonitor> { IosNetworkMonitor() }
    single<PhotoFileWriter> { IosPhotoFileWriter() }
    single<FileReader> { IosFileReader() }
}
