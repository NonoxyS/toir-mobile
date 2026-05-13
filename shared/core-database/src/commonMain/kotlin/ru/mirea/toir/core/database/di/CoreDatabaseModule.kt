package ru.mirea.toir.core.database.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.core.database.Action_logs
import ru.mirea.toir.core.database.Checklist_item_results
import ru.mirea.toir.core.database.Inspection_equipment_results
import ru.mirea.toir.core.database.Inspections
import ru.mirea.toir.core.database.Photos
import ru.mirea.toir.core.database.Route_assignments
import ru.mirea.toir.core.database.Sync_batches
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.TransactionRunner
import ru.mirea.toir.core.database.TransactionRunnerImpl
import ru.mirea.toir.core.database.adapters.EnumColumnAdapter
import ru.mirea.toir.core.database.driver.DatabaseDriverFactory
import ru.mirea.toir.core.database.models.LocalBatchStatus
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.core.database.models.LocalRouteAssignmentStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.action_log.ActionLogStorage
import ru.mirea.toir.core.database.storage.action_log.ActionLogStorageImpl
import ru.mirea.toir.core.database.storage.action_log.ActionLogger
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorage
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorageImpl
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorage
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorageImpl
import ru.mirea.toir.core.database.storage.inspection.InspectionStorage
import ru.mirea.toir.core.database.storage.inspection.InspectionStorageImpl
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.location.LocationStorage
import ru.mirea.toir.core.database.storage.location.LocationStorageImpl
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
                statusAdapter = EnumColumnAdapter.create<LocalRouteAssignmentStatus>(),
            ),
            inspectionsAdapter = Inspections.Adapter(
                statusAdapter = EnumColumnAdapter.create<LocalInspectionStatus>(),
                sync_statusAdapter = EnumColumnAdapter.create<LocalSyncStatus>(),
                sync_rejection_reasonAdapter = EnumColumnAdapter.create<LocalRejectionReason>(),
            ),
            inspection_equipment_resultsAdapter = Inspection_equipment_results.Adapter(
                statusAdapter = EnumColumnAdapter.create<LocalEquipmentResultStatus>(),
                sync_statusAdapter = EnumColumnAdapter.create<LocalSyncStatus>(),
                sync_rejection_reasonAdapter = EnumColumnAdapter.create<LocalRejectionReason>(),
            ),
            checklist_item_resultsAdapter = Checklist_item_results.Adapter(
                sync_statusAdapter = EnumColumnAdapter.create<LocalSyncStatus>(),
                sync_rejection_reasonAdapter = EnumColumnAdapter.create<LocalRejectionReason>(),
            ),
            photosAdapter = Photos.Adapter(
                sync_statusAdapter = EnumColumnAdapter.create<LocalSyncStatus>(),
            ),
            action_logsAdapter = Action_logs.Adapter(
                sync_statusAdapter = EnumColumnAdapter.create<LocalSyncStatus>(),
            ),
            sync_batchesAdapter = Sync_batches.Adapter(
                statusAdapter = EnumColumnAdapter.create<LocalBatchStatus>(),
            ),
        )
    }

    factory<InspectionStorage> { new(::InspectionStorageImpl) }
    factory<RouteStorage> { new(::RouteStorageImpl) }
    factory<EquipmentStorage> { new(::EquipmentStorageImpl) }
    factory<LocationStorage> { new(::LocationStorageImpl) }
    factory<UserStorage> { new(::UserStorageImpl) }
    factory<ChecklistStorage> { new(::ChecklistStorageImpl) }
    factory<PhotoStorage> { new(::PhotoStorageImpl) }
    factory<ActionLogStorage> { new(::ActionLogStorageImpl) }
    single { new(::ActionLogger) }
    factory<SyncMetaStorage> { new(::SyncMetaStorageImpl) }
    factory<TransactionRunner> { new(::TransactionRunnerImpl) }
}
