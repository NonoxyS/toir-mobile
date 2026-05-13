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
import ru.mirea.toir.sync.data.network.models.RemoteConfigChangesResponse
import ru.mirea.toir.sync.data.network.models.RemoteDeletedIds
import ru.mirea.toir.sync.fixtures.TestData.seedEquipment
import ru.mirea.toir.sync.fixtures.TestData.seedLocation
import ru.mirea.toir.sync.fixtures.TestDatabase
import ru.mirea.toir.sync.fixtures.testDispatchers

class ConfigChangesApplierPartialTest {

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
    fun `apply with all empty lists is a no-op`() {
        val empty = RemoteConfigChangesResponse(
            locations = emptyList(),
            equipment = emptyList(),
            checklists = emptyList(),
            checklistItems = emptyList(),
            routes = emptyList(),
            routePoints = emptyList(),
            assignments = emptyList(),
            deletedIds = RemoteDeletedIds(),
            serverTime = "2026-05-13T12:00:00Z",
        )

        applier.apply(empty)

        assertEquals(0, db.locationQueries.selectAll().executeAsList().size)
        assertEquals(0, db.equipmentQueries.selectAll().executeAsList().size)
    }

    @Test
    fun `apply with deleted ids removes existing rows`() {
        db.seedLocation(id = "loc-del")
        db.seedEquipment(id = "eq-del", locationId = "loc-del")

        val response = RemoteConfigChangesResponse(
            locations = emptyList(),
            equipment = emptyList(),
            checklists = emptyList(),
            checklistItems = emptyList(),
            routes = emptyList(),
            routePoints = emptyList(),
            assignments = emptyList(),
            deletedIds = RemoteDeletedIds(
                locations = listOf("loc-del"),
                equipment = listOf("eq-del"),
            ),
            serverTime = "2026-05-13T12:00:00Z",
        )

        applier.apply(response)

        assertEquals(0, db.locationQueries.selectAll().executeAsList().size)
        assertEquals(0, db.equipmentQueries.selectAll().executeAsList().size)
    }
}
