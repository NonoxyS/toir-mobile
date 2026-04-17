package ru.mirea.toir.feature.bootstrap.impl.data.network.models.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.mirea.toir.core.network.deserializer.enumSerializerWithFallback

@Serializable(with = RemoteAssignmentStatusSerializer::class)
internal enum class RemoteAssignmentStatus {
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    UNKNOWN,
}

internal object RemoteAssignmentStatusSerializer : KSerializer<RemoteAssignmentStatus> {
    private val delegate = enumSerializerWithFallback(
        fallbackEnum = RemoteAssignmentStatus.UNKNOWN,
        serialNameMapping = mapOf(
            RemoteAssignmentStatus.ASSIGNED to "assigned",
            RemoteAssignmentStatus.IN_PROGRESS to "in_progress",
            RemoteAssignmentStatus.COMPLETED to "completed",
        )
    )
    override val descriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: RemoteAssignmentStatus) = delegate.serialize(encoder, value)
    override fun deserialize(decoder: Decoder): RemoteAssignmentStatus = delegate.deserialize(decoder)
}
