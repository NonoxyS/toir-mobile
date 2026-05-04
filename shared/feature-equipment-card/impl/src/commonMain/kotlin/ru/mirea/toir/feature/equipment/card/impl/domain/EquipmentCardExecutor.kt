package ru.mirea.toir.feature.equipment.card.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore.Intent
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore.Label
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore.State
import ru.mirea.toir.feature.equipment.card.impl.domain.EquipmentCardStoreFactory.Action
import ru.mirea.toir.feature.equipment.card.impl.domain.EquipmentCardStoreFactory.Message
import ru.mirea.toir.feature.equipment.card.impl.domain.repository.EquipmentCardRepository

internal class EquipmentCardExecutor(
    private val repository: EquipmentCardRepository,
    mainDispatcher: CoroutineDispatcher,
    private val inspectionId: String,
    private val routePointId: String,
) : BaseExecutor<Intent, Action, State, Message, Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteAction(action: Action) {
        when (action) {
            Action.Load -> loadCard()
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            Intent.OnOpenChecklist -> {
                val equipmentResultId = state().card?.equipmentResultId ?: return
                publish(Label.NavigateToChecklist(equipmentResultId))
            }
        }
    }

    private suspend fun loadCard() {
        dispatch(Message.SetLoading)
        repository.getOrCreateEquipmentResult(inspectionId, routePointId).fold(
            onSuccess = { card -> dispatch(Message.SetCard(card)) },
            onFailure = { dispatch(Message.SetError) },
        )
    }
}
