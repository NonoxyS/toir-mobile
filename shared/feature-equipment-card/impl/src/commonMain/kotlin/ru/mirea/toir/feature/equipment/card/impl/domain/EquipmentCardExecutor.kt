package ru.mirea.toir.feature.equipment.card.impl.domain

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import ru.mirea.toir.common.extensions.safeCatch
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
    private var subscriptionJob: Job? = null

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
        repository.ensureEquipmentResult(inspectionId, routePointId).fold(
            onSuccess = { subscribeToEquipmentCard(inspectionId, routePointId) },
            onFailure = { throwable ->
                Napier.e(message = "ensureEquipmentResult failed", throwable = throwable)
                dispatch(Message.SetError)
            },
        )
    }

    private fun subscribeToEquipmentCard(inspectionId: String, routePointId: String) {
        subscriptionJob?.cancel()
        subscriptionJob = repository.observeEquipmentCard(inspectionId, routePointId)
            .onStart { dispatch(Message.SetLoading) }
            .onEach { card -> dispatch(Message.SetCard(card)) }
            .safeCatch { throwable ->
                Napier.e(message = "observeEquipmentCard failed", throwable = throwable)
                dispatch(Message.SetError)
            }
            .launchIn(scope)
    }
}
