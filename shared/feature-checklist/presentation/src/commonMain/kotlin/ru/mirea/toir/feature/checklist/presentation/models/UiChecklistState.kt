package ru.mirea.toir.feature.checklist.presentation.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class UiChecklistState(
    val items: ImmutableList<UiChecklistItem> = persistentListOf(),
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val isValidationError: Boolean = false,
    val isPhotoValidationError: Boolean = false,
    val isOutOfRangeError: Boolean = false,
    val isInvalidNumberError: Boolean = false,
    val isCompleted: Boolean = false,
)
