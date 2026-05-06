package ru.mirea.toir.feature.checklist.presentation.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface UiAnswerType {
    data object Boolean : UiAnswerType
    data object Number : UiAnswerType
    data object Text : UiAnswerType
    data class Select(val options: ImmutableList<String>) : UiAnswerType
    data object Confirm : UiAnswerType
}

@Immutable
data class UiChecklistItem(
    val id: String,
    val title: String,
    val description: String?,
    val answerType: UiAnswerType,
    val isRequired: Boolean,
    val requiresPhoto: Boolean,
    val resultId: String?,
    val valueBoolean: Boolean?,
    val valueNumber: String,
    val valueText: String,
    val valueSelect: String?,
    val isConfirmed: Boolean,
    val photoCount: Int,
    val numericMin: String?,
    val numericMax: String?,
    val showValidationError: Boolean,
)
