package ru.mirea.toir.sync.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteSyncPushRequest(
    @SerialName("clientBatchId") val clientBatchId: String,
    @SerialName("deviceId") val deviceId: String,
    @SerialName("sentAt") val sentAt: String,
    @SerialName("inspections") val inspections: List<RemoteSyncInspection>?,
    @SerialName("inspectionEquipmentResults") val inspectionEquipmentResults: List<RemoteSyncEquipmentResult>?,
    @SerialName("checklistItemResults") val checklistItemResults: List<RemoteSyncChecklistItemResult>?,
    @SerialName("actionLogs") val actionLogs: List<RemoteSyncActionLog>?,
)

@Serializable
internal data class RemoteSyncInspection(
    @SerialName("id") val id: String,
    @SerialName("assignmentId") val assignmentId: String,
    @SerialName("routeId") val routeId: String,
    @SerialName("status") val status: String,
    @SerialName("startedAt") val startedAt: String,
    @SerialName("completedAt") val completedAt: String?,
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
)

@Serializable
internal data class RemoteSyncChecklistItemResult(
    @SerialName("id") val id: String,
    @SerialName("inspectionEquipmentResultId") val inspectionEquipmentResultId: String,
    @SerialName("checklistItemId") val checklistItemId: String,
    @SerialName("valueBoolean") val valueBoolean: Boolean?,
    @SerialName("valueNumber") val valueNumber: Double?,
    @SerialName("valueText") val valueText: String?,
    @SerialName("valueSelect") val valueSelect: String?,
    @SerialName("isConfirmed") val isConfirmed: Boolean,
    @SerialName("answeredAt") val answeredAt: String?,
)

@Serializable
internal data class RemoteSyncActionLog(
    @SerialName("id") val id: String,
    @SerialName("inspectionId") val inspectionId: String,
    @SerialName("actionType") val actionType: String,
    @SerialName("metadata") val metadata: String?,
    @SerialName("createdAt") val createdAt: String,
)
