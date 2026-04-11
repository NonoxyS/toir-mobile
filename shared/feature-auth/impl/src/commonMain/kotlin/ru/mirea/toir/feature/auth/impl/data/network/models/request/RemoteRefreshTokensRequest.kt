package ru.mirea.toir.feature.auth.impl.data.network.models.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteRefreshTokensRequest(
    @SerialName("refreshToken") val refreshToken: String,
)
