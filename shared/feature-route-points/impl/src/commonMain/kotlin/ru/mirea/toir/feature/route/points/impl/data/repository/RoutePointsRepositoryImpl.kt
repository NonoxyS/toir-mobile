package ru.mirea.toir.feature.route.points.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.storage.action_log.ActionLogEntityType
import ru.mirea.toir.core.database.storage.action_log.ActionLogType
import ru.mirea.toir.core.database.storage.action_log.ActionLogger
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorage
import ru.mirea.toir.core.database.storage.inspection.InspectionStorage
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.location.LocationStorage
import ru.mirea.toir.core.database.storage.route.RouteStorage
import ru.mirea.toir.feature.route.points.api.models.DomainRoutePoint
import ru.mirea.toir.feature.route.points.api.models.EquipmentResultStatus
import ru.mirea.toir.feature.route.points.impl.domain.repository.RoutePointsRepository

internal class RoutePointsRepositoryImpl(
    private val inspectionStorage: InspectionStorage,
    private val routeStorage: RouteStorage,
    private val equipmentStorage: EquipmentStorage,
    private val locationStorage: LocationStorage,
    private val actionLogger: ActionLogger,
    private val coroutineDispatchers: CoroutineDispatchers,
) : RoutePointsRepository {

    override suspend fun getRoutePoints(inspectionId: String): Result<Pair<String, List<DomainRoutePoint>>> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val inspection = inspectionStorage.selectInspectionById(inspectionId)
                        ?: error("Inspection not found: $inspectionId")
                    val route = routeStorage.selectRouteById(inspection.routeId)
                    val routePoints = routeStorage.selectPointsByRouteId(inspection.routeId)
                    val equipmentResults = inspectionStorage.selectEquipmentResultsByInspectionId(inspectionId)

                    val points = routePoints.map { point ->
                        val equipment = equipmentStorage.selectById(point.equipmentId)
                        val locationName = equipment?.locationId?.let { locationStorage.selectById(it)?.name }
                        val result = equipmentResults.firstOrNull { it.routePointId == point.id }
                        DomainRoutePoint(
                            routePointId = point.id,
                            equipmentId = point.equipmentId,
                            equipmentCode = equipment?.code.orEmpty(),
                            equipmentName = equipment?.name.orEmpty(),
                            locationName = locationName.orEmpty(),
                            equipmentResultId = result?.id,
                            status = EquipmentResultStatus.fromString(
                                result?.status?.name ?: LocalEquipmentResultStatus.NOT_STARTED.name
                            ),
                            hasIssues = result?.status == LocalEquipmentResultStatus.SKIPPED,
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
                    val now = Clock.System.now().toString()
                    val results = inspectionStorage.selectEquipmentResultsByInspectionId(inspectionId)
                    val finalStatus = resolveInspectionFinalStatus(results)
                    inspectionStorage.updateInspectionStatus(
                        id = inspectionId,
                        status = finalStatus,
                        completedAt = now,
                        updatedAt = now,
                    )
                    actionLogger.log(
                        actionType = ActionLogType.INSPECTION_COMPLETED,
                        entityType = ActionLogEntityType.INSPECTION,
                        entityId = inspectionId,
                        payloadJson = """{"status":"${finalStatus.localValue}"}""",
                    )
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "finishInspection failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    private fun resolveInspectionFinalStatus(
        results: List<LocalEquipmentResult>,
    ): LocalInspectionStatus {
        if (results.isEmpty()) return LocalInspectionStatus.COMPLETED
        val anySkipped = results.any { it.status == LocalEquipmentResultStatus.SKIPPED }
        val allDone = results.all {
            it.status == LocalEquipmentResultStatus.COMPLETED ||
                it.status == LocalEquipmentResultStatus.SKIPPED
        }
        return when {
            !allDone -> LocalInspectionStatus.PARTIALLY_COMPLETED
            anySkipped -> LocalInspectionStatus.PARTIALLY_COMPLETED
            else -> LocalInspectionStatus.COMPLETED
        }
    }
}
