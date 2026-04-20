package ru.mirea.toir.sync.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteSyncPushResponse(
    @SerialName("clientBatchId") val clientBatchId: String,
    @SerialName("result") val result: RemoteSyncResult,
    @SerialName("accepted") val accepted: RemoteSyncAccepted,
    @SerialName("rejected") val rejected: List<RemoteSyncRejected>,
    @SerialName("serverTime") val serverTime: String,
)

@Serializable
internal data class RemoteSyncAccepted(
    @SerialName("inspections") val inspections: List<String>,
    @SerialName("inspectionEquipmentResults") val inspectionEquipmentResults: List<String>,
    @SerialName("checklistItemResults") val checklistItemResults: List<String>,
    @SerialName("actionLogs") val actionLogs: List<String>,
)

@Serializable
internal data class RemoteSyncRejected(
    @SerialName("entityType") val entityType: RemoteSyncRejectedEntityType,
    @SerialName("entityId") val entityId: String,
    @SerialName("reason") val reason: RemoteSyncRejectedReason,
)
