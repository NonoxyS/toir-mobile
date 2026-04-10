package ru.mirea.toir.feature.auth.impl.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteLoginResponse(
    @SerialName("accessToken") val accessToken: String?,
    @SerialName("refreshToken") val refreshToken: String?,
    @SerialName("user") val user: RemoteUser?,
    @SerialName("device") val device: RemoteDevice?,
)

@Serializable
internal data class RemoteUser(
    @SerialName("id") val id: String?,
    @SerialName("displayName") val displayName: String?,
    @SerialName("role") val role: String?,
)

@Serializable
internal data class RemoteDevice(
    @SerialName("id") val id: String?,
    @SerialName("deviceCode") val deviceCode: String?,
)

@Serializable
internal data class RemoteRefreshRequest(
    @SerialName("refreshToken") val refreshToken: String,
)

@Serializable
internal data class RemoteRefreshResponse(
    @SerialName("accessToken") val accessToken: String?,
    @SerialName("refreshToken") val refreshToken: String?,
)
