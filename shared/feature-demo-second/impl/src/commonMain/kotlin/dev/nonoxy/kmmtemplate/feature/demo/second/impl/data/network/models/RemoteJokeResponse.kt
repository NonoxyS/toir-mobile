package dev.nonoxy.kmmtemplate.feature.demo.second.impl.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteJokeResponse(
    @SerialName("error") val error: Boolean,
    @SerialName("joke") val joke: String,
)
