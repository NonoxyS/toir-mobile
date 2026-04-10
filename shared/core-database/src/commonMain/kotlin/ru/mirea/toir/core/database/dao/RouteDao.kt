package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.Route_assignments
import ru.mirea.toir.core.database.Route_points
import ru.mirea.toir.core.database.Routes
import ru.mirea.toir.core.database.ToirDatabase

internal class RouteDao(db: ToirDatabase) {
    private val routeQueries = db.routeQueries
    private val pointQueries = db.routePointQueries
    private val assignmentQueries = db.routeAssignmentQueries

    fun upsertRoute(id: String, name: String, description: String?) {
        routeQueries.upsertRoute(id = id, name = name, description = description)
    }

    fun selectAllRoutes(): List<Routes> = routeQueries.selectAll().executeAsList()

    fun selectRouteById(id: String): Routes? = routeQueries.selectById(id).executeAsOneOrNull()

    fun upsertRoutePoint(id: String, routeId: String, equipmentId: String, checklistId: String, orderIndex: Long) {
        pointQueries.upsertRoutePoint(
            id = id,
            route_id = routeId,
            equipment_id = equipmentId,
            checklist_id = checklistId,
            order_index = orderIndex,
        )
    }

    fun selectPointsByRouteId(routeId: String): List<Route_points> =
        pointQueries.selectByRouteId(routeId).executeAsList()

    fun selectPointById(id: String): Route_points? = pointQueries.selectById(id).executeAsOneOrNull()

    @Suppress("LongParameterList")
    fun upsertAssignment(
        id: String,
        routeId: String,
        userId: String,
        status: String,
        assignedAt: String,
        dueDate: String?,
    ) {
        assignmentQueries.upsertRouteAssignment(
            id = id,
            route_id = routeId,
            user_id = userId,
            status = status,
            assigned_at = assignedAt,
            due_date = dueDate,
        )
    }

    fun selectAllAssignments(): List<Route_assignments> = assignmentQueries.selectAll().executeAsList()

    fun selectAssignmentById(id: String): Route_assignments? = assignmentQueries.selectById(id).executeAsOneOrNull()

    fun updateAssignmentStatus(id: String, status: String) {
        assignmentQueries.updateStatus(status = status, id = id)
    }

    fun deleteAllRoutes() = routeQueries.deleteAll()

    fun deleteAllPoints() = pointQueries.deleteAll()

    fun deleteAllAssignments() = assignmentQueries.deleteAll()
}
