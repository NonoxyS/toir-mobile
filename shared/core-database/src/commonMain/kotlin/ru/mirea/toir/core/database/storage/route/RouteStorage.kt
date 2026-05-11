package ru.mirea.toir.core.database.storage.route

import kotlinx.coroutines.flow.Flow
import ru.mirea.toir.core.database.models.LocalRouteAssignmentStatus
import ru.mirea.toir.core.database.storage.route.models.LocalRoute
import ru.mirea.toir.core.database.storage.route.models.LocalRouteAssignment
import ru.mirea.toir.core.database.storage.route.models.LocalRoutePoint

interface RouteStorage {

    fun upsertRoute(id: String, code: String, name: String, description: String?)

    fun selectAllRoutes(): List<LocalRoute>

    fun selectRouteById(id: String): LocalRoute?

    fun deleteRouteById(id: String)

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

    fun deleteRoutePointById(id: String)

    @Suppress("LongParameterList")
    fun upsertAssignment(
        id: String,
        routeId: String,
        userId: String,
        status: LocalRouteAssignmentStatus,
        assignedAt: String,
        shiftCode: String?,
        updatedAt: String,
    )

    fun selectAllAssignments(): List<LocalRouteAssignment>

    fun selectAssignmentById(id: String): LocalRouteAssignment?

    fun deleteAssignmentById(id: String)

    fun deleteAllRoutes()

    fun deleteAllPoints()

    fun deleteAllAssignments()

    fun observeAllAssignments(): Flow<List<LocalRouteAssignment>>

    fun observeAssignmentById(id: String): Flow<LocalRouteAssignment?>

    fun observePointsByRouteId(routeId: String): Flow<List<LocalRoutePoint>>
}
