package ru.mirea.toir.core.database.models

enum class LocalRejectionReason(
    override val localValue: String,
) : LocalEnum {

    INVALID_ASSIGNMENT_ID("invalid_assignment_id"),
    INVALID_ROUTE_ID("invalid_route_id"),
    ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN("route_assignment_not_found_or_forbidden"),
    ROUTE_ID_MISMATCH("route_id_mismatch"),
    INSPECTION_NOT_FOUND("inspection_not_found"),
    ROUTE_POINT_NOT_FOUND("route_point_not_found"),
    EQUIPMENT_MISMATCH("equipment_mismatch"),
    UNKNOWN("unknown"),
}
