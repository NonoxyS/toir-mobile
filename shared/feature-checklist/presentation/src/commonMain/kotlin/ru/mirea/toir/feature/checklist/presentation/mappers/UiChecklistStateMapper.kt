package ru.mirea.toir.feature.checklist.presentation.mappers

import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.collections.immutable.toImmutableList
import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistDescription
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistState
import ru.mirea.toir.res.MR

interface UiChecklistStateMapper : Mapper<ChecklistStore.State, UiChecklistState>

internal class UiChecklistStateMapperImpl : UiChecklistStateMapper {

    override fun map(item: ChecklistStore.State): UiChecklistState = UiChecklistState(
        items = item.items
            .map {
                it.toUi(
                    showValidationErrors = item.isValidationError,
                    invalidNumberInputs = item.invalidNumberInputs,
                )
            }
            .toImmutableList(),
        isLoading = item.isLoading,
        isError = item.isError,
        isValidationError = item.isValidationError,
        isPhotoValidationError = item.isPhotoValidationError,
        isOutOfRangeError = item.isOutOfRangeError,
        isInvalidNumberError = item.isInvalidNumberError,
        isCompleted = item.isCompleted,
        openDescription = item.openDescriptionItemId
            ?.let { id -> item.items.firstOrNull { it.id == id } }
            ?.takeIf { !it.description.isNullOrBlank() }
            ?.let { domainItem ->
                UiChecklistDescription(
                    title = domainItem.title,
                    description = domainItem.description.orEmpty(),
                )
            },
    )

    private fun DomainChecklistItem.toUi(
        showValidationErrors: Boolean,
        invalidNumberInputs: Map<String, String>,
    ): UiChecklistItem {
        val showValidationError = showValidationErrors && isRequired && !isAnswered
        return when (this) {
            is DomainChecklistItem.BooleanItem -> UiChecklistItem.BooleanItem(
                id = id,
                title = title,
                description = description,
                isRequired = isRequired,
                requiresPhoto = requiresPhoto,
                canAddPhoto = resultId != null,
                photoCount = photoCount,
                showValidationError = showValidationError,
                value = value,
            )

            is DomainChecklistItem.NumberItem -> {
                val minValue = min
                val maxValue = max
                val rangeHint: StringDesc? = when {
                    minValue != null && maxValue != null -> StringDesc.ResourceFormatted(
                        MR.strings.checklist_number_hint_range,
                        minValue.formatNumber(),
                        maxValue.formatNumber(),
                    )
                    minValue != null -> StringDesc.ResourceFormatted(
                        MR.strings.checklist_number_hint_min,
                        minValue.formatNumber(),
                    )
                    maxValue != null -> StringDesc.ResourceFormatted(
                        MR.strings.checklist_number_hint_max,
                        maxValue.formatNumber(),
                    )
                    else -> null
                }
                val invalidRaw = invalidNumberInputs[id]
                UiChecklistItem.NumberItem(
                    id = id,
                    title = title,
                    description = description,
                    isRequired = isRequired,
                    requiresPhoto = requiresPhoto,
                    canAddPhoto = resultId != null,
                    photoCount = photoCount,
                    showValidationError = showValidationError,
                    value = invalidRaw ?: value?.formatNumber().orEmpty(),
                    rangeHint = rangeHint,
                    isOutOfRange = isOutOfRange,
                    isInvalidNumber = invalidRaw != null,
                )
            }

            is DomainChecklistItem.TextItem -> UiChecklistItem.TextItem(
                id = id,
                title = title,
                description = description,
                isRequired = isRequired,
                requiresPhoto = requiresPhoto,
                canAddPhoto = resultId != null,
                photoCount = photoCount,
                showValidationError = showValidationError,
                value = value.orEmpty(),
            )

            is DomainChecklistItem.SelectItem -> UiChecklistItem.SelectItem(
                id = id,
                title = title,
                description = description,
                isRequired = isRequired,
                requiresPhoto = requiresPhoto,
                canAddPhoto = resultId != null,
                photoCount = photoCount,
                showValidationError = showValidationError,
                value = value,
                options = options.toImmutableList(),
            )

            is DomainChecklistItem.ConfirmItem -> UiChecklistItem.ConfirmItem(
                id = id,
                title = title,
                description = description,
                isRequired = isRequired,
                requiresPhoto = requiresPhoto,
                canAddPhoto = resultId != null,
                photoCount = photoCount,
                showValidationError = showValidationError,
                value = isConfirmed,
            )
        }
    }

    private fun Double.formatNumber(): String =
        if (this % 1.0 == 0.0) toLong().toString() else toString()
}
