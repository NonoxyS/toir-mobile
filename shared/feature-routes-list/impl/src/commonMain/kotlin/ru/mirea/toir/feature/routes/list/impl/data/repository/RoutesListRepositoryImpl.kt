@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.mirea.toir.feature.routes.list.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalRouteAssignmentStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.action_log.ActionLogEntityType
import ru.mirea.toir.core.database.storage.action_log.ActionLogType
import ru.mirea.toir.core.database.storage.action_log.ActionLogger
import ru.mirea.toir.core.database.storage.inspection.InspectionStorage
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.route.RouteStorage
import ru.mirea.toir.core.database.storage.route.models.LocalRouteAssignment
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.api.models.RouteAssignmentStatus
import ru.mirea.toir.feature.routes.list.impl.data.mappers.RouteAssignmentMapper
import ru.mirea.toir.feature.routes.list.impl.domain.repository.RoutesListRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class RoutesListRepositoryImpl(
    private val routeStorage: RouteStorage,
    private val inspectionStorage: InspectionStorage,
    private val actionLogger: ActionLogger,
    private val mapper: RouteAssignmentMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
) : RoutesListRepository {

    override fun observeAssignments(): Flow<List<DomainRouteAssignment>> =
        routeStorage.observeAllAssignments()
            .flatMapLatest { assignments ->
                if (assignments.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val perAssignmentFlows = assignments.map { assignment ->
                        buildAssignmentFlow(assignment)
                    }
                    combine(perAssignmentFlows) { it.asList() }
                }
            }
            .flowOn(coroutineDispatchers.io)

    private fun buildAssignmentFlow(
        assignment: LocalRouteAssignment,
    ): Flow<DomainRouteAssignment> {
        // Routes are reference data that change only via applyConfigChanges
        // (not during a session) — safe to capture once at subscription time.
        val route = routeStorage.selectRouteById(assignment.routeId)
        val pointsFlow = routeStorage.observePointsByRouteId(assignment.routeId)
        val inspectionFlow = inspectionStorage.observeInspectionByAssignmentId(assignment.id)
        return inspectionFlow.flatMapLatest { inspection ->
            val equipmentResultsFlow = if (inspection != null) {
                inspectionStorage.observeEquipmentResultsByInspectionId(inspection.id)
            } else {
                flowOf(emptyList())
            }
            combine(pointsFlow, equipmentResultsFlow) { points, results ->
                val completedCount = results.count {
                    it.status == LocalEquipmentResultStatus.COMPLETED
                }
                val hasPendingSync = inspection?.syncStatus == LocalSyncStatus.PENDING &&
                    inspection.status in COMPLETED_INSPECTION_STATUSES
                val effectiveStatus = resolveEffectiveStatus(
                    assignmentStatus = assignment.status,
                    inspectionStatus = inspection?.status,
                    totalPoints = points.size,
                    completedCount = completedCount,
                )
                mapper.map(
                    assignment = assignment,
                    route = route,
                    status = effectiveStatus,
                    totalPoints = points.size,
                    completedPoints = completedCount,
                    inspectionId = inspection?.id,
                    hasPendingSync = hasPendingSync,
                )
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun startInspection(assignmentId: String): Result<String> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val existing = inspectionStorage.selectInspectionByAssignmentId(assignmentId)
                    if (existing != null) return@coRunCatching existing.id.wrapResultSuccess()

                    val assignment = routeStorage.selectAssignmentById(assignmentId)
                        ?: error("Assignment not found: $assignmentId")
                    val now = Clock.System.now().toString()
                    val inspectionId = Uuid.random().toString()
                    inspectionStorage.insertInspection(
                        id = inspectionId,
                        assignmentId = assignmentId,
                        routeId = assignment.routeId,
                        status = LocalInspectionStatus.IN_PROGRESS,
                        startedAt = now,
                        createdAt = now,
                        updatedAt = now,
                    )
                    actionLogger.log(
                        actionType = ActionLogType.INSPECTION_STARTED,
                        entityType = ActionLogEntityType.INSPECTION,
                        entityId = inspectionId,
                    )
                    inspectionId.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "startInspection failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    private fun resolveEffectiveStatus(
        assignmentStatus: LocalRouteAssignmentStatus,
        inspectionStatus: LocalInspectionStatus?,
        totalPoints: Int,
        completedCount: Int,
    ): RouteAssignmentStatus = when {
        assignmentStatus == LocalRouteAssignmentStatus.CANCELLED -> RouteAssignmentStatus.CANCELLED
        inspectionStatus == LocalInspectionStatus.IN_PROGRESS -> RouteAssignmentStatus.IN_PROGRESS
        inspectionStatus == LocalInspectionStatus.COMPLETED -> {
            if (totalPoints > 0 && completedCount == totalPoints) {
                RouteAssignmentStatus.COMPLETED
            } else {
                RouteAssignmentStatus.PARTIALLY_COMPLETED
            }
        }
        inspectionStatus == LocalInspectionStatus.PARTIALLY_COMPLETED -> RouteAssignmentStatus.PARTIALLY_COMPLETED
        else -> assignmentStatus.toDomain()
    }

    private fun LocalRouteAssignmentStatus.toDomain(): RouteAssignmentStatus = when (this) {
        LocalRouteAssignmentStatus.ASSIGNED -> RouteAssignmentStatus.ASSIGNED
        LocalRouteAssignmentStatus.IN_PROGRESS -> RouteAssignmentStatus.IN_PROGRESS
        LocalRouteAssignmentStatus.COMPLETED -> RouteAssignmentStatus.COMPLETED
        LocalRouteAssignmentStatus.PARTIALLY_COMPLETED -> RouteAssignmentStatus.PARTIALLY_COMPLETED
        LocalRouteAssignmentStatus.CANCELLED -> RouteAssignmentStatus.CANCELLED
    }

    private companion object {
        val COMPLETED_INSPECTION_STATUSES = setOf(
            LocalInspectionStatus.COMPLETED,
            LocalInspectionStatus.PARTIALLY_COMPLETED,
            LocalInspectionStatus.CANCELLED,
        )
    }
}
