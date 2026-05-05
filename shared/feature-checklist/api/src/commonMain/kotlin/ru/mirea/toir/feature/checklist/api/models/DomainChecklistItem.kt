package ru.mirea.toir.feature.checklist.api.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface DomainAnswerType {
    data object Boolean : DomainAnswerType
    data object Number : DomainAnswerType
    data object Text : DomainAnswerType
    data class Select(val options: ImmutableList<String>) : DomainAnswerType
    data object Confirm : DomainAnswerType
}

@Immutable
data class DomainChecklistItem(
    val id: String,
    val title: String,
    val description: String?,
    val answerType: DomainAnswerType,
    val isRequired: Boolean,
    val requiresPhoto: Boolean,
    val resultId: String?,
    val valueBoolean: Boolean?,
    val valueNumber: Double?,
    val valueText: String?,
    val valueSelect: String?,
    val isConfirmed: Boolean,
    val photoCount: Int,
    val numericMin: Double?,
    val numericMax: Double?,
)
