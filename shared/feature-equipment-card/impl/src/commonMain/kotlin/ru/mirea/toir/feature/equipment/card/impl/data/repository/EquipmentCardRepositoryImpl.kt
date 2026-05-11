@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.mirea.toir.feature.equipment.card.impl.data.repository

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
import ru.mirea.toir.core.database.storage.action_log.ActionLogEntityType
import ru.mirea.toir.core.database.storage.action_log.ActionLogType
import ru.mirea.toir.core.database.storage.action_log.ActionLogger
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorage
import ru.mirea.toir.core.database.storage.inspection.InspectionStorage
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.location.LocationStorage
import ru.mirea.toir.core.database.storage.route.RouteStorage
import ru.mirea.toir.feature.equipment.card.api.models.DomainEquipmentCard
import ru.mirea.toir.feature.equipment.card.api.models.EquipmentResultStatus
import ru.mirea.toir.feature.equipment.card.impl.domain.repository.EquipmentCardRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class EquipmentCardRepositoryImpl(
    private val inspectionStorage: InspectionStorage,
    private val routeStorage: RouteStorage,
    private val equipmentStorage: EquipmentStorage,
    private val locationStorage: LocationStorage,
    private val actionLogger: ActionLogger,
    private val coroutineDispatchers: CoroutineDispatchers,
) : EquipmentCardRepository {

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun ensureEquipmentResult(
        inspectionId: String,
        routePointId: String,
    ): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val routePoint = routeStorage.selectPointById(routePointId)
                        ?: error("RoutePoint not found: $routePointId")
                    val existing = inspectionStorage
                        .selectEquipmentResultByRoutePoint(routePointId, inspectionId)
                    if (existing == null) {
                        val now = Clock.System.now().toString()
                        inspectionStorage.insertEquipmentResult(
                            id = Uuid.random().toString(),
                            inspectionId = inspectionId,
                            routePointId = routePointId,
                            equipmentId = routePoint.equipmentId,
                            status = LocalEquipmentResultStatus.IN_PROGRESS,
                            createdAt = now,
                            updatedAt = now,
                        )
                        actionLogger.log(
                            actionType = ActionLogType.ROUTE_POINT_OPENED,
                            entityType = ActionLogEntityType.ROUTE_POINT,
                            entityId = routePointId,
                        )
                    }
                    actionLogger.log(
                        actionType = ActionLogType.EQUIPMENT_OPENED,
                        entityType = ActionLogEntityType.EQUIPMENT,
                        entityId = routePoint.equipmentId,
                    )
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "ensureEquipmentResult failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override fun observeEquipmentCard(
        inspectionId: String,
        routePointId: String,
    ): Flow<DomainEquipmentCard> = flow {
        val routePoint = routeStorage.selectPointById(routePointId)
            ?: error("RoutePoint not found: $routePointId")
        // Equipment + location reference data is stable for the lifetime of a subscription —
        // fetched once on the io dispatcher inside flow {} + flowOn(io). The reactive parts
        // are the equipment row (renames/edits) and the equipment result (status updates).
        val equipmentFlow = equipmentStorage.observeEquipmentById(routePoint.equipmentId)
        val resultFlow = inspectionStorage.observeEquipmentResultByRoutePoint(
            routePointId = routePointId,
            inspectionId = inspectionId,
        )

        emitAll(
            combine(equipmentFlow, resultFlow) { equipment, result ->
                val safeEquipment = equipment
                    ?: error("Equipment not found: ${routePoint.equipmentId}")
                val locationName = safeEquipment.locationId
                    ?.let { locationStorage.selectById(it)?.name }
                    .orEmpty()
                val statusName = result?.status?.name
                    ?: LocalEquipmentResultStatus.NOT_STARTED.name
                DomainEquipmentCard(
                    equipmentId = safeEquipment.id,
                    code = safeEquipment.code,
                    name = safeEquipment.name,
                    type = safeEquipment.type,
                    locationName = locationName,
                    equipmentResultId = result?.id.orEmpty(),
                    inspectionStatus = EquipmentResultStatus.fromString(statusName),
                )
            }
        )
    }.flowOn(coroutineDispatchers.io)
}
