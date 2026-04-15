package ru.mirea.toir.feature.equipment.card.impl.domain.repository

import ru.mirea.toir.feature.equipment.card.api.models.DomainEquipmentCard

internal interface EquipmentCardRepository {
    suspend fun getOrCreateEquipmentResult(inspectionId: String, routePointId: String): Result<DomainEquipmentCard>
}
