package ru.mirea.toir.feature.route.points.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.dao.EquipmentDao
import ru.mirea.toir.core.database.dao.InspectionDao
import ru.mirea.toir.core.database.dao.RouteDao
import ru.mirea.toir.core.database.models.LocalRouteStatus
import ru.mirea.toir.feature.route.points.api.models.DomainRoutePoint
import ru.mirea.toir.feature.route.points.impl.domain.repository.RoutePointsRepository

internal class RoutePointsRepositoryImpl(
    private val inspectionDao: InspectionDao,
    private val routeDao: RouteDao,
    private val equipmentDao: EquipmentDao,
    private val coroutineDispatchers: CoroutineDispatchers,
) : RoutePointsRepository {

    override suspend fun getRoutePoints(inspectionId: String): Result<Pair<String, List<DomainRoutePoint>>> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val inspection = inspectionDao.selectById(inspectionId)
                        ?: error("Inspection not found: $inspectionId")
                    val route = routeDao.selectRouteById(inspection.route_id)
                    val routePoints = routeDao.selectPointsByRouteId(inspection.route_id)
                    val equipmentResults = inspectionDao.selectEquipmentResultsByInspectionId(inspectionId)

                    val points = routePoints.map { point ->
                        val equipment = equipmentDao.selectById(point.equipment_id)
                        val result = equipmentResults.firstOrNull { it.route_point_id == point.id }
                        DomainRoutePoint(
                            routePointId = point.id,
                            equipmentId = point.equipment_id,
                            equipmentCode = equipment?.code.orEmpty(),
                            equipmentName = equipment?.name.orEmpty(),
                            locationName = equipment?.location_id.orEmpty(),
                            equipmentResultId = result?.id,
                            status = result?.status ?: "NOT_STARTED",
                            hasIssues = result?.status == "SKIPPED",
                        )
                    }

                    (route?.name.orEmpty() to points).wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "getRoutePoints failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    @OptIn(ExperimentalTime::class)
    override suspend fun finishInspection(inspectionId: String): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    inspectionDao.updateInspectionStatus(
                        id = inspectionId,
                        status = LocalRouteStatus.COMPLETED,
                        completedAt = Clock.System.now().toString(),
                    )
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "finishInspection failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
