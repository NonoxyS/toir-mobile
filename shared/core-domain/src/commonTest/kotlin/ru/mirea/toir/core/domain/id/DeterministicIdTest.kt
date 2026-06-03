package ru.mirea.toir.core.domain.id

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DeterministicIdTest {

    private val assignmentA = "61000000-0000-0000-0000-000000000001"
    private val assignmentB = "61000000-0000-0000-0000-000000000002"
    private val inspectionId = "e95434bc-0000-5a7c-0000-000000000001"
    private val routePointA = "70000000-0000-0000-0000-000000000001"
    private val routePointB = "70000000-0000-0000-0000-000000000002"
    private val ierId = "a1111111-0000-5111-0000-000000000001"
    private val itemA = "80000000-0000-0000-0000-000000000001"
    private val itemB = "80000000-0000-0000-0000-000000000002"

    private val uuidRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")

    @Test
    fun `inspection id is deterministic per assignment`() {
        assertEquals(
            DeterministicId.forInspection(assignmentA),
            DeterministicId.forInspection(assignmentA),
        )
    }

    @Test
    fun `inspection id differs per assignment`() {
        assertNotEquals(
            DeterministicId.forInspection(assignmentA),
            DeterministicId.forInspection(assignmentB),
        )
    }

    @Test
    fun `equipment result id is deterministic per inspection and route point`() {
        assertEquals(
            DeterministicId.forEquipmentResult(inspectionId, routePointA),
            DeterministicId.forEquipmentResult(inspectionId, routePointA),
        )
    }

    @Test
    fun `equipment result id differs per route point`() {
        assertNotEquals(
            DeterministicId.forEquipmentResult(inspectionId, routePointA),
            DeterministicId.forEquipmentResult(inspectionId, routePointB),
        )
    }

    @Test
    fun `checklist item result id is deterministic per equipment result and item`() {
        assertEquals(
            DeterministicId.forChecklistItemResult(ierId, itemA),
            DeterministicId.forChecklistItemResult(ierId, itemA),
        )
    }

    @Test
    fun `checklist item result id differs per item`() {
        assertNotEquals(
            DeterministicId.forChecklistItemResult(ierId, itemA),
            DeterministicId.forChecklistItemResult(ierId, itemB),
        )
    }

    @Test
    fun `namespaces are distinct so same name yields different ids per entity type`() {
        // Один и тот же name не должен схлопываться между типами сущностей.
        val name = assignmentA
        val asInspection = DeterministicId.forInspection(name)
        val asEquipment = DeterministicId.forEquipmentResult(name, "")
        assertNotEquals(asInspection, asEquipment)
    }

    @Test
    fun `all ids are canonical lowercase uuid strings`() {
        assertTrue(uuidRegex.matches(DeterministicId.forInspection(assignmentA)))
        assertTrue(uuidRegex.matches(DeterministicId.forEquipmentResult(inspectionId, routePointA)))
        assertTrue(uuidRegex.matches(DeterministicId.forChecklistItemResult(ierId, itemA)))
    }
}
