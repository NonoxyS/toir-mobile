package ru.mirea.toir.sync.data.mappers

import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedReason

internal fun RemoteSyncRejectedReason.toLocal(): LocalRejectionReason = when (this) {
    RemoteSyncRejectedReason.INVALID_ASSIGNMENT_ID -> LocalRejectionReason.INVALID_ASSIGNMENT_ID
    RemoteSyncRejectedReason.INVALID_ROUTE_ID -> LocalRejectionReason.INVALID_ROUTE_ID
    RemoteSyncRejectedReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN ->
        LocalRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN
    RemoteSyncRejectedReason.ROUTE_ID_MISMATCH -> LocalRejectionReason.ROUTE_ID_MISMATCH
    RemoteSyncRejectedReason.INSPECTION_NOT_FOUND -> LocalRejectionReason.INSPECTION_NOT_FOUND
    RemoteSyncRejectedReason.ROUTE_POINT_NOT_FOUND -> LocalRejectionReason.ROUTE_POINT_NOT_FOUND
    RemoteSyncRejectedReason.EQUIPMENT_MISMATCH -> LocalRejectionReason.EQUIPMENT_MISMATCH
    RemoteSyncRejectedReason.UNKNOWN -> LocalRejectionReason.UNKNOWN
}
