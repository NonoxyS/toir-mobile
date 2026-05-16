package ru.mirea.toir.feature.bootstrap.impl.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.enums.RemoteAnswerType
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.enums.RemoteAssignmentStatus
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.enums.RemoteUserRole

@Serializable
internal data class RemoteBootstrapResponse(
    @SerialName("user") val user: RemoteBootstrapUser?,
    @SerialName("device") val device: RemoteBootstrapDevice?,
    @SerialName("assignments") val assignments: List<RemoteBootstrapAssignment> = emptyList(),
    @SerialName("routes") val routes: List<RemoteBootstrapRoute> = emptyList(),
    @SerialName("routePoints") val routePoints: List<RemoteBootstrapRoutePoint> = emptyList(),
    @SerialName("equipment") val equipment: List<RemoteBootstrapEquipment> = emptyList(),
    @SerialName("locations") val locations: List<RemoteBootstrapLocation> = emptyList(),
    @SerialName("checklists") val checklists: List<RemoteBootstrapChecklist> = emptyList(),
    @SerialName("checklistItems") val checklistItems: List<RemoteBootstrapChecklistItem> = emptyList(),
    @SerialName("inspections") val inspections: List<RemoteBootstrapInspection> = emptyList(),
    @SerialName("inspectionEquipmentResults")
    val inspectionEquipmentResults: List<RemoteBootstrapEquipmentResult> = emptyList(),
    @SerialName("checklistItemResults")
    val checklistItemResults: List<RemoteBootstrapChecklistItemResult> = emptyList(),
    @SerialName("photos") val photos: List<RemoteBootstrapPhoto> = emptyList(),
    @SerialName("serverTime") val serverTime: String,
)

@Serializable
internal data class RemoteBootstrapUser(
    @SerialName("id") val id: String,
    @SerialName("login") val login: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("role") val role: RemoteUserRole,
)

@Serializable
internal data class RemoteBootstrapDevice(
    @SerialName("id") val id: String,
    @SerialName("deviceCode") val deviceCode: String,
)

@Serializable
internal data class RemoteBootstrapAssignment(
    @SerialName("id") val id: String,
    @SerialName("routeId") val routeId: String,
    @SerialName("userId") val userId: String,
    @SerialName("assignmentDate") val assignmentDate: String,
    @SerialName("shiftCode") val shiftCode: String?,
    @SerialName("status") val status: RemoteAssignmentStatus,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteBootstrapRoute(
    @SerialName("id") val id: String,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String?,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteBootstrapRoutePoint(
    @SerialName("id") val id: String,
    @SerialName("routeId") val routeId: String,
    @SerialName("equipmentId") val equipmentId: String,
    @SerialName("orderIndex") val orderIndex: Int,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteBootstrapEquipment(
    @SerialName("id") val id: String,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("locationId") val locationId: String?,
    @SerialName("qrCode") val qrCode: String?,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteBootstrapLocation(
    @SerialName("id") val id: String,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String?,
    @SerialName("parentLocationId") val parentLocationId: String?,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteBootstrapChecklist(
    @SerialName("id") val id: String,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("equipmentType") val equipmentType: String,
    @SerialName("description") val description: String?,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteBootstrapChecklistItem(
    @SerialName("id") val id: String,
    @SerialName("checklistId") val checklistId: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("responseType") val responseType: RemoteAnswerType,
    @SerialName("isRequired") val isRequired: Boolean,
    @SerialName("requirePhoto") val requirePhoto: Boolean,
    @SerialName("optionsJson") val optionsJson: String?,
    @SerialName("numericMin") val numericMin: Double?,
    @SerialName("numericMax") val numericMax: Double?,
    @SerialName("orderIndex") val orderIndex: Int,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteBootstrapInspection(
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
internal data class RemoteBootstrapEquipmentResult(
    @SerialName("id") val id: String,
    @SerialName("inspectionId") val inspectionId: String,
    @SerialName("equipmentId") val equipmentId: String,
    @SerialName("routePointId") val routePointId: String,
    @SerialName("status") val status: String,
    @SerialName("startedAt") val startedAt: String?,
    @SerialName("completedAt") val completedAt: String?,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteBootstrapChecklistItemResult(
    @SerialName("id") val id: String,
    @SerialName("inspectionEquipmentResultId") val inspectionEquipmentResultId: String,
    @SerialName("checklistItemId") val checklistItemId: String,
    @SerialName("valueText") val valueText: String? = null,
    @SerialName("valueNumber") val valueNumber: Double? = null,
    @SerialName("valueBoolean") val valueBoolean: Boolean? = null,
    @SerialName("selectedOption") val selectedOption: String? = null,
    @SerialName("comment") val comment: String? = null,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("updatedAt") val updatedAt: String,
)

/** Метаданные фото без байтов — файл докачивается через [SyncRepository.downloadMissingPhotos]. */
@Serializable
internal data class RemoteBootstrapPhoto(
    @SerialName("id") val id: String,
    @SerialName("checklistItemResultId") val checklistItemResultId: String,
    @SerialName("fileName") val fileName: String,
    @SerialName("mimeType") val mimeType: String,
    @SerialName("sizeBytes") val sizeBytes: Long,
    @SerialName("checksum") val checksum: String?,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("uploadedAt") val uploadedAt: String?,
)
