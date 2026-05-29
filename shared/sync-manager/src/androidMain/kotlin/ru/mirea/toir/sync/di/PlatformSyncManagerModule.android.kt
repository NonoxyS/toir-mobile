package ru.mirea.toir.sync.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.mirea.toir.sync.data.AndroidFileReader
import ru.mirea.toir.sync.data.AndroidPhotoFileWriter
import ru.mirea.toir.sync.data.FileReader
import ru.mirea.toir.sync.data.PhotoFileWriter
import ru.mirea.toir.sync.data.network.AndroidNetworkMonitor
import ru.mirea.toir.sync.domain.network.NetworkMonitor

internal actual val platformSyncManagerModule = module {
    single<NetworkMonitor> {
        AndroidNetworkMonitor(
            context = androidContext(),
            coroutineDispatchers = get(),
        )
    }
    single<PhotoFileWriter> { AndroidPhotoFileWriter(context = androidContext()) }
    single<FileReader> { AndroidFileReader(context = androidContext()) }
}
