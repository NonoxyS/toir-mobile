package ru.mirea.toir.feature.checklist.presentation.mappers

import kotlinx.collections.immutable.toImmutableList
import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.checklist.api.models.DomainAnswerType
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore
import ru.mirea.toir.feature.checklist.presentation.models.UiAnswerType
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistState

interface UiChecklistStateMapper : Mapper<ChecklistStore.State, UiChecklistState>

internal class UiChecklistStateMapperImpl : UiChecklistStateMapper {

    override fun map(item: ChecklistStore.State): UiChecklistState = UiChecklistState(
        items = item.items
            .map {
                it.toUi(
                    showValidationErrors = item.isValidationError,
                    numberDraft = item.numberDrafts[it.id],
                )
            }
            .toImmutableList(),
        isLoading = item.isLoading,
        isError = item.isError,
        isValidationError = item.isValidationError,
        isPhotoValidationError = item.isPhotoValidationError,
        isCompleted = item.isCompleted,
    )

    private fun DomainChecklistItem.toUi(
        showValidationErrors: Boolean,
        numberDraft: String?,
    ): UiChecklistItem {
        val showValidationError = showValidationErrors && isRequired && !isAnswered()
        val effectiveNumberText = numberDraft ?: valueNumber?.formatNumber().orEmpty()
        val parsedNumber = effectiveNumberText.replace(',', '.').toDoubleOrNull()
        val min = numericMin
        val max = numericMax
        val isNumberOutOfRange = answerType is DomainAnswerType.Number && parsedNumber != null && (
            (min != null && parsedNumber < min) || (max != null && parsedNumber > max)
            )
        return UiChecklistItem(
            id = id,
            title = title,
            description = description,
            answerType = answerType.toUi(),
            isRequired = isRequired,
            requiresPhoto = requiresPhoto,
            resultId = resultId,
            valueBoolean = valueBoolean,
            valueNumber = effectiveNumberText,
            valueText = valueText.orEmpty(),
            valueSelect = valueSelect,
            isConfirmed = isConfirmed,
            photoCount = photoCount,
            numericMin = numericMin?.formatNumber(),
            numericMax = numericMax?.formatNumber(),
            showValidationError = showValidationError,
            isNumberOutOfRange = isNumberOutOfRange,
        )
    }

    private fun DomainChecklistItem.isAnswered(): Boolean = when (answerType) {
        DomainAnswerType.Boolean -> valueBoolean != null
        DomainAnswerType.Number -> valueNumber != null
        DomainAnswerType.Text -> !valueText.isNullOrBlank()
        is DomainAnswerType.Select -> !valueSelect.isNullOrBlank()
        DomainAnswerType.Confirm -> isConfirmed
    }

    private fun DomainAnswerType.toUi(): UiAnswerType = when (this) {
        DomainAnswerType.Boolean -> UiAnswerType.Boolean
        DomainAnswerType.Number -> UiAnswerType.Number
        DomainAnswerType.Text -> UiAnswerType.Text
        is DomainAnswerType.Select -> UiAnswerType.Select(options.toImmutableList())
        DomainAnswerType.Confirm -> UiAnswerType.Confirm
    }

    private fun Double.formatNumber(): String =
        if (this % 1.0 == 0.0) toLong().toString() else toString()
}
