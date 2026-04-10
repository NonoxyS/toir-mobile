package ru.mirea.toir.app

import android.app.Application
import ru.mirea.toir.BuildConfig
import ru.mirea.toir.di.appModule
import ru.mirea.toir.di.initKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext

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
            modules(
                appModule,
            )
        }
    }
}
