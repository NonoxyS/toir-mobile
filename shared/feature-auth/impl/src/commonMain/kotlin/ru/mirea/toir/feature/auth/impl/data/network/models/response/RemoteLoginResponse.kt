package ru.mirea.toir.feature.auth.impl.data.network.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteDevice
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteUser

@Serializable
internal data class RemoteLoginResponse(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("user") val user: RemoteUser,
    @SerialName("device") val device: RemoteDevice,
)
