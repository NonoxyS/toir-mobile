package ru.mirea.toir.feature.checklist.api.store

import com.arkivanov.mvikotlin.core.store.Store
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore.Intent
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore.Label
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore.State

interface ChecklistStore : Store<Intent, State, Label> {

    data class State(
        val equipmentResultId: String = "",
        val items: List<DomainChecklistItem> = emptyList(),
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val isValidationError: Boolean = false,
        val isPhotoValidationError: Boolean = false,
        val isOutOfRangeError: Boolean = false,
        val isCompleted: Boolean = false,
    )

    sealed interface Intent {
        data class OnBooleanAnswer(val itemId: String, val value: Boolean?) : Intent
        data class OnNumberAnswer(val itemId: String, val value: String) : Intent
        data class OnTextAnswer(val itemId: String, val value: String) : Intent
        data class OnSelectAnswer(val itemId: String, val value: String) : Intent
        data class OnConfirm(val itemId: String, val value: Boolean) : Intent
        data class OnAddPhoto(val itemId: String) : Intent
        data object OnFinishChecklist : Intent
    }

    sealed interface Label {
        data class NavigateToPhotoCapture(val checklistItemResultId: String) : Label
        data object ChecklistCompleted : Label
    }
}
