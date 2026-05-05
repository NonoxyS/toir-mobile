package ru.mirea.toir.sync.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteSyncPushRequest(
    @SerialName("clientBatchId") val clientBatchId: String,
    @SerialName("deviceId") val deviceId: String,
    @SerialName("sentAt") val sentAt: String,
    @SerialName("inspections") val inspections: List<RemoteSyncInspection> = emptyList(),
    @SerialName(
        "inspectionEquipmentResults"
    ) val inspectionEquipmentResults: List<RemoteSyncEquipmentResult> = emptyList(),
    @SerialName("checklistItemResults") val checklistItemResults: List<RemoteSyncChecklistItemResult> = emptyList(),
    @SerialName("actionLogs") val actionLogs: List<RemoteSyncActionLog> = emptyList(),
)

@Serializable
internal data class RemoteSyncInspection(
    @SerialName("id") val id: String,
    @SerialName("routeAssignmentId") val routeAssignmentId: String?,
    @SerialName("routeId") val routeId: String,
    @SerialName("status") val status: String,
    @SerialName("startedAt") val startedAt: String?,
    @SerialName("completedAt") val completedAt: String?,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteSyncEquipmentResult(
    @SerialName("id") val id: String,
    @SerialName("inspectionId") val inspectionId: String,
    @SerialName("routePointId") val routePointId: String,
    @SerialName("equipmentId") val equipmentId: String,
    @SerialName("status") val status: String,
    @SerialName("startedAt") val startedAt: String?,
    @SerialName("completedAt") val completedAt: String?,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteSyncChecklistItemResult(
    @SerialName("id") val id: String,
    @SerialName("inspectionEquipmentResultId") val inspectionEquipmentResultId: String,
    @SerialName("checklistItemId") val checklistItemId: String,
    @SerialName("valueText") val valueText: String?,
    @SerialName("valueNumber") val valueNumber: Double?,
    @SerialName("valueBoolean") val valueBoolean: Boolean?,
    @SerialName("selectedOption") val selectedOption: String?,
    @SerialName("comment") val comment: String?,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteSyncActionLog(
    @SerialName("id") val id: String,
    @SerialName("actionType") val actionType: String,
    @SerialName("entityType") val entityType: String?,
    @SerialName("entityId") val entityId: String?,
    @SerialName("payloadJson") val payloadJson: String?,
    @SerialName("actionTime") val actionTime: String,
)
