package ru.mirea.toir.feature.routes.list.presentation.mappers

import kotlinx.collections.immutable.toImmutableList
import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.api.models.RouteAssignmentStatus
import ru.mirea.toir.feature.routes.list.api.models.RoutesListPendingInspection
import ru.mirea.toir.feature.routes.list.api.models.RoutesListRejectionReason
import ru.mirea.toir.feature.routes.list.api.models.RoutesListSyncFailure
import ru.mirea.toir.feature.routes.list.api.models.RoutesListSyncIndicator
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import ru.mirea.toir.feature.routes.list.presentation.models.UiPendingInspection
import ru.mirea.toir.feature.routes.list.presentation.models.UiRejectionReason
import ru.mirea.toir.feature.routes.list.presentation.models.UiRouteAssignment
import ru.mirea.toir.feature.routes.list.presentation.models.UiRouteStatus
import ru.mirea.toir.feature.routes.list.presentation.models.UiRoutesListState
import ru.mirea.toir.feature.routes.list.presentation.models.UiSyncFailure
import ru.mirea.toir.feature.routes.list.presentation.models.UiSyncIndicator

interface UiRoutesListStateMapper : Mapper<RoutesListStore.State, UiRoutesListState>

internal class UiRoutesListStateMapperImpl : UiRoutesListStateMapper {
    override fun map(item: RoutesListStore.State): UiRoutesListState = UiRoutesListState(
        assignments = item.assignments.map { it.toUi() }.toImmutableList(),
        isLoading = item.isLoading,
        isError = item.isError,
        syncIndicator = item.syncIndicator.toUi(),
        syncLastSuccessAt = item.syncLastSuccessAt,
        syncLastFailedAt = item.syncLastFailedAt,
        isSyncSheetVisible = item.isSyncSheetVisible,
    )

    private fun DomainRouteAssignment.toUi(): UiRouteAssignment = UiRouteAssignment(
        assignmentId = assignmentId,
        routeName = routeName,
        routeNumber = routeCode,
        status = status.toUiStatus(),
        completedPoints = completedPoints,
        totalPoints = totalPoints,
        progress = if (totalPoints > 0) completedPoints.toFloat() / totalPoints.toFloat() else 0f,
        assignedAt = assignedAt,
        inspectionId = inspectionId,
        hasPendingSync = hasPendingSync,
    )

    private fun RouteAssignmentStatus.toUiStatus(): UiRouteStatus = when (this) {
        RouteAssignmentStatus.ASSIGNED -> UiRouteStatus.ASSIGNED
        RouteAssignmentStatus.IN_PROGRESS -> UiRouteStatus.IN_PROGRESS
        RouteAssignmentStatus.COMPLETED -> UiRouteStatus.COMPLETED
        RouteAssignmentStatus.PARTIALLY_COMPLETED -> UiRouteStatus.PARTIALLY_COMPLETED
        RouteAssignmentStatus.CANCELLED -> UiRouteStatus.CANCELLED
        RouteAssignmentStatus.SYNC_REQUIRED -> UiRouteStatus.SYNC_REQUIRED
    }

    private fun RoutesListSyncIndicator.toUi(): UiSyncIndicator = UiSyncIndicator(
        isRunning = isRunning,
        hasPending = hasPending,
        pendingInspections = pendingInspections.map { it.toUi() },
        lastError = lastError?.toUi(),
    )

    private fun RoutesListPendingInspection.toUi(): UiPendingInspection =
        UiPendingInspection(
            inspectionId = inspectionId,
            routeName = routeName,
            rejectionReason = rejectionReason?.toUi(),
        )

    private fun RoutesListRejectionReason.toUi(): UiRejectionReason = when (this) {
        RoutesListRejectionReason.INVALID_ASSIGNMENT_ID -> UiRejectionReason.INVALID_ASSIGNMENT_ID
        RoutesListRejectionReason.INVALID_ROUTE_ID -> UiRejectionReason.INVALID_ROUTE_ID
        RoutesListRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN ->
            UiRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN
        RoutesListRejectionReason.ROUTE_ID_MISMATCH -> UiRejectionReason.ROUTE_ID_MISMATCH
        RoutesListRejectionReason.INSPECTION_NOT_FOUND -> UiRejectionReason.INSPECTION_NOT_FOUND
        RoutesListRejectionReason.ROUTE_POINT_NOT_FOUND -> UiRejectionReason.ROUTE_POINT_NOT_FOUND
        RoutesListRejectionReason.EQUIPMENT_MISMATCH -> UiRejectionReason.EQUIPMENT_MISMATCH
        RoutesListRejectionReason.UNKNOWN -> UiRejectionReason.UNKNOWN
    }

    private fun RoutesListSyncFailure.toUi(): UiSyncFailure = when (this) {
        RoutesListSyncFailure.NETWORK -> UiSyncFailure.NETWORK
        RoutesListSyncFailure.AUTH -> UiSyncFailure.AUTH
        RoutesListSyncFailure.SERVER -> UiSyncFailure.SERVER
        RoutesListSyncFailure.UNKNOWN -> UiSyncFailure.UNKNOWN
    }
}
