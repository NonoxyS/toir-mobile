@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.sync.data.applier

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import ru.mirea.toir.core.database.TransactionRunnerImpl
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorageImpl
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorageImpl
import ru.mirea.toir.core.database.storage.location.LocationStorageImpl
import ru.mirea.toir.core.database.storage.route.RouteStorageImpl
import ru.mirea.toir.sync.data.network.models.RemoteConfigAssignment
import ru.mirea.toir.sync.fixtures.testDispatchers
import ru.mirea.toir.sync.data.network.models.RemoteConfigChangesResponse
import ru.mirea.toir.sync.data.network.models.RemoteConfigChecklist
import ru.mirea.toir.sync.data.network.models.RemoteConfigChecklistItem
import ru.mirea.toir.sync.data.network.models.RemoteConfigEquipment
import ru.mirea.toir.sync.data.network.models.RemoteConfigLocation
import ru.mirea.toir.sync.data.network.models.RemoteConfigRoute
import ru.mirea.toir.sync.data.network.models.RemoteConfigRoutePoint
import ru.mirea.toir.sync.data.network.models.RemoteDeletedIds
import ru.mirea.toir.sync.fixtures.TestDatabase

class ConfigChangesApplierHappyPathTest {

    private val handle = TestDatabase.create()
    private val db = handle.db
    private val driver = handle.driver
    private val dispatchers = testDispatchers()

    private val applier = ConfigChangesApplier(
        routeStorage = RouteStorageImpl(db, dispatchers),
        equipmentStorage = EquipmentStorageImpl(db, dispatchers),
        locationStorage = LocationStorageImpl(db),
        checklistStorage = ChecklistStorageImpl(db, dispatchers),
        transactionRunner = TransactionRunnerImpl(db),
    )

    @AfterTest
    fun tearDown() = driver.close()

    @Test
    fun `apply persists all entity types in a single happy path`() {
        // Equipment type and checklist equipmentType must match so the applier can
        // resolve the checklist for the route point via selectChecklistByEquipmentType.
        val equipmentType = "PUMP"

        val response = RemoteConfigChangesResponse(
            locations = listOf(
                RemoteConfigLocation(
                    id = "loc-1",
                    code = "LOC-001",
                    name = "Pump Room",
                    description = null,
                    parentLocationId = null,
                    updatedAt = "2026-05-13T12:00:00Z",
                ),
            ),
            equipment = listOf(
                RemoteConfigEquipment(
                    id = "eq-1",
                    code = "EQ-001",
                    name = "Main Pump",
                    type = equipmentType,
                    locationId = "loc-1",
                    qrCode = null,
                    updatedAt = "2026-05-13T12:00:00Z",
                ),
            ),
            checklists = listOf(
                RemoteConfigChecklist(
                    id = "chk-1",
                    code = "CHK-001",
                    name = "Pump Checklist",
                    equipmentType = equipmentType,
                    description = null,
                    updatedAt = "2026-05-13T12:00:00Z",
                ),
            ),
            checklistItems = listOf(
                RemoteConfigChecklistItem(
                    id = "item-1",
                    checklistId = "chk-1",
                    title = "Check pressure",
                    description = null,
                    responseType = "BOOLEAN",
                    isRequired = true,
                    requirePhoto = false,
                    optionsJson = null,
                    numericMin = null,
                    numericMax = null,
                    orderIndex = 0,
                    updatedAt = "2026-05-13T12:00:00Z",
                ),
            ),
            routes = listOf(
                RemoteConfigRoute(
                    id = "route-1",
                    code = "RT-001",
                    name = "Morning Route",
                    description = null,
                    updatedAt = "2026-05-13T12:00:00Z",
                ),
            ),
            routePoints = listOf(
                RemoteConfigRoutePoint(
                    id = "rp-1",
                    routeId = "route-1",
                    equipmentId = "eq-1",
                    orderIndex = 0,
                    updatedAt = "2026-05-13T12:00:00Z",
                ),
            ),
            assignments = listOf(
                RemoteConfigAssignment(
                    id = "assign-1",
                    routeId = "route-1",
                    userId = "user-1",
                    assignmentDate = "2026-05-13",
                    shiftCode = null,
                    status = "assigned",
                    updatedAt = "2026-05-13T12:00:00Z",
                ),
            ),
            deletedIds = RemoteDeletedIds(),
            serverTime = "2026-05-13T12:00:00Z",
        )

        applier.apply(response)

        assertEquals(1, db.locationQueries.selectAll().executeAsList().size)
        assertEquals(1, db.equipmentQueries.selectAll().executeAsList().size)
        assertEquals(1, db.checklistQueries.selectAll().executeAsList().size)
        assertEquals(
            1,
            db.checklistItemQueries.selectByChecklistId("chk-1").executeAsList().size,
            "checklistItems: expected 1 row for checklistId=chk-1",
        )
        assertEquals(1, db.routeQueries.selectAll().executeAsList().size)
        assertEquals(
            1,
            db.routePointQueries.selectByRouteId("route-1").executeAsList().size,
            "routePoints: expected 1 row for routeId=route-1",
        )
        assertEquals(1, db.routeAssignmentQueries.selectAll().executeAsList().size)
    }
}
