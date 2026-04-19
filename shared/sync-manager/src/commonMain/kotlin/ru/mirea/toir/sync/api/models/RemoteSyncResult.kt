package ru.mirea.toir.sync.api.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.mirea.toir.core.network.deserializer.enumSerializerWithFallback

@Serializable(with = RemoteSyncResultSerializer::class)
internal enum class RemoteSyncResult {
    ACCEPTED,
    REJECTED,
    UNKNOWN,
}

internal object RemoteSyncResultSerializer : KSerializer<RemoteSyncResult> {
    private val delegate = enumSerializerWithFallback(
        fallbackEnum = RemoteSyncResult.UNKNOWN,
        serialNameMapping = mapOf(
            RemoteSyncResult.ACCEPTED to "accepted",
            RemoteSyncResult.REJECTED to "rejected",
        )
    )
    override val descriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: RemoteSyncResult) = delegate.serialize(encoder, value)
    override fun deserialize(decoder: Decoder): RemoteSyncResult = delegate.deserialize(decoder)
}
