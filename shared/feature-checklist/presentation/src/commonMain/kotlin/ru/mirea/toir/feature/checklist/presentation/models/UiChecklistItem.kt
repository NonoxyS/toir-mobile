package ru.mirea.toir.feature.checklist.presentation.models

import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.collections.immutable.ImmutableList

private typealias KBoolean = Boolean

sealed interface UiChecklistItem {
    val id: String
    val title: String
    val description: String?
    val isRequired: KBoolean
    val requiresPhoto: KBoolean
    val photoCount: Int
    val showValidationError: KBoolean

    data class Boolean(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: KBoolean,
        override val requiresPhoto: KBoolean,
        override val photoCount: Int,
        override val showValidationError: KBoolean,
        val value: KBoolean?,
    ) : UiChecklistItem

    data class Number(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: KBoolean,
        override val requiresPhoto: KBoolean,
        override val photoCount: Int,
        override val showValidationError: KBoolean,
        val value: String,
        val rangeHint: StringDesc?,
        val isOutOfRange: KBoolean,
    ) : UiChecklistItem

    data class Text(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: KBoolean,
        override val requiresPhoto: KBoolean,
        override val photoCount: Int,
        override val showValidationError: KBoolean,
        val value: String,
    ) : UiChecklistItem

    data class Select(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: KBoolean,
        override val requiresPhoto: KBoolean,
        override val photoCount: Int,
        override val showValidationError: KBoolean,
        val value: String?,
        val options: ImmutableList<String>,
    ) : UiChecklistItem

    data class Confirm(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: KBoolean,
        override val requiresPhoto: KBoolean,
        override val photoCount: Int,
        override val showValidationError: KBoolean,
        val value: KBoolean,
    ) : UiChecklistItem
}
