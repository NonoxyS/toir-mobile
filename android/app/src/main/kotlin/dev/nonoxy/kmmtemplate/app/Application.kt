package dev.nonoxy.kmmtemplate.app

import android.app.Application
import dev.nonoxy.kmmtemplate.BuildConfig
import dev.nonoxy.kmmtemplate.di.appModule
import dev.nonoxy.kmmtemplate.di.initKoin
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
            Napier.base(DebugAntilog(defaultTag = "KMMTemplate"))
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