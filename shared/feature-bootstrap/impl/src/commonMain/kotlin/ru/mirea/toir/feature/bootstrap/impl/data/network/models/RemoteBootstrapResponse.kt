package ru.mirea.toir.feature.bootstrap.impl.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteBootstrapResponse(
    @SerialName("user") val user: RemoteBootstrapUser?,
    @SerialName("device") val device: RemoteBootstrapDevice?,
    @SerialName("assignments") val assignments: List<RemoteBootstrapAssignment>?,
    @SerialName("routes") val routes: List<RemoteBootstrapRoute>?,
    @SerialName("routePoints") val routePoints: List<RemoteBootstrapRoutePoint>?,
    @SerialName("equipment") val equipment: List<RemoteBootstrapEquipment>?,
    @SerialName("locations") val locations: List<RemoteBootstrapLocation>?,
    @SerialName("checklists") val checklists: List<RemoteBootstrapChecklist>?,
    @SerialName("checklistItems") val checklistItems: List<RemoteBootstrapChecklistItem>?,
    @SerialName("serverTime") val serverTime: String?,
)

@Serializable
internal data class RemoteBootstrapUser(
    @SerialName("id") val id: String?,
    @SerialName("login") val login: String?,
    @SerialName("displayName") val displayName: String?,
    @SerialName("role") val role: String?,
)

@Serializable
internal data class RemoteBootstrapDevice(
    @SerialName("id") val id: String?,
    @SerialName("deviceCode") val deviceCode: String?,
)

@Serializable
internal data class RemoteBootstrapAssignment(
    @SerialName("id") val id: String?,
    @SerialName("routeId") val routeId: String?,
    @SerialName("userId") val userId: String?,
    @SerialName("status") val status: String?,
    @SerialName("assignedAt") val assignedAt: String?,
    @SerialName("dueDate") val dueDate: String?,
)

@Serializable
internal data class RemoteBootstrapRoute(
    @SerialName("id") val id: String?,
    @SerialName("name") val name: String?,
    @SerialName("description") val description: String?,
)

@Serializable
internal data class RemoteBootstrapRoutePoint(
    @SerialName("id") val id: String?,
    @SerialName("routeId") val routeId: String?,
    @SerialName("equipmentId") val equipmentId: String?,
    @SerialName("checklistId") val checklistId: String?,
    @SerialName("orderIndex") val orderIndex: Int?,
)

@Serializable
internal data class RemoteBootstrapEquipment(
    @SerialName("id") val id: String?,
    @SerialName("code") val code: String?,
    @SerialName("name") val name: String?,
    @SerialName("type") val type: String?,
    @SerialName("locationId") val locationId: String?,
)

@Serializable
internal data class RemoteBootstrapLocation(
    @SerialName("id") val id: String?,
    @SerialName("name") val name: String?,
    @SerialName("description") val description: String?,
)

@Serializable
internal data class RemoteBootstrapChecklist(
    @SerialName("id") val id: String?,
    @SerialName("name") val name: String?,
    @SerialName("equipmentId") val equipmentId: String?,
)

@Serializable
internal data class RemoteBootstrapChecklistItem(
    @SerialName("id") val id: String?,
    @SerialName("checklistId") val checklistId: String?,
    @SerialName("title") val title: String?,
    @SerialName("description") val description: String?,
    @SerialName("answerType") val answerType: String?,
    @SerialName("isRequired") val isRequired: Boolean?,
    @SerialName("requiresPhoto") val requiresPhoto: Boolean?,
    @SerialName("selectOptions") val selectOptions: List<String>?,
    @SerialName("orderIndex") val orderIndex: Int?,
)
