package ru.mirea.toir.sync.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteConfigChangesResponse(
    @SerialName("assignments") val assignments: List<RemoteConfigAssignment> = emptyList(),
    @SerialName("routes") val routes: List<RemoteConfigRoute> = emptyList(),
    @SerialName("routePoints") val routePoints: List<RemoteConfigRoutePoint> = emptyList(),
    @SerialName("equipment") val equipment: List<RemoteConfigEquipment> = emptyList(),
    @SerialName("locations") val locations: List<RemoteConfigLocation> = emptyList(),
    @SerialName("checklists") val checklists: List<RemoteConfigChecklist> = emptyList(),
    @SerialName("checklistItems") val checklistItems: List<RemoteConfigChecklistItem> = emptyList(),
    @SerialName("inspections") val inspections: List<RemoteConfigChangesInspection> = emptyList(),
    @SerialName("inspectionEquipmentResults")
    val inspectionEquipmentResults: List<RemoteConfigChangesEquipmentResult> = emptyList(),
    @SerialName("checklistItemResults")
    val checklistItemResults: List<RemoteConfigChangesChecklistItemResult> = emptyList(),
    @SerialName("photos") val photos: List<RemoteConfigChangesPhoto> = emptyList(),
    @SerialName("deletedIds") val deletedIds: RemoteDeletedIds = RemoteDeletedIds(),
    @SerialName("serverTime") val serverTime: String,
)

@Serializable
internal data class RemoteDeletedIds(
    @SerialName("assignments") val assignments: List<String> = emptyList(),
    @SerialName("routes") val routes: List<String> = emptyList(),
    @SerialName("routePoints") val routePoints: List<String> = emptyList(),
    @SerialName("equipment") val equipment: List<String> = emptyList(),
    @SerialName("locations") val locations: List<String> = emptyList(),
    @SerialName("checklists") val checklists: List<String> = emptyList(),
    @SerialName("checklistItems") val checklistItems: List<String> = emptyList(),
)

@Serializable
internal data class RemoteConfigAssignment(
    @SerialName("id") val id: String,
    @SerialName("routeId") val routeId: String,
    @SerialName("userId") val userId: String,
    @SerialName("assignmentDate") val assignmentDate: String,
    @SerialName("shiftCode") val shiftCode: String?,
    @SerialName("status") val status: String,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteConfigRoute(
    @SerialName("id") val id: String,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String?,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteConfigRoutePoint(
    @SerialName("id") val id: String,
    @SerialName("routeId") val routeId: String,
    @SerialName("equipmentId") val equipmentId: String,
    @SerialName("orderIndex") val orderIndex: Int,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteConfigEquipment(
    @SerialName("id") val id: String,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("locationId") val locationId: String?,
    @SerialName("qrCode") val qrCode: String?,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteConfigLocation(
    @SerialName("id") val id: String,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String?,
    @SerialName("parentLocationId") val parentLocationId: String?,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteConfigChecklist(
    @SerialName("id") val id: String,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("equipmentType") val equipmentType: String,
    @SerialName("description") val description: String?,
    @SerialName("updatedAt") val updatedAt: String,
)

@Serializable
internal data class RemoteConfigChecklistItem(
    @SerialName("id") val id: String,
    @SerialName("checklistId") val checklistId: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("responseType") val responseType: String,
    @SerialName("isRequired") val isRequired: Boolean,
    @SerialName("requirePhoto") val requirePhoto: Boolean,
    @SerialName("optionsJson") val optionsJson: String?,
    @SerialName("numericMin") val numericMin: Double?,
    @SerialName("numericMax") val numericMax: Double?,
    @SerialName("orderIndex") val orderIndex: Int,
    @SerialName("updatedAt") val updatedAt: String,
)

/**
 * Восстановление незавершённого обхода через delta (кнопка «Обновить»). Mirrors backend
 * `InspectionSyncDto` from `~/IdeaProjects/toir-backend/src/main/kotlin/ru/mirea/toir/api/dto/mobile/SyncPushRequest.kt`
 * — те же поля, что и в `RemoteBootstrapInspection` в feature-bootstrap. Применяется
 * правилом мёржа Waypoint 11 §1.3 (см. `InspectionStorage.applyServerInspection`).
 * Дублирование DTO с bootstrap-модулем намеренное: оба фичевых модуля держат свои
 * сетевые DTO, кросс-модульной зависимости избегаем.
 */
@Serializable
internal data class RemoteConfigChangesInspection(
    @SerialName("id") val id: String,
    @SerialName("routeAssignmentId") val routeAssignmentId: String?,
    @SerialName("routeId") val routeId: String,
    @SerialName("status") val status: String,
    @SerialName("startedAt") val startedAt: String?,
    @SerialName("completedAt") val completedAt: String?,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("updatedAt") val updatedAt: String,
)

/** Mirrors backend `InspectionEquipmentResultSyncDto`. */
@Serializable
internal data class RemoteConfigChangesEquipmentResult(
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

/** Mirrors backend `ChecklistItemResultSyncDto`. */
@Serializable
internal data class RemoteConfigChangesChecklistItemResult(
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

/**
 * Mirrors backend `PhotoSyncDto`. Метаданные без байтов файла — бинарь скачивается
 * отдельно фоном через `SyncRepository.downloadMissingPhotos` (Phase 5).
 */
@Serializable
internal data class RemoteConfigChangesPhoto(
    @SerialName("id") val id: String,
    @SerialName("checklistItemResultId") val checklistItemResultId: String,
    @SerialName("fileName") val fileName: String,
    @SerialName("mimeType") val mimeType: String,
    @SerialName("sizeBytes") val sizeBytes: Long,
    @SerialName("checksum") val checksum: String?,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("uploadedAt") val uploadedAt: String?,
)
