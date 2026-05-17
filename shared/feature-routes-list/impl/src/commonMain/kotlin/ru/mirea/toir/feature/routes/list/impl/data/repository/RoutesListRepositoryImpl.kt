@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.mirea.toir.feature.routes.list.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorage
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.api.models.RouteAssignmentStatus
import ru.mirea.toir.feature.routes.list.api.models.RoutesListPendingInspection
import ru.mirea.toir.feature.routes.list.api.models.RoutesListRejectionReason
import ru.mirea.toir.feature.routes.list.api.models.RoutesListSyncFailure
import ru.mirea.toir.feature.routes.list.api.models.RoutesListSyncIndicator
import ru.mirea.toir.feature.routes.list.impl.data.mappers.RouteAssignmentMapper
import ru.mirea.toir.feature.routes.list.impl.domain.repository.RoutesListRepository
import ru.mirea.toir.sync.domain.DomainPendingInspection
import ru.mirea.toir.sync.domain.InspectionRejectionReason
import ru.mirea.toir.sync.domain.SyncFailureReason
import ru.mirea.toir.sync.domain.SyncManager
import ru.mirea.toir.sync.domain.SyncStatus
import ru.mirea.toir.sync.domain.SyncTrigger
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class RoutesListRepositoryImpl(
    private val routeStorage: RouteStorage,
    private val inspectionStorage: InspectionStorage,
    private val actionLogger: ActionLogger,
    private val syncManager: SyncManager,
    private val syncMetaStorage: SyncMetaStorage,
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
        // Сервер говорит, что инспекция идёт, но локальной записи нет — например, её начали
        // на другом устройстве, а pull-делта пока не пришла. Создавать новую запись здесь
        // означало бы породить дубликат. Сигнализируем UI явным состоянием, чтобы юзер
        // мог дёрнуть синхронизацию вместо «Продолжить» с несуществующим inspectionId.
        assignmentStatus == LocalRouteAssignmentStatus.IN_PROGRESS ||
            assignmentStatus == LocalRouteAssignmentStatus.PARTIALLY_COMPLETED ->
            RouteAssignmentStatus.SYNC_REQUIRED
        else -> assignmentStatus.toDomain()
    }

    private fun LocalRouteAssignmentStatus.toDomain(): RouteAssignmentStatus = when (this) {
        LocalRouteAssignmentStatus.ASSIGNED -> RouteAssignmentStatus.ASSIGNED
        LocalRouteAssignmentStatus.IN_PROGRESS -> RouteAssignmentStatus.IN_PROGRESS
        LocalRouteAssignmentStatus.COMPLETED -> RouteAssignmentStatus.COMPLETED
        LocalRouteAssignmentStatus.PARTIALLY_COMPLETED -> RouteAssignmentStatus.PARTIALLY_COMPLETED
        LocalRouteAssignmentStatus.CANCELLED -> RouteAssignmentStatus.CANCELLED
    }

    override fun observeSyncIndicator(): Flow<RoutesListSyncIndicator> =
        combine(
            syncManager.status,
            syncManager.hasPending,
            syncManager.pendingInspections,
            syncMetaStorage.observeByKey(SyncMetaStorage.KEY_LAST_SYNC_ERROR_REASON),
            syncMetaStorage.observeByKey(SyncMetaStorage.KEY_LAST_SYNC_ERROR_AT),
            syncMetaStorage.observeByKey(SyncMetaStorage.KEY_LAST_SYNC_AT_SUCCESS),
        ) { values ->
            val status = values[0] as SyncStatus
            val hasPending = values[1] as Boolean

            @Suppress("UNCHECKED_CAST")
            val domainPending = values[2] as List<DomainPendingInspection>
            val errorReason = values[3] as String?
            val errorAt = values[4] as String?
            val successAt = values[5] as String?
            RoutesListSyncIndicator(
                isRunning = status is SyncStatus.Running,
                hasPending = hasPending,
                pendingInspections = domainPending.map { it.toApi() },
                lastError = resolveLastError(status, errorReason, errorAt, successAt),
            )
        }.flowOn(coroutineDispatchers.io)

    private fun resolveLastError(
        status: SyncStatus,
        errorReason: String?,
        errorAt: String?,
        successAt: String?,
    ): RoutesListSyncFailure? = when (status) {
        is SyncStatus.Failed -> status.reason.toApi()
        is SyncStatus.Success -> null
        SyncStatus.Running, SyncStatus.Idle -> {
            val errorIsFresh = errorAt != null && (successAt == null || errorAt > successAt)
            if (errorIsFresh && errorReason != null) {
                runCatching { SyncFailureReason.valueOf(errorReason) }.getOrNull()?.toApi()
            } else {
                null
            }
        }
    }

    override fun observeLastSuccessAt(): Flow<String?> =
        syncMetaStorage.observeByKey(SyncMetaStorage.KEY_LAST_SYNC_AT_SUCCESS)
            .flowOn(coroutineDispatchers.io)

    override fun observeLastFailedAt(): Flow<String?> =
        syncMetaStorage.observeByKey(SyncMetaStorage.KEY_LAST_SYNC_ERROR_AT)
            .flowOn(coroutineDispatchers.io)

    override fun triggerManualSync() {
        syncManager.syncNow(SyncTrigger.Manual)
    }

    private fun DomainPendingInspection.toApi(): RoutesListPendingInspection {
        val routeName = routeStorage.selectRouteById(routeId)?.name
        return RoutesListPendingInspection(
            inspectionId = inspectionId,
            routeName = routeName,
            rejectionReason = rejectionReason?.toApi(),
        )
    }

    private fun InspectionRejectionReason.toApi(): RoutesListRejectionReason = when (this) {
        InspectionRejectionReason.INVALID_ASSIGNMENT_ID -> RoutesListRejectionReason.INVALID_ASSIGNMENT_ID
        InspectionRejectionReason.INVALID_ROUTE_ID -> RoutesListRejectionReason.INVALID_ROUTE_ID
        InspectionRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN ->
            RoutesListRejectionReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN
        InspectionRejectionReason.ROUTE_ID_MISMATCH -> RoutesListRejectionReason.ROUTE_ID_MISMATCH
        InspectionRejectionReason.INSPECTION_NOT_FOUND -> RoutesListRejectionReason.INSPECTION_NOT_FOUND
        InspectionRejectionReason.ROUTE_POINT_NOT_FOUND -> RoutesListRejectionReason.ROUTE_POINT_NOT_FOUND
        InspectionRejectionReason.EQUIPMENT_MISMATCH -> RoutesListRejectionReason.EQUIPMENT_MISMATCH
        InspectionRejectionReason.UNKNOWN -> RoutesListRejectionReason.UNKNOWN
    }

    private fun SyncFailureReason.toApi(): RoutesListSyncFailure = when (this) {
        SyncFailureReason.NETWORK -> RoutesListSyncFailure.NETWORK
        SyncFailureReason.AUTH -> RoutesListSyncFailure.AUTH
        SyncFailureReason.SERVER -> RoutesListSyncFailure.SERVER
        SyncFailureReason.UNKNOWN -> RoutesListSyncFailure.UNKNOWN
    }

    private companion object {
        val COMPLETED_INSPECTION_STATUSES = setOf(
            LocalInspectionStatus.COMPLETED,
            LocalInspectionStatus.PARTIALLY_COMPLETED,
            LocalInspectionStatus.CANCELLED,
        )
    }
}
