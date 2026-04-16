package ru.mirea.toir.feature.routes.list.presentation.mappers

import kotlinx.collections.immutable.toImmutableList
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.api.models.RouteAssignmentStatus
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import ru.mirea.toir.feature.routes.list.presentation.models.UiRouteAssignment
import ru.mirea.toir.feature.routes.list.presentation.models.UiRouteStatus
import ru.mirea.toir.feature.routes.list.presentation.models.UiRoutesListState

interface UiRoutesListStateMapper {
    fun map(state: RoutesListStore.State): UiRoutesListState
}

internal class UiRoutesListStateMapperImpl : UiRoutesListStateMapper {
    override fun map(state: RoutesListStore.State): UiRoutesListState = UiRoutesListState(
        assignments = state.assignments.map { it.toUi() }.toImmutableList(),
        isLoading = state.isLoading,
        isError = state.isError,
    )

    private fun DomainRouteAssignment.toUi(): UiRouteAssignment = UiRouteAssignment(
        assignmentId = assignmentId,
        routeName = routeName,
        routeNumber = assignmentId.take(8).uppercase(),
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
    }
}
