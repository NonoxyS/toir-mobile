package ru.mirea.toir.feature.route.points.presentation.mappers

import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore
import ru.mirea.toir.feature.route.points.presentation.models.UiRoutePointsLabel

interface UiRoutePointsLabelMapper : Mapper<RoutePointsStore.Label, UiRoutePointsLabel>

internal class UiRoutePointsLabelMapperImpl : UiRoutePointsLabelMapper {
    override fun map(item: RoutePointsStore.Label): UiRoutePointsLabel = when (item) {
        is RoutePointsStore.Label.NavigateToEquipmentCard ->
            UiRoutePointsLabel.NavigateToEquipmentCard(item.inspectionId, item.routePointId)
        RoutePointsStore.Label.InspectionFinished -> UiRoutePointsLabel.InspectionFinished
    }
}
