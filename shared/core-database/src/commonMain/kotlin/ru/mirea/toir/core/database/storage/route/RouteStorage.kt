package ru.mirea.toir.core.database.storage.route

import ru.mirea.toir.core.database.models.LocalRouteStatus
import ru.mirea.toir.core.database.storage.route.models.LocalRoute
import ru.mirea.toir.core.database.storage.route.models.LocalRouteAssignment
import ru.mirea.toir.core.database.storage.route.models.LocalRoutePoint

interface RouteStorage {

    fun upsertRoute(id: String, name: String, description: String?)

    fun selectAllRoutes(): List<LocalRoute>

    fun selectRouteById(id: String): LocalRoute?

    @Suppress("LongParameterList")
    fun upsertRoutePoint(
        id: String,
        routeId: String,
        equipmentId: String,
        checklistId: String,
        orderIndex: Long,
    )

    fun selectPointsByRouteId(routeId: String): List<LocalRoutePoint>

    fun selectPointById(id: String): LocalRoutePoint?

    @Suppress("LongParameterList")
    fun upsertAssignment(
        id: String,
        routeId: String,
        userId: String,
        status: LocalRouteStatus,
        assignedAt: String,
        dueDate: String?,
    )

    fun selectAllAssignments(): List<LocalRouteAssignment>

    fun selectAssignmentById(id: String): LocalRouteAssignment?

    fun updateAssignmentStatus(id: String, status: LocalRouteStatus)

    fun deleteAllRoutes()

    fun deleteAllPoints()

    fun deleteAllAssignments()
}
