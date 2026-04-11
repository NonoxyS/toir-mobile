package ru.mirea.toir.feature.auth.impl.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteDevice(
    @SerialName("id") val id: String?,
    @SerialName("deviceCode") val deviceCode: String?,
)
