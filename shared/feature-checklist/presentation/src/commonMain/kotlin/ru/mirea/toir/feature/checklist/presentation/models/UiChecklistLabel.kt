package ru.mirea.toir.feature.checklist.presentation.models

sealed interface UiChecklistLabel {
    data class NavigateToPhotoCapture(val checklistItemResultId: String) : UiChecklistLabel
    data object ChecklistCompleted : UiChecklistLabel
}
