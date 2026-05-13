package ru.mirea.toir.sync.data.mappers

import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedReason
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoteRejectedReasonMapperTest {

    @Test
    fun `every RemoteSyncRejectedReason maps to LocalRejectionReason`() {
        val expected = mapOf(
            RemoteSyncRejectedReason.INVALID_ASSIGNMENT_ID to LocalRejectionReason.INVALID_ASSIGNMENT_ID,
            RemoteSyncRejectedReason.INVALID_ROUTE_ID to LocalRejectionReason.INVALID_ROUTE_ID,
            RemoteSyncRejectedReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN to
                LocalRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN,
            RemoteSyncRejectedReason.ROUTE_ID_MISMATCH to LocalRejectionReason.ROUTE_ID_MISMATCH,
            RemoteSyncRejectedReason.INSPECTION_NOT_FOUND to LocalRejectionReason.INSPECTION_NOT_FOUND,
            RemoteSyncRejectedReason.ROUTE_POINT_NOT_FOUND to LocalRejectionReason.ROUTE_POINT_NOT_FOUND,
            RemoteSyncRejectedReason.EQUIPMENT_MISMATCH to LocalRejectionReason.EQUIPMENT_MISMATCH,
            RemoteSyncRejectedReason.UNKNOWN to LocalRejectionReason.UNKNOWN,
        )
        expected.forEach { (remote, local) ->
            assertEquals(local, remote.toLocal())
        }
    }
}
