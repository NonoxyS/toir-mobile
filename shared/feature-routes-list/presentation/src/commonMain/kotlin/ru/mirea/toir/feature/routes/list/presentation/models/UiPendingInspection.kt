package ru.mirea.toir.feature.routes.list.presentation.models

data class UiPendingInspection(
    val inspectionId: String,
    val routeName: String?,
    val rejectionReason: UiRejectionReason?,
)

enum class UiRejectionReason {
    INVALID_ASSIGNMENT_ID,
    INVALID_ROUTE_ID,
    ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN,
    ROUTE_ID_MISMATCH,
    INSPECTION_NOT_FOUND,
    ROUTE_POINT_NOT_FOUND,
    EQUIPMENT_MISMATCH,
    UNKNOWN,
}
