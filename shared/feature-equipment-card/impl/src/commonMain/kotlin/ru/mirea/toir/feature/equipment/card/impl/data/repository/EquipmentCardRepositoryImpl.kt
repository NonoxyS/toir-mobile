package ru.mirea.toir.feature.equipment.card.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorage
import ru.mirea.toir.core.database.storage.inspection.InspectionStorage
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
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
    private val coroutineDispatchers: CoroutineDispatchers,
) : EquipmentCardRepository {

    @OptIn(ExperimentalUuidApi::class)
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

                    var result = inspectionStorage.selectEquipmentResultByRoutePoint(routePointId, inspectionId)
                    if (result == null) {
                        val newId = Uuid.random().toString()
                        inspectionStorage.insertEquipmentResult(
                            id = newId,
                            inspectionId = inspectionId,
                            routePointId = routePointId,
                            equipmentId = equipment.id,
                            status = LocalEquipmentResultStatus.IN_PROGRESS,
                        )
                        result = inspectionStorage.selectEquipmentResultById(newId)
                            ?: error("Failed to create equipment result")
                    }

                    DomainEquipmentCard(
                        equipmentId = equipment.id,
                        code = equipment.code,
                        name = equipment.name,
                        type = equipment.type,
                        locationName = equipment.locationId.orEmpty(),
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
