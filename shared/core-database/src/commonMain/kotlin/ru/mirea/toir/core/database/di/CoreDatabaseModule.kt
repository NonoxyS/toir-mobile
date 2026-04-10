package ru.mirea.toir.core.database.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.dao.ActionLogDao
import ru.mirea.toir.core.database.dao.ChecklistDao
import ru.mirea.toir.core.database.dao.EquipmentDao
import ru.mirea.toir.core.database.dao.InspectionDao
import ru.mirea.toir.core.database.dao.PhotoDao
import ru.mirea.toir.core.database.dao.RouteDao
import ru.mirea.toir.core.database.dao.SyncMetaDao
import ru.mirea.toir.core.database.dao.UserDao

internal expect val platformCoreDatabaseModule: Module

val coreDatabaseModule = module {
    includes(platformCoreDatabaseModule)

    single<ToirDatabase> {
        ToirDatabase(
        driver = get<ru.mirea.toir.core.database.driver.DatabaseDriverFactory>().create()
    )
    }

    factory { new(::UserDao) }
    factory { new(::EquipmentDao) }
    factory { new(::RouteDao) }
    factory { new(::InspectionDao) }
    factory { new(::ChecklistDao) }
    factory { new(::PhotoDao) }
    factory { new(::ActionLogDao) }
    factory { new(::SyncMetaDao) }
}
