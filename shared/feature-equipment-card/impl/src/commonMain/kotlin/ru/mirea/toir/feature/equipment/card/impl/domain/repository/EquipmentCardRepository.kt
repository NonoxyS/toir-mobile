package ru.mirea.toir.feature.equipment.card.impl.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.feature.equipment.card.api.models.DomainEquipmentCard

internal interface EquipmentCardRepository {
    suspend fun ensureEquipmentResult(inspectionId: String, routePointId: String): Result<Unit>

    fun observeEquipmentCard(
        inspectionId: String,
        routePointId: String,
    ): Flow<DomainEquipmentCard>
}
