package ru.mirea.toir.sync.data.network.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.mirea.toir.core.network.deserializer.enumSerializerWithFallback
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedReason.EQUIPMENT_MISMATCH
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedReason.INSPECTION_NOT_FOUND
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedReason.INVALID_ASSIGNMENT_ID
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedReason.INVALID_ROUTE_ID
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedReason.ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedReason.ROUTE_ID_MISMATCH
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedReason.ROUTE_POINT_NOT_FOUND
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedReason.UNKNOWN

@Serializable(with = RemoteSyncRejectedReasonSerializer::class)
internal enum class RemoteSyncRejectedReason {
    INVALID_ASSIGNMENT_ID,
    INVALID_ROUTE_ID,
    ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN,
    ROUTE_ID_MISMATCH,
    INSPECTION_NOT_FOUND,
    ROUTE_POINT_NOT_FOUND,
    EQUIPMENT_MISMATCH,
    UNKNOWN,
}

internal object RemoteSyncRejectedReasonSerializer : KSerializer<RemoteSyncRejectedReason> {
    private val delegate = enumSerializerWithFallback(
        fallbackEnum = UNKNOWN,
        serialNameMapping = mapOf(
            INVALID_ASSIGNMENT_ID to "INVALID_ASSIGNMENT_ID",
            INVALID_ROUTE_ID to "INVALID_ROUTE_ID",
            ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN to "ROUTE_ASSIGNMENT_NOT_FOUND_OR_FORBIDDEN",
            ROUTE_ID_MISMATCH to "ROUTE_ID_MISMATCH",
            INSPECTION_NOT_FOUND to "INSPECTION_NOT_FOUND",
            ROUTE_POINT_NOT_FOUND to "ROUTE_POINT_NOT_FOUND",
            EQUIPMENT_MISMATCH to "EQUIPMENT_MISMATCH",
        )
    )
    override val descriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: RemoteSyncRejectedReason) = delegate.serialize(encoder, value)
    override fun deserialize(decoder: Decoder): RemoteSyncRejectedReason = delegate.deserialize(decoder)
}
