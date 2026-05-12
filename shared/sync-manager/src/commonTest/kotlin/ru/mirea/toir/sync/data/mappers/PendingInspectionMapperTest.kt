package ru.mirea.toir.sync.data.mappers

import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.core.database.storage.inspection.models.LocalPendingInspection
import ru.mirea.toir.sync.domain.InspectionRejectionReason
import ru.mirea.toir.sync.domain.PendingInspectionStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class PendingInspectionMapperTest {

    @Test
    fun `maps completed inspection without rejection`() {
        val local = LocalPendingInspection(
            id = "ins-1",
            assignmentId = "asg-1",
            routeId = "route-1",
            status = LocalInspectionStatus.COMPLETED,
            completedAt = "2026-05-12T10:00:00Z",
            attemptCount = 0,
            rejectionReason = null,
        )
        val domain = local.toDomain()
        assertEquals("ins-1", domain.inspectionId)
        assertEquals("route-1", domain.routeId)
        assertEquals("asg-1", domain.assignmentId)
        assertEquals(Instant.parse("2026-05-12T10:00:00Z"), domain.completedAt)
        assertEquals(PendingInspectionStatus.COMPLETED, domain.status)
        assertEquals(0, domain.attemptCount)
        assertNull(domain.rejectionReason)
    }

    @Test
    fun `maps cancelled inspection with rejection reason`() {
        val local = LocalPendingInspection(
            id = "ins-2",
            assignmentId = null,
            routeId = "route-2",
            status = LocalInspectionStatus.CANCELLED,
            completedAt = null,
            attemptCount = 3,
            rejectionReason = LocalRejectionReason.ROUTE_ID_MISMATCH,
        )
        val domain = local.toDomain()
        assertEquals(PendingInspectionStatus.CANCELLED, domain.status)
        assertEquals(3, domain.attemptCount)
        assertEquals(InspectionRejectionReason.ROUTE_ID_MISMATCH, domain.rejectionReason)
        assertNull(domain.completedAt)
        assertNull(domain.assignmentId)
    }
}
