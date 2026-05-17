package ru.mirea.toir.feature.checklist.impl.domain

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import ru.mirea.toir.common.extensions.safeCatch
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore.Intent
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore.Label
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore.State
import ru.mirea.toir.feature.checklist.impl.domain.ChecklistStoreFactory.Action
import ru.mirea.toir.feature.checklist.impl.domain.ChecklistStoreFactory.Message
import ru.mirea.toir.feature.checklist.impl.domain.repository.ChecklistRepository

internal class ChecklistExecutor(
    private val repository: ChecklistRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<Intent, Action, State, Message, Label>(
    mainContext = mainDispatcher,
) {
    private var subscriptionJob: Job? = null

    override suspend fun suspendExecuteAction(action: Action) {
        when (action) {
            Action.Load -> subscribeToChecklist(state().equipmentResultId)
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.OnBooleanAnswer -> {
                repository.saveBooleanAnswer(state().equipmentResultId, intent.itemId, intent.value)
            }

            is Intent.OnNumberAnswer -> {
                val number = intent.value.replace(',', '.').toDoubleOrNull() ?: return
                repository.saveNumberAnswer(state().equipmentResultId, intent.itemId, number)
            }

            is Intent.OnTextAnswer -> {
                repository.saveTextAnswer(state().equipmentResultId, intent.itemId, intent.value)
            }

            is Intent.OnSelectAnswer -> {
                repository.saveSelectAnswer(state().equipmentResultId, intent.itemId, intent.value)
            }

            is Intent.OnConfirm -> {
                repository.saveConfirm(state().equipmentResultId, intent.itemId, intent.value)
            }

            is Intent.OnAddPhoto -> {
                val item = state().items.firstOrNull { it.id == intent.itemId } ?: return
                val resultId = item.resultId ?: return
                publish(Label.NavigateToPhotoCapture(resultId))
            }

            Intent.OnFinishChecklist -> finishChecklist()
        }
    }

    private fun subscribeToChecklist(equipmentResultId: String) {
        subscriptionJob?.cancel()
        subscriptionJob = repository.observeChecklistItems(equipmentResultId)
            .onStart { dispatch(Message.SetLoading) }
            .onEach { items -> dispatch(Message.SetItems(items)) }
            .safeCatch { throwable ->
                Napier.e(message = "observeChecklistItems failed", throwable = throwable)
                dispatch(Message.SetError)
            }
            .launchIn(scope)
    }

    private suspend fun finishChecklist() {
        val currentState = state()
        val items = currentState.items
        val missingRequired = items.any { item -> item.isRequired && !item.isAnswered }
        if (missingRequired) {
            dispatch(Message.SetValidationRequiredError)
            return
        }
        val hasOutOfRange = items.any { item ->
            item is DomainChecklistItem.NumberItem && item.isOutOfRange
        }
        if (hasOutOfRange) {
            dispatch(Message.SetValidationOutOfRangeError)
            return
        }
        val missingPhoto = items.any { item ->
            item.requiresPhoto && item.photoCount == 0 && item.isAnswered
        }
        if (missingPhoto) {
            dispatch(Message.SetValidationPhotoError)
            return
        }
        dispatch(Message.ClearValidationError)
        repository.finishChecklist(currentState.equipmentResultId).fold(
            onSuccess = {
                dispatch(Message.SetCompleted)
                publish(Label.ChecklistCompleted)
            },
            onFailure = {
                dispatch(Message.SetError)
            },
        )
    }
}
