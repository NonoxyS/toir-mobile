package ru.mirea.toir.feature.checklist.presentation.mappers

import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.collections.immutable.toImmutableList
import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.checklist.api.models.DomainAnswerType
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistState
import ru.mirea.toir.res.MR

interface UiChecklistStateMapper : Mapper<ChecklistStore.State, UiChecklistState>

internal class UiChecklistStateMapperImpl : UiChecklistStateMapper {

    override fun map(item: ChecklistStore.State): UiChecklistState = UiChecklistState(
        items = item.items
            .map { it.toUi(showValidationErrors = item.isValidationError) }
            .toImmutableList(),
        isLoading = item.isLoading,
        isError = item.isError,
        isValidationError = item.isValidationError,
        isPhotoValidationError = item.isPhotoValidationError,
        isOutOfRangeError = item.isOutOfRangeError,
        isCompleted = item.isCompleted,
    )

    private fun DomainChecklistItem.toUi(showValidationErrors: Boolean): UiChecklistItem {
        val showValidationError = showValidationErrors && isRequired && !isAnswered()
        return when (val type = answerType) {
            DomainAnswerType.Boolean -> UiChecklistItem.Boolean(
                id = id,
                title = title,
                description = description,
                isRequired = isRequired,
                requiresPhoto = requiresPhoto,
                photoCount = photoCount,
                showValidationError = showValidationError,
                value = valueBoolean,
            )

            DomainAnswerType.Number -> {
                val min = numericMin
                val max = numericMax
                val value = valueNumber
                val isOutOfRange = value != null && (
                    (min != null && value < min) || (max != null && value > max)
                    )
                val rangeHint: StringDesc? = when {
                    min != null && max != null -> StringDesc.ResourceFormatted(
                        MR.strings.checklist_number_hint_range,
                        min.formatNumber(),
                        max.formatNumber(),
                    )
                    min != null -> StringDesc.ResourceFormatted(
                        MR.strings.checklist_number_hint_min,
                        min.formatNumber(),
                    )
                    max != null -> StringDesc.ResourceFormatted(
                        MR.strings.checklist_number_hint_max,
                        max.formatNumber(),
                    )
                    else -> null
                }
                UiChecklistItem.Number(
                    id = id,
                    title = title,
                    description = description,
                    isRequired = isRequired,
                    requiresPhoto = requiresPhoto,
                    photoCount = photoCount,
                    showValidationError = showValidationError,
                    value = value?.formatNumber().orEmpty(),
                    rangeHint = rangeHint,
                    isOutOfRange = isOutOfRange,
                )
            }

            DomainAnswerType.Text -> UiChecklistItem.Text(
                id = id,
                title = title,
                description = description,
                isRequired = isRequired,
                requiresPhoto = requiresPhoto,
                photoCount = photoCount,
                showValidationError = showValidationError,
                value = valueText.orEmpty(),
            )

            is DomainAnswerType.Select -> UiChecklistItem.Select(
                id = id,
                title = title,
                description = description,
                isRequired = isRequired,
                requiresPhoto = requiresPhoto,
                photoCount = photoCount,
                showValidationError = showValidationError,
                value = valueSelect,
                options = type.options.toImmutableList(),
            )

            DomainAnswerType.Confirm -> UiChecklistItem.Confirm(
                id = id,
                title = title,
                description = description,
                isRequired = isRequired,
                requiresPhoto = requiresPhoto,
                photoCount = photoCount,
                showValidationError = showValidationError,
                value = isConfirmed,
            )
        }
    }

    private fun DomainChecklistItem.isAnswered(): Boolean = when (answerType) {
        DomainAnswerType.Boolean -> valueBoolean != null
        DomainAnswerType.Number -> valueNumber != null
        DomainAnswerType.Text -> !valueText.isNullOrBlank()
        is DomainAnswerType.Select -> !valueSelect.isNullOrBlank()
        DomainAnswerType.Confirm -> isConfirmed
    }

    private fun Double.formatNumber(): String =
        if (this % 1.0 == 0.0) toLong().toString() else toString()
}
