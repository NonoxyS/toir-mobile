package ru.mirea.toir.feature.auth.impl.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteUser(
    @SerialName("id") val id: String?,
    @SerialName("displayName") val displayName: String?,
    @SerialName("role") val role: String?,
)
