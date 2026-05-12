package ru.mirea.toir.sync.data.mappers

import ru.mirea.toir.core.database.models.LocalRejectionReason
import ru.mirea.toir.sync.domain.InspectionRejectionReason

internal fun LocalRejectionReason.toDomain(): InspectionRejectionReason = when (this) {
    LocalRejectionReason.INVALID_ASSIGNMENT_ID -> InspectionRejectionReason.INVALID_ASSIGNMENT_ID
    LocalRejectionReason.INVALID_ROUTE_ID -> InspectionRejectionReason.INVALID_ROUTE_ID
    LocalRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN -> InspectionRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN
    LocalRejectionReason.ROUTE_ID_MISMATCH -> InspectionRejectionReason.ROUTE_ID_MISMATCH
    LocalRejectionReason.INSPECTION_NOT_FOUND -> InspectionRejectionReason.INSPECTION_NOT_FOUND
    LocalRejectionReason.ROUTE_POINT_NOT_FOUND -> InspectionRejectionReason.ROUTE_POINT_NOT_FOUND
    LocalRejectionReason.EQUIPMENT_MISMATCH -> InspectionRejectionReason.EQUIPMENT_MISMATCH
    LocalRejectionReason.UNKNOWN -> InspectionRejectionReason.UNKNOWN
}
