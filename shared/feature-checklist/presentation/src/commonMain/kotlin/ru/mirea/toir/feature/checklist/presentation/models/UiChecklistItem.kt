package ru.mirea.toir.feature.checklist.presentation.models

import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.collections.immutable.ImmutableList

sealed interface UiChecklistItem {
    val id: String
    val title: String
    val description: String?
    val isRequired: Boolean
    val requiresPhoto: Boolean
    val canAddPhoto: Boolean
    val photoCount: Int
    val showValidationError: Boolean

    data class BooleanItem(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: Boolean,
        override val requiresPhoto: Boolean,
        override val canAddPhoto: Boolean,
        override val photoCount: Int,
        override val showValidationError: Boolean,
        val value: Boolean?,
    ) : UiChecklistItem

    data class NumberItem(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: Boolean,
        override val requiresPhoto: Boolean,
        override val canAddPhoto: Boolean,
        override val photoCount: Int,
        override val showValidationError: Boolean,
        val value: String,
        val rangeHint: StringDesc?,
        val isOutOfRange: Boolean,
        val isInvalidNumber: Boolean,
    ) : UiChecklistItem

    data class TextItem(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: Boolean,
        override val requiresPhoto: Boolean,
        override val canAddPhoto: Boolean,
        override val photoCount: Int,
        override val showValidationError: Boolean,
        val value: String,
    ) : UiChecklistItem

    data class SelectItem(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: Boolean,
        override val requiresPhoto: Boolean,
        override val canAddPhoto: Boolean,
        override val photoCount: Int,
        override val showValidationError: Boolean,
        val value: String?,
        val options: ImmutableList<String>,
    ) : UiChecklistItem

    data class ConfirmItem(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: Boolean,
        override val requiresPhoto: Boolean,
        override val canAddPhoto: Boolean,
        override val photoCount: Int,
        override val showValidationError: Boolean,
        val value: Boolean,
    ) : UiChecklistItem
}
