package dev.nonoxy.kmmtemplate.core.mvikotlin.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.logging.logger.Logger
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.github.aakira.napier.Napier
import org.koin.dsl.module

val coreMVIKotlinModule = module {

    factory<StoreFactory> {
        val logger = object : Logger {
            override fun log(text: String) {
                Napier.v(text)
            }
        }

        LoggingStoreFactory(delegate = DefaultStoreFactory(), logger = logger)
    }
}
