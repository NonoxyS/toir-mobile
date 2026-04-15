package ru.mirea.toir.feature.equipment.card.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.dao.EquipmentDao
import ru.mirea.toir.core.database.dao.InspectionDao
import ru.mirea.toir.core.database.dao.RouteDao
import ru.mirea.toir.feature.equipment.card.api.models.DomainEquipmentCard
import ru.mirea.toir.feature.equipment.card.impl.domain.repository.EquipmentCardRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class EquipmentCardRepositoryImpl(
    private val inspectionDao: InspectionDao,
    private val routeDao: RouteDao,
    private val equipmentDao: EquipmentDao,
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
                    val routePoint = routeDao.selectPointById(routePointId)
                        ?: error("RoutePoint not found: $routePointId")
                    val equipment = equipmentDao.selectById(routePoint.equipment_id)
                        ?: error("Equipment not found: ${routePoint.equipment_id}")

                    var result = inspectionDao.selectEquipmentResultByRoutePoint(routePointId, inspectionId)
                    if (result == null) {
                        val newId = Uuid.random().toString()
                        inspectionDao.insertEquipmentResult(
                            id = newId,
                            inspectionId = inspectionId,
                            routePointId = routePointId,
                            equipmentId = equipment.id,
                            status = "IN_PROGRESS",
                        )
                        result = inspectionDao.selectEquipmentResultById(newId)
                            ?: error("Failed to create equipment result")
                    }

                    DomainEquipmentCard(
                        equipmentId = equipment.id,
                        code = equipment.code,
                        name = equipment.name,
                        type = equipment.type,
                        locationName = equipment.location_id,
                        equipmentResultId = result.id,
                        inspectionStatus = result.status,
                    ).wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "getOrCreateEquipmentResult failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
