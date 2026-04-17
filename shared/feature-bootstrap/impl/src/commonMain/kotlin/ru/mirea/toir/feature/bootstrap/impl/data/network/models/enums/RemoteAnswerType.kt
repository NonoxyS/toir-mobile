package ru.mirea.toir.feature.bootstrap.impl.data.network.models.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.mirea.toir.core.network.deserializer.enumSerializerWithFallback

@Serializable(with = RemoteAnswerTypeSerializer::class)
internal enum class RemoteAnswerType {
    BOOLEAN,
    NUMBER,
    TEXT,
    SELECT,
    CONFIRM,
    UNKNOWN,
}

internal object RemoteAnswerTypeSerializer : KSerializer<RemoteAnswerType> {
    private val delegate = enumSerializerWithFallback(
        fallbackEnum = RemoteAnswerType.UNKNOWN,
        serialNameMapping = mapOf(
            RemoteAnswerType.BOOLEAN to "boolean",
            RemoteAnswerType.NUMBER to "number",
            RemoteAnswerType.TEXT to "text",
            RemoteAnswerType.SELECT to "select",
            RemoteAnswerType.CONFIRM to "confirm",
        )
    )
    override val descriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: RemoteAnswerType) = delegate.serialize(encoder, value)
    override fun deserialize(decoder: Decoder): RemoteAnswerType = delegate.deserialize(decoder)
}
