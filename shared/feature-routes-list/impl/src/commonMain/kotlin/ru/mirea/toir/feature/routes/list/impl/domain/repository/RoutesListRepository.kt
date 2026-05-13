package ru.mirea.toir.feature.routes.list.impl.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.api.models.RoutesListSyncIndicator

internal interface RoutesListRepository {
    fun observeAssignments(): Flow<List<DomainRouteAssignment>>
    suspend fun startInspection(assignmentId: String): Result<String>

    fun observeSyncIndicator(): Flow<RoutesListSyncIndicator>
    fun observeLastSuccessAt(): Flow<String?>
    fun observeLastFailedAt(): Flow<String?>
    fun triggerManualSync()
}
