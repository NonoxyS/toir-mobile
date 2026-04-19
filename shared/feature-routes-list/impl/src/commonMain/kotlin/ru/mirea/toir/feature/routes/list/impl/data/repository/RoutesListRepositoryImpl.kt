package ru.mirea.toir.feature.routes.list.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.models.LocalRouteStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.inspection.InspectionStorage
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.route.RouteStorage
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.impl.data.mappers.RouteAssignmentMapper
import ru.mirea.toir.feature.routes.list.impl.domain.repository.RoutesListRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class RoutesListRepositoryImpl(
    private val routeStorage: RouteStorage,
    private val inspectionStorage: InspectionStorage,
    private val mapper: RouteAssignmentMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
) : RoutesListRepository {

    override suspend fun getAssignments(): Result<List<DomainRouteAssignment>> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val assignments = routeStorage.selectAllAssignments()
                    val result = assignments.map { assignment ->
                        val route = routeStorage.selectRouteById(assignment.routeId)
                        val points = routeStorage.selectPointsByRouteId(assignment.routeId)
                        val inspection = inspectionStorage.selectInspectionByAssignmentId(assignment.id)
                        val completedCount = if (inspection != null) {
                            inspectionStorage.selectEquipmentResultsByInspectionId(inspection.id)
                                .count { it.status == LocalEquipmentResultStatus.COMPLETED }
                        } else {
                            0
                        }
                        val hasPendingSync = inspection?.syncStatus == LocalSyncStatus.PENDING &&
                            inspection.status == LocalRouteStatus.COMPLETED
                        mapper.map(
                            assignment = assignment,
                            route = route,
                            totalPoints = points.size,
                            completedPoints = completedCount,
                            inspectionId = inspection?.id,
                            hasPendingSync = hasPendingSync,
                        )
                    }
                    result.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "getAssignments failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
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
                    val inspectionId = Uuid.random().toString()
                    inspectionStorage.insertInspection(
                        id = inspectionId,
                        assignmentId = assignmentId,
                        routeId = assignment.routeId,
                        status = LocalRouteStatus.IN_PROGRESS,
                        startedAt = Clock.System.now().toString(),
                    )
                    routeStorage.updateAssignmentStatus(
                        id = assignmentId,
                        status = LocalRouteStatus.IN_PROGRESS,
                    )
                    inspectionId.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "startInspection failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
