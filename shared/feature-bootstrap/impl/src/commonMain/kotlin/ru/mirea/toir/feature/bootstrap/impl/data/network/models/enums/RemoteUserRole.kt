package ru.mirea.toir.feature.bootstrap.impl.data.network.models.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.mirea.toir.core.network.deserializer.enumSerializerWithFallback

@Serializable(with = RemoteUserRoleSerializer::class)
internal enum class RemoteUserRole {
    EXECUTOR,
    ADMIN,
    UNKNOWN,
}

internal object RemoteUserRoleSerializer : KSerializer<RemoteUserRole> {
    private val delegate = enumSerializerWithFallback(
        fallbackEnum = RemoteUserRole.UNKNOWN,
        serialNameMapping = mapOf(
            RemoteUserRole.EXECUTOR to "executor",
            RemoteUserRole.ADMIN to "admin",
        )
    )
    override val descriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: RemoteUserRole) = delegate.serialize(encoder, value)
    override fun deserialize(decoder: Decoder): RemoteUserRole = delegate.deserialize(decoder)
}
