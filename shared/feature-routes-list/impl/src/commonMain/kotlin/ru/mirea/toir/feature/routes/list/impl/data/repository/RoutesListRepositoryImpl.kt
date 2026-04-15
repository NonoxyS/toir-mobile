package ru.mirea.toir.feature.routes.list.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.dao.InspectionDao
import ru.mirea.toir.core.database.dao.RouteDao
import ru.mirea.toir.core.database.models.LocalRouteStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.impl.data.mappers.RouteAssignmentMapper
import ru.mirea.toir.feature.routes.list.impl.domain.repository.RoutesListRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class RoutesListRepositoryImpl(
    private val routeDao: RouteDao,
    private val inspectionDao: InspectionDao,
    private val mapper: RouteAssignmentMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
) : RoutesListRepository {

    override suspend fun getAssignments(): Result<List<DomainRouteAssignment>> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val assignments = routeDao.selectAllAssignments()
                    val result = assignments.map { assignment ->
                        val route = routeDao.selectRouteById(assignment.route_id)
                        val points = routeDao.selectPointsByRouteId(assignment.route_id)
                        val inspection = inspectionDao.selectByAssignmentId(assignment.id)
                        val completedCount = if (inspection != null) {
                            inspectionDao.selectEquipmentResultsByInspectionId(inspection.id)
                                .count { it.status == "COMPLETED" }
                        } else {
                            0
                        }
                        val hasPendingSync = inspection?.sync_status == LocalSyncStatus.PENDING &&
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
                    val existing = inspectionDao.selectByAssignmentId(assignmentId)
                    if (existing != null) return@coRunCatching existing.id.wrapResultSuccess()

                    val assignment = routeDao.selectAssignmentById(assignmentId)
                        ?: error("Assignment not found: $assignmentId")
                    val inspectionId = Uuid.random().toString()
                    inspectionDao.insertInspection(
                        id = inspectionId,
                        assignmentId = assignmentId,
                        routeId = assignment.route_id,
                        status = LocalRouteStatus.IN_PROGRESS,
                        startedAt = Clock.System.now().toString(),
                    )
                    routeDao.updateAssignmentStatus(
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
