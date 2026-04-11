package ru.mirea.toir.feature.auth.impl.data.network.models.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteLoginRequest(
    @SerialName("login") val login: String,
    @SerialName("password") val password: String,
    @SerialName("deviceCode") val deviceCode: String,
    @SerialName("platform") val platform: String?,
    @SerialName("appVersion") val appVersion: String?,
)