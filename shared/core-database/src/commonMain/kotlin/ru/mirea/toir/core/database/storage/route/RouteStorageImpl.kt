package ru.mirea.toir.core.database.storage.route

import ru.mirea.toir.core.database.Route_assignments
import ru.mirea.toir.core.database.Route_points
import ru.mirea.toir.core.database.Routes
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.models.LocalRouteStatus
import ru.mirea.toir.core.database.storage.route.models.LocalRoute
import ru.mirea.toir.core.database.storage.route.models.LocalRouteAssignment
import ru.mirea.toir.core.database.storage.route.models.LocalRoutePoint

internal class RouteStorageImpl(db: ToirDatabase) : RouteStorage {

    private val routeQueries = db.routeQueries
    private val pointQueries = db.routePointQueries
    private val assignmentQueries = db.routeAssignmentQueries

    override fun upsertRoute(id: String, name: String, description: String?) {
        routeQueries.upsertRoute(id = id, name = name, description = description)
    }

    override fun selectAllRoutes(): List<LocalRoute> =
        routeQueries.selectAll().executeAsList().map { it.toLocal() }

    override fun selectRouteById(id: String): LocalRoute? =
        routeQueries.selectById(id).executeAsOneOrNull()?.toLocal()

    override fun upsertRoutePoint(
        id: String,
        routeId: String,
        equipmentId: String,
        checklistId: String,
        orderIndex: Long,
    ) {
        pointQueries.upsertRoutePoint(
            id = id,
            route_id = routeId,
            equipment_id = equipmentId,
            checklist_id = checklistId,
            order_index = orderIndex,
        )
    }

    override fun selectPointsByRouteId(routeId: String): List<LocalRoutePoint> =
        pointQueries.selectByRouteId(routeId).executeAsList().map { it.toLocal() }

    override fun selectPointById(id: String): LocalRoutePoint? =
        pointQueries.selectById(id).executeAsOneOrNull()?.toLocal()

    override fun upsertAssignment(
        id: String,
        routeId: String,
        userId: String,
        status: LocalRouteStatus,
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

    override fun selectAllAssignments(): List<LocalRouteAssignment> =
        assignmentQueries.selectAll().executeAsList().map { it.toLocal() }

    override fun selectAssignmentById(id: String): LocalRouteAssignment? =
        assignmentQueries.selectById(id).executeAsOneOrNull()?.toLocal()

    override fun updateAssignmentStatus(id: String, status: LocalRouteStatus) {
        assignmentQueries.updateStatus(status = status, id = id)
    }

    override fun deleteAllRoutes() = routeQueries.deleteAll()

    override fun deleteAllPoints() = pointQueries.deleteAll()

    override fun deleteAllAssignments() = assignmentQueries.deleteAll()

    private fun Routes.toLocal() = LocalRoute(
        id = id,
        name = name,
        description = description,
    )

    private fun Route_points.toLocal() = LocalRoutePoint(
        id = id,
        routeId = route_id,
        equipmentId = equipment_id,
        checklistId = checklist_id,
        orderIndex = order_index,
    )

    private fun Route_assignments.toLocal() = LocalRouteAssignment(
        id = id,
        routeId = route_id,
        userId = user_id,
        status = status,
        assignedAt = assigned_at,
        dueDate = due_date,
    )
}
