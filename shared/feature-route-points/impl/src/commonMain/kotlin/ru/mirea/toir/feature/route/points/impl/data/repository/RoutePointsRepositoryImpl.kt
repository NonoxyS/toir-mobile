@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.mirea.toir.feature.route.points.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
import ru.mirea.toir.core.domain.models.EquipmentResultStatus
import ru.mirea.toir.feature.route.points.impl.domain.repository.RoutePointsRepository
import ru.mirea.toir.sync.domain.SyncManager
import ru.mirea.toir.sync.domain.SyncTrigger

internal class RoutePointsRepositoryImpl(
    private val inspectionStorage: InspectionStorage,
    private val routeStorage: RouteStorage,
    private val equipmentStorage: EquipmentStorage,
    private val locationStorage: LocationStorage,
    private val actionLogger: ActionLogger,
    private val syncManager: SyncManager,
    private val coroutineDispatchers: CoroutineDispatchers,
) : RoutePointsRepository {

    override fun observeRoutePoints(
        inspectionId: String,
    ): Flow<Pair<String, List<DomainRoutePoint>>> = flow {
        val inspection = inspectionStorage.selectInspectionById(inspectionId)
            ?: error("Inspection not found: $inspectionId")
        // Reference data (route, equipment, location) is stable for the lifetime of a
        // subscription — fetched once on the io dispatcher via flow {} + flowOn(io). Routes
        // don't change while a user inspects, so combining a third Flow would only add noise.
        val route = routeStorage.selectRouteById(inspection.routeId)
        val routeName = route?.name.orEmpty()

        val pointsFlow = routeStorage.observePointsByRouteId(inspection.routeId)
        val equipmentResultsFlow =
            inspectionStorage.observeEquipmentResultsByInspectionId(inspectionId)

        emitAll(
            combine(pointsFlow, equipmentResultsFlow) { points, results ->
                val resultByPoint = results.associateBy { it.routePointId }
                val domainPoints = points.map { point ->
                    val equipment = equipmentStorage.selectById(point.equipmentId)
                    val locationName = equipment?.locationId
                        ?.let { locationStorage.selectById(it)?.name }
                        .orEmpty()
                    val result = resultByPoint[point.id]
                    DomainRoutePoint(
                        routePointId = point.id,
                        equipmentId = point.equipmentId,
                        equipmentCode = equipment?.code.orEmpty(),
                        equipmentName = equipment?.name.orEmpty(),
                        locationName = locationName,
                        equipmentResultId = result?.id,
                        status = result?.status?.toDomain() ?: EquipmentResultStatus.NOT_STARTED,
                        hasIssues = result?.status == LocalEquipmentResultStatus.SKIPPED,
                    )
                }
                routeName to domainPoints
            }
        )
    }.flowOn(coroutineDispatchers.io)

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
                    syncManager.syncNow(SyncTrigger.AfterInspection)
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

private fun LocalEquipmentResultStatus.toDomain(): EquipmentResultStatus = when (this) {
    LocalEquipmentResultStatus.NOT_STARTED -> EquipmentResultStatus.NOT_STARTED
    LocalEquipmentResultStatus.IN_PROGRESS -> EquipmentResultStatus.IN_PROGRESS
    LocalEquipmentResultStatus.COMPLETED -> EquipmentResultStatus.COMPLETED
    LocalEquipmentResultStatus.SKIPPED -> EquipmentResultStatus.SKIPPED
}

