package ru.mirea.toir.core.database.di

import org.koin.dsl.module
import ru.mirea.toir.core.database.driver.DatabaseDriverFactory

internal actual val platformCoreDatabaseModule = module {
    factory { DatabaseDriverFactory() }
}
