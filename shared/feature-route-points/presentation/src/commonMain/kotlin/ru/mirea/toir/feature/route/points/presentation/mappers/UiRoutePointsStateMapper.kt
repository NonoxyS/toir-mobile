package ru.mirea.toir.feature.route.points.presentation.mappers

import kotlinx.collections.immutable.toImmutableList
import ru.mirea.toir.feature.route.points.api.models.DomainRoutePoint
import ru.mirea.toir.feature.route.points.api.models.EquipmentResultStatus
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore
import ru.mirea.toir.feature.route.points.presentation.models.UiEquipmentResultStatus
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
        status = status.toUi(),
        hasIssues = hasIssues,
        equipmentResultId = equipmentResultId,
    )

    private fun EquipmentResultStatus.toUi(): UiEquipmentResultStatus = when (this) {
        EquipmentResultStatus.NOT_STARTED -> UiEquipmentResultStatus.NOT_STARTED
        EquipmentResultStatus.IN_PROGRESS -> UiEquipmentResultStatus.IN_PROGRESS
        EquipmentResultStatus.COMPLETED -> UiEquipmentResultStatus.COMPLETED
        EquipmentResultStatus.SKIPPED -> UiEquipmentResultStatus.SKIPPED
    }
}
