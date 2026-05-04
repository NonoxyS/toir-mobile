package ru.mirea.toir.feature.equipment.card.impl.data.repository

import io.github.aakira.napier.Napier
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
    override suspend fun getOrCreateEquipmentResult(
        inspectionId: String,
        routePointId: String,
    ): Result<DomainEquipmentCard> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val routePoint = routeStorage.selectPointById(routePointId)
                        ?: error("RoutePoint not found: $routePointId")
                    val equipment = equipmentStorage.selectById(routePoint.equipmentId)
                        ?: error("Equipment not found: ${routePoint.equipmentId}")
                    val locationName = equipment.locationId
                        ?.let { locationStorage.selectById(it)?.name }
                        .orEmpty()

                    var result = inspectionStorage.selectEquipmentResultByRoutePoint(routePointId, inspectionId)
                    if (result == null) {
                        val newId = Uuid.random().toString()
                        val now = Clock.System.now().toString()
                        inspectionStorage.insertEquipmentResult(
                            id = newId,
                            inspectionId = inspectionId,
                            routePointId = routePointId,
                            equipmentId = equipment.id,
                            status = LocalEquipmentResultStatus.IN_PROGRESS,
                            createdAt = now,
                            updatedAt = now,
                        )
                        result = inspectionStorage.selectEquipmentResultById(newId)
                            ?: error("Failed to create equipment result")
                        actionLogger.log(
                            actionType = ActionLogType.ROUTE_POINT_OPENED,
                            entityType = ActionLogEntityType.ROUTE_POINT,
                            entityId = routePointId,
                        )
                    }
                    actionLogger.log(
                        actionType = ActionLogType.EQUIPMENT_OPENED,
                        entityType = ActionLogEntityType.EQUIPMENT,
                        entityId = equipment.id,
                    )

                    DomainEquipmentCard(
                        equipmentId = equipment.id,
                        code = equipment.code,
                        name = equipment.name,
                        type = equipment.type,
                        locationName = locationName,
                        equipmentResultId = result.id,
                        inspectionStatus = EquipmentResultStatus.fromString(result.status.name),
                    ).wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "getOrCreateEquipmentResult failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
