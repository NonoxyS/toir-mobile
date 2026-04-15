package ru.mirea.toir.feature.routes.list.impl.domain.repository

import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment

internal interface RoutesListRepository {
    suspend fun getAssignments(): Result<List<DomainRouteAssignment>>
    suspend fun startInspection(assignmentId: String): Result<String>
}
