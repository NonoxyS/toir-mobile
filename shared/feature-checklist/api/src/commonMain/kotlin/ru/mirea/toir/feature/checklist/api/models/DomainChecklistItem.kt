package ru.mirea.toir.feature.checklist.api.models

sealed interface DomainChecklistItem {
    val id: String
    val title: String
    val description: String?
    val isRequired: Boolean
    val requiresPhoto: Boolean
    val resultId: String?
    val photoCount: Int
    val isAnswered: Boolean

    data class BooleanItem(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: Boolean,
        override val requiresPhoto: Boolean,
        override val resultId: String?,
        override val photoCount: Int,
        val value: Boolean?,
    ) : DomainChecklistItem {
        override val isAnswered: Boolean get() = value != null
    }

    data class NumberItem(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: Boolean,
        override val requiresPhoto: Boolean,
        override val resultId: String?,
        override val photoCount: Int,
        val value: Double?,
        val min: Double?,
        val max: Double?,
    ) : DomainChecklistItem {
        override val isAnswered: Boolean get() = value != null
        val isOutOfRange: Boolean get() =
            value != null && ((min != null && value < min) || (max != null && value > max))
    }

    data class TextItem(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: Boolean,
        override val requiresPhoto: Boolean,
        override val resultId: String?,
        override val photoCount: Int,
        val value: String?,
    ) : DomainChecklistItem {
        override val isAnswered: Boolean get() = !value.isNullOrBlank()
    }

    data class SelectItem(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: Boolean,
        override val requiresPhoto: Boolean,
        override val resultId: String?,
        override val photoCount: Int,
        val value: String?,
        val options: List<String>,
    ) : DomainChecklistItem {
        override val isAnswered: Boolean get() = !value.isNullOrBlank()
    }

    data class ConfirmItem(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: Boolean,
        override val requiresPhoto: Boolean,
        override val resultId: String?,
        override val photoCount: Int,
        val isConfirmed: Boolean,
    ) : DomainChecklistItem {
        override val isAnswered: Boolean get() = isConfirmed
    }
}
