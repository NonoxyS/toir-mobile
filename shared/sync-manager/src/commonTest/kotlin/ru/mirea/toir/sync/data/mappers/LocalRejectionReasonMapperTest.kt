package ru.mirea.toir.sync.data.mappers

import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.sync.domain.InspectionRejectionReason
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalRejectionReasonMapperTest {

    @Test
    fun `every LocalRejectionReason maps to corresponding domain value`() {
        val expected = mapOf(
            LocalRejectionReason.INVALID_ASSIGNMENT_ID to InspectionRejectionReason.INVALID_ASSIGNMENT_ID,
            LocalRejectionReason.INVALID_ROUTE_ID to InspectionRejectionReason.INVALID_ROUTE_ID,
            LocalRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN to InspectionRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN,
            LocalRejectionReason.ROUTE_ID_MISMATCH to InspectionRejectionReason.ROUTE_ID_MISMATCH,
            LocalRejectionReason.INSPECTION_NOT_FOUND to InspectionRejectionReason.INSPECTION_NOT_FOUND,
            LocalRejectionReason.ROUTE_POINT_NOT_FOUND to InspectionRejectionReason.ROUTE_POINT_NOT_FOUND,
            LocalRejectionReason.EQUIPMENT_MISMATCH to InspectionRejectionReason.EQUIPMENT_MISMATCH,
            LocalRejectionReason.UNKNOWN to InspectionRejectionReason.UNKNOWN,
        )
        expected.forEach { (local, domain) ->
            assertEquals(domain, local.toDomain())
        }
    }
}
