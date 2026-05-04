package ru.mirea.toir.sync.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemotePhotoUploadResponse(
    @SerialName("photoId") val photoId: String,
    @SerialName("uploadedAt") val uploadedAt: String,
    @SerialName("storageKey") val storageKey: String,
)
