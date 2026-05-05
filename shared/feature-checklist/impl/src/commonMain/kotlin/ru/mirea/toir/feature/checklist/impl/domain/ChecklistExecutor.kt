package ru.mirea.toir.feature.checklist.impl.domain

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.checklist.api.models.DomainAnswerType
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

    override suspend fun suspendExecuteAction(action: Action) {
        when (action) {
            Action.Load -> loadItems()
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.OnBooleanAnswer -> {
                repository.saveBooleanAnswer(state().equipmentResultId, intent.itemId, intent.value)
                reloadItems()
            }

            is Intent.OnNumberAnswer -> {
                val number = intent.value.replace(',', '.').toDoubleOrNull() ?: return
                val item = state().items.firstOrNull { it.id == intent.itemId } ?: return
                val min = item.numericMin
                val max = item.numericMax
                if ((min != null && number < min) || (max != null && number > max)) return
                repository.saveNumberAnswer(state().equipmentResultId, intent.itemId, number)
                reloadItems()
            }

            is Intent.OnTextAnswer -> {
                repository.saveTextAnswer(state().equipmentResultId, intent.itemId, intent.value)
                reloadItems()
            }

            is Intent.OnSelectAnswer -> {
                repository.saveSelectAnswer(state().equipmentResultId, intent.itemId, intent.value)
                reloadItems()
            }

            is Intent.OnConfirm -> {
                repository.saveConfirm(state().equipmentResultId, intent.itemId)
                reloadItems()
            }

            is Intent.OnAddPhoto -> {
                val item = state().items.firstOrNull { it.id == intent.itemId } ?: return
                val resultId = item.resultId ?: return
                publish(Label.NavigateToPhotoCapture(resultId))
            }

            Intent.OnFinishChecklist -> finishChecklist()
        }
    }

    private suspend fun loadItems() {
        dispatch(Message.SetLoading)
        val equipmentResultId = state().equipmentResultId
        repository.getChecklistItems(equipmentResultId).fold(
            onSuccess = { items ->
                dispatch(Message.SetItems(items.toImmutableList()))
            },
            onFailure = {
                dispatch(Message.SetError)
            },
        )
    }

    private suspend fun reloadItems() {
        val equipmentResultId = state().equipmentResultId
        repository.getChecklistItems(equipmentResultId).fold(
            onSuccess = { items ->
                dispatch(Message.SetItems(items.toImmutableList()))
            },
            onFailure = {
                dispatch(Message.SetError)
            },
        )
    }

    private suspend fun finishChecklist() {
        val currentState = state()
        val items = currentState.items
        val missingRequired = items.any { item -> item.isRequired && !item.isAnswered() }
        if (missingRequired) {
            dispatch(Message.SetValidationRequiredError)
            return
        }
        val missingPhoto = items.any { item ->
            item.requiresPhoto && item.photoCount == 0 && item.isAnswered()
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

    private fun DomainChecklistItem.isAnswered(): Boolean = when (answerType) {
        DomainAnswerType.Boolean -> valueBoolean != null
        DomainAnswerType.Number -> valueNumber != null
        DomainAnswerType.Text -> !valueText.isNullOrBlank()
        is DomainAnswerType.Select -> !valueSelect.isNullOrBlank()
        DomainAnswerType.Confirm -> isConfirmed
    }
}
