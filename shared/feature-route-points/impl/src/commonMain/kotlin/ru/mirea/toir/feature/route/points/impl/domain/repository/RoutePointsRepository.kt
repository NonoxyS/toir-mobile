package ru.mirea.toir.feature.route.points.impl.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.feature.route.points.api.models.DomainRoutePoint

internal interface RoutePointsRepository {
    fun observeRoutePoints(inspectionId: String): Flow<Pair<String, List<DomainRoutePoint>>>
    suspend fun finishInspection(inspectionId: String): Result<Unit>
}
