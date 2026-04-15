package ru.mirea.toir.core.database.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.core.database.Inspections
import ru.mirea.toir.core.database.Route_assignments
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.adapters.EnumColumnAdapter
import ru.mirea.toir.core.database.dao.ActionLogDao
import ru.mirea.toir.core.database.dao.ChecklistDao
import ru.mirea.toir.core.database.dao.EquipmentDao
import ru.mirea.toir.core.database.dao.InspectionDao
import ru.mirea.toir.core.database.dao.PhotoDao
import ru.mirea.toir.core.database.dao.RouteDao
import ru.mirea.toir.core.database.dao.SyncMetaDao
import ru.mirea.toir.core.database.dao.UserDao
import ru.mirea.toir.core.database.driver.DatabaseDriverFactory
import ru.mirea.toir.core.database.models.LocalRouteStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus

internal expect val platformCoreDatabaseModule: Module

val coreDatabaseModule = module {
    includes(platformCoreDatabaseModule)

    single<ToirDatabase> {
        ToirDatabase(
            driver = get<DatabaseDriverFactory>().create(),
            route_assignmentsAdapter = Route_assignments.Adapter(
                statusAdapter = EnumColumnAdapter { LocalRouteStatus.fromString(it) },
            ),
            inspectionsAdapter = Inspections.Adapter(
                statusAdapter = EnumColumnAdapter { LocalRouteStatus.fromString(it) },
                sync_statusAdapter = EnumColumnAdapter { LocalSyncStatus.fromString(it) },
            ),
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
