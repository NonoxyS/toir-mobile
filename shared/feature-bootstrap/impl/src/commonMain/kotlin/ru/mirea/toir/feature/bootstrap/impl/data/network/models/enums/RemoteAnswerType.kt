package ru.mirea.toir.feature.bootstrap.impl.data.network.models.enums

import kotlinx.serialization.Serializable
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

internal val RemoteAnswerTypeSerializer = enumSerializerWithFallback(
    fallbackEnum = RemoteAnswerType.UNKNOWN,
    serialNameMapping = mapOf(
        RemoteAnswerType.BOOLEAN to "boolean",
        RemoteAnswerType.NUMBER to "number",
        RemoteAnswerType.TEXT to "text",
        RemoteAnswerType.SELECT to "select",
        RemoteAnswerType.CONFIRM to "confirm",
    )
)
