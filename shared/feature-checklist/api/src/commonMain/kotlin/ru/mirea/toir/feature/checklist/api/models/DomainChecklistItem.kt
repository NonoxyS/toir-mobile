package ru.mirea.toir.feature.checklist.api.models

private typealias KBoolean = Boolean

sealed interface DomainChecklistItem {
    val id: String
    val title: String
    val description: String?
    val isRequired: KBoolean
    val requiresPhoto: KBoolean
    val resultId: String?
    val photoCount: Int
    val isAnswered: KBoolean

    data class Boolean(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: KBoolean,
        override val requiresPhoto: KBoolean,
        override val resultId: String?,
        override val photoCount: Int,
        val value: KBoolean?,
    ) : DomainChecklistItem {
        override val isAnswered: KBoolean get() = value != null
    }

    data class Number(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: KBoolean,
        override val requiresPhoto: KBoolean,
        override val resultId: String?,
        override val photoCount: Int,
        val value: Double?,
        val min: Double?,
        val max: Double?,
    ) : DomainChecklistItem {
        override val isAnswered: KBoolean get() = value != null
        val isOutOfRange: KBoolean get() =
            value != null && ((min != null && value < min) || (max != null && value > max))
    }

    data class Text(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: KBoolean,
        override val requiresPhoto: KBoolean,
        override val resultId: String?,
        override val photoCount: Int,
        val value: String?,
    ) : DomainChecklistItem {
        override val isAnswered: KBoolean get() = !value.isNullOrBlank()
    }

    data class Select(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: KBoolean,
        override val requiresPhoto: KBoolean,
        override val resultId: String?,
        override val photoCount: Int,
        val value: String?,
        val options: List<String>,
    ) : DomainChecklistItem {
        override val isAnswered: KBoolean get() = !value.isNullOrBlank()
    }

    data class Confirm(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val isRequired: KBoolean,
        override val requiresPhoto: KBoolean,
        override val resultId: String?,
        override val photoCount: Int,
        val isConfirmed: KBoolean,
    ) : DomainChecklistItem {
        override val isAnswered: KBoolean get() = isConfirmed
    }
}
