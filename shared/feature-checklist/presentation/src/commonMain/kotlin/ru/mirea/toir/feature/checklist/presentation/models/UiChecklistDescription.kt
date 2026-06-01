package ru.mirea.toir.feature.checklist.presentation.models

import androidx.compose.runtime.Immutable

@Immutable
data class UiChecklistDescription(
    val title: String,
    val description: String,
)
