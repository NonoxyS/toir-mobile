package ru.mirea.toir.feature.auth.impl.data.network.models

import kotlinx.serialization.Serializable
import ru.mirea.toir.core.network.deserializer.enumSerializerWithFallback

@Serializable(with = RemoteUserRoleSerializer::class)
internal enum class RemoteUserRole {
    EXECUTOR,
    ADMIN,
    UNKNOWN,
}

internal val RemoteUserRoleSerializer = enumSerializerWithFallback(
    fallbackEnum = RemoteUserRole.UNKNOWN,
    serialNameMapping = mapOf(
        RemoteUserRole.EXECUTOR to "executor",
        RemoteUserRole.ADMIN to "admin",
    )
)
