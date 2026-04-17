package ru.mirea.toir.feature.bootstrap.impl.data.network.models.enums

import kotlinx.serialization.Serializable
import ru.mirea.toir.core.network.deserializer.enumSerializerWithFallback

@Serializable(with = RemoteAssignmentStatusSerializer::class)
internal enum class RemoteAssignmentStatus {
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    UNKNOWN,
}

internal val RemoteAssignmentStatusSerializer = enumSerializerWithFallback(
    fallbackEnum = RemoteAssignmentStatus.UNKNOWN,
    serialNameMapping = mapOf(
        RemoteAssignmentStatus.ASSIGNED to "assigned",
        RemoteAssignmentStatus.IN_PROGRESS to "in_progress",
        RemoteAssignmentStatus.COMPLETED to "completed",
    )
)
