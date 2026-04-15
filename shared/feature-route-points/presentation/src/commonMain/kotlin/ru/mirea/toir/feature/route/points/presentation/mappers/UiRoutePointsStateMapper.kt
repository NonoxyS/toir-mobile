package ru.mirea.toir.feature.route.points.presentation.mappers

import kotlinx.collections.immutable.toImmutableList
import ru.mirea.toir.feature.route.points.api.models.DomainRoutePoint
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore
import ru.mirea.toir.feature.route.points.presentation.models.UiRoutePoint
import ru.mirea.toir.feature.route.points.presentation.models.UiRoutePointsState

interface UiRoutePointsStateMapper {
    fun map(state: RoutePointsStore.State): UiRoutePointsState
}

internal class UiRoutePointsStateMapperImpl : UiRoutePointsStateMapper {
    override fun map(state: RoutePointsStore.State): UiRoutePointsState = UiRoutePointsState(
        routeName = state.routeName,
        points = state.points.map { it.toUi() }.toImmutableList(),
        isLoading = state.isLoading,
        canFinish = state.canFinish,
        errorMessage = state.errorMessage,
    )

    private fun DomainRoutePoint.toUi(): UiRoutePoint = UiRoutePoint(
        routePointId = routePointId,
        equipmentCode = equipmentCode,
        equipmentName = equipmentName,
        locationName = locationName,
        statusLabel = status.toStatusLabel(),
        statusColor = status.toStatusColor(),
        hasIssues = hasIssues,
        equipmentResultId = equipmentResultId,
    )

    private fun String.toStatusLabel(): String = when (this) {
        "NOT_STARTED" -> "Не начато"
        "IN_PROGRESS" -> "В работе"
        "COMPLETED" -> "Завершено"
        "SKIPPED" -> "Пропущено"
        else -> this
    }

    private fun String.toStatusColor(): String = when (this) {
        "NOT_STARTED" -> "default"
        "IN_PROGRESS" -> "warning"
        "COMPLETED" -> "success"
        "SKIPPED" -> "error"
        else -> "default"
    }
}
