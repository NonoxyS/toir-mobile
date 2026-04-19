package ru.mirea.toir.app

import android.app.Application
import ru.mirea.toir.BuildConfig
import ru.mirea.toir.di.appModule
import ru.mirea.toir.di.initKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import ru.mirea.toir.sync.worker.SyncScheduler

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        setupKoin()
        setupLogging()
    }

    private fun setupLogging() {
        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog(defaultTag = "Toir"))
        }
    }

    private fun setupKoin() {
        initKoin {
            androidContext(this@Application)
            workManagerFactory()
            modules(
                appModule,
            )
        }
        SyncScheduler.schedule(this)
    }
}
