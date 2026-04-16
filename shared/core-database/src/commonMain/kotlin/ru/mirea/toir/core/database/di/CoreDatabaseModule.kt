package ru.mirea.toir.core.database.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.core.database.Inspection_equipment_results
import ru.mirea.toir.core.database.Inspections
import ru.mirea.toir.core.database.Route_assignments
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.adapters.EnumColumnAdapter
import ru.mirea.toir.core.database.driver.DatabaseDriverFactory
import ru.mirea.toir.core.database.models.LocalRouteStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.action_log.ActionLogStorage
import ru.mirea.toir.core.database.storage.action_log.ActionLogStorageImpl
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorage
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorageImpl
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorage
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorageImpl
import ru.mirea.toir.core.database.storage.inspection.InspectionStorage
import ru.mirea.toir.core.database.storage.inspection.InspectionStorageImpl
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.photo.PhotoStorage
import ru.mirea.toir.core.database.storage.photo.PhotoStorageImpl
import ru.mirea.toir.core.database.storage.route.RouteStorage
import ru.mirea.toir.core.database.storage.route.RouteStorageImpl
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorage
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorageImpl
import ru.mirea.toir.core.database.storage.user.UserStorage
import ru.mirea.toir.core.database.storage.user.UserStorageImpl

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
            inspection_equipment_resultsAdapter = Inspection_equipment_results.Adapter(
                statusAdapter = EnumColumnAdapter { LocalEquipmentResultStatus.fromString(it) },
                sync_statusAdapter = EnumColumnAdapter { LocalSyncStatus.fromString(it) },
            ),
        )
    }

    factory<InspectionStorage> { new(::InspectionStorageImpl) }
    factory<RouteStorage> { new(::RouteStorageImpl) }
    factory<EquipmentStorage> { new(::EquipmentStorageImpl) }
    factory<UserStorage> { new(::UserStorageImpl) }
    factory<ChecklistStorage> { new(::ChecklistStorageImpl) }
    factory<PhotoStorage> { new(::PhotoStorageImpl) }
    factory<ActionLogStorage> { new(::ActionLogStorageImpl) }
    factory<SyncMetaStorage> { new(::SyncMetaStorageImpl) }
}
