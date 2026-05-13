@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.sync.fixtures

import app.cash.sqldelight.db.SqlDriver
import ru.mirea.toir.core.database.Action_logs
import ru.mirea.toir.core.database.Checklist_item_results
import ru.mirea.toir.core.database.Inspection_equipment_results
import ru.mirea.toir.core.database.Inspections
import ru.mirea.toir.core.database.Photos
import ru.mirea.toir.core.database.Route_assignments
import ru.mirea.toir.core.database.Sync_batches
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.adapters.EnumColumnAdapter
import ru.mirea.toir.core.database.models.LocalBatchStatus
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.core.database.models.LocalRouteAssignmentStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus

data class TestDatabaseHandle(val db: ToirDatabase, val driver: SqlDriver)

object TestDatabase {

    fun create(): TestDatabaseHandle {
        val driver = createInMemoryDriver()
        val db = ToirDatabase(
            driver = driver,
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
        return TestDatabaseHandle(db = db, driver = driver)
    }
}
