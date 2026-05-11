package ru.mirea.toir.feature.routes.list.impl.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment

internal interface RoutesListRepository {
    fun observeAssignments(): Flow<List<DomainRouteAssignment>>
    suspend fun startInspection(assignmentId: String): Result<String>
}
