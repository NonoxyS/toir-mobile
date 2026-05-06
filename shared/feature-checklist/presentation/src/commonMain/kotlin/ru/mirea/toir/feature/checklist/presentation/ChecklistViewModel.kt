package ru.mirea.toir.feature.checklist.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.map
import ru.mirea.toir.core.presentation.viewmodel.BaseViewModel
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore.Intent
import ru.mirea.toir.feature.checklist.presentation.mappers.UiChecklistLabelMapper
import ru.mirea.toir.feature.checklist.presentation.mappers.UiChecklistStateMapper
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistLabel
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistState

class ChecklistViewModel internal constructor(
    private val store: ChecklistStore,
    stateMapper: UiChecklistStateMapper,
    labelMapper: UiChecklistLabelMapper,
) : BaseViewModel<UiChecklistState, UiChecklistLabel>(initialState = UiChecklistState()) {

    init {
        bindAndStart {
            store.states.map(stateMapper::map) bindTo ::acceptState
            store.labels.map(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onBooleanAnswer(itemId: String, value: Boolean?) =
        store.accept(Intent.OnBooleanAnswer(itemId, value))

    fun onNumberAnswer(itemId: String, value: String) =
        store.accept(Intent.OnNumberAnswer(itemId, value))

    fun onTextAnswer(itemId: String, value: String) =
        store.accept(Intent.OnTextAnswer(itemId, value))

    fun onSelectAnswer(itemId: String, value: String) =
        store.accept(Intent.OnSelectAnswer(itemId, value))

    fun onConfirm(itemId: String, value: Boolean) = store.accept(Intent.OnConfirm(itemId, value))

    fun onAddPhoto(itemId: String) = store.accept(Intent.OnAddPhoto(itemId))

    fun onFinishChecklist() = store.accept(Intent.OnFinishChecklist)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
