package ru.mirea.toir.feature.route.points.impl.domain.repository

import ru.mirea.toir.feature.route.points.api.models.DomainRoutePoint

internal interface RoutePointsRepository {
    suspend fun getRoutePoints(inspectionId: String): Result<Pair<String, List<DomainRoutePoint>>>
    suspend fun finishInspection(inspectionId: String): Result<Unit>
}
