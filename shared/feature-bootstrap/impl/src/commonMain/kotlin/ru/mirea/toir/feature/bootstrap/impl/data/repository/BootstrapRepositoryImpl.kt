package ru.mirea.toir.feature.bootstrap.impl.data.repository

import io.github.aakira.napier.Napier
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.core.database.models.LocalRouteAssignmentStatus
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorage
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorage
import ru.mirea.toir.core.database.storage.location.LocationStorage
import ru.mirea.toir.core.database.storage.route.RouteStorage
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorage
import ru.mirea.toir.core.database.storage.user.UserStorage
import ru.mirea.toir.feature.bootstrap.impl.data.network.BootstrapApiClient
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.enums.RemoteAnswerType
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.enums.RemoteAssignmentStatus
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.enums.RemoteUserRole
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapResult

internal class BootstrapRepositoryImpl(
    private val apiClient: BootstrapApiClient,
    private val userStorage: UserStorage,
    private val equipmentStorage: EquipmentStorage,
    private val locationStorage: LocationStorage,
    private val routeStorage: RouteStorage,
    private val checklistStorage: ChecklistStorage,
    private val syncMetaStorage: SyncMetaStorage,
    private val coroutineDispatchers: CoroutineDispatchers,
) : BootstrapRepository {

    override suspend fun loadAndSaveBootstrap(): BootstrapResult =
        withContext(coroutineDispatchers.io) {
            try {
                val response = apiClient.fetchBootstrap().getOrThrow()

                response.user?.let { remoteUser ->
                    userStorage.upsert(
                        id = remoteUser.id,
                        login = remoteUser.login,
                        displayName = remoteUser.displayName,
                        role = remoteUser.role.toDbValue(),
                    )
                }

                response.locations.forEach { loc ->
                    locationStorage.upsert(
                        id = loc.id,
                        code = loc.code,
                        name = loc.name,
                        description = loc.description,
                        parentLocationId = loc.parentLocationId,
                    )
                }

                response.equipment.forEach { eq ->
                    equipmentStorage.upsert(
                        id = eq.id,
                        code = eq.code,
                        name = eq.name,
                        type = eq.type,
                        locationId = eq.locationId,
                        qrCode = eq.qrCode,
                    )
                }

                response.checklists.forEach { cl ->
                    checklistStorage.upsertChecklist(
                        id = cl.id,
                        code = cl.code,
                        name = cl.name,
                        equipmentType = cl.equipmentType,
                        description = cl.description,
                    )
                }

                response.checklistItems.forEach { item ->
                    checklistStorage.upsertItem(
                        id = item.id,
                        checklistId = item.checklistId,
                        title = item.title,
                        description = item.description,
                        answerType = item.responseType.toDbValue(),
                        isRequired = if (item.isRequired) 1L else 0L,
                        requiresPhoto = if (item.requirePhoto) 1L else 0L,
                        selectOptions = item.optionsJson,
                        numericMin = item.numericMin,
                        numericMax = item.numericMax,
                        orderIndex = item.orderIndex.toLong(),
                    )
                }

                response.routes.forEach { route ->
                    routeStorage.upsertRoute(
                        id = route.id,
                        code = route.code,
                        name = route.name,
                        description = route.description,
                    )
                }

                response.routePoints.forEach { point ->
                    val equipment = equipmentStorage.selectById(point.equipmentId)
                    val checklist = equipment?.let {
                        checklistStorage.selectChecklistByEquipmentType(it.type)
                    }
                    if (checklist == null) {
                        Napier.w(
                            "No checklist for equipmentId=${point.equipmentId}, skipping route point ${point.id}"
                        )
                        return@forEach
                    }
                    routeStorage.upsertRoutePoint(
                        id = point.id,
                        routeId = point.routeId,
                        equipmentId = point.equipmentId,
                        checklistId = checklist.id,
                        orderIndex = point.orderIndex.toLong(),
                    )
                }

                response.assignments.forEach { assignment ->
                    routeStorage.upsertAssignment(
                        id = assignment.id,
                        routeId = assignment.routeId,
                        userId = assignment.userId,
                        status = assignment.status.toLocal(),
                        assignedAt = assignment.assignmentDate,
                        shiftCode = assignment.shiftCode,
                        updatedAt = assignment.updatedAt,
                    )
                }

                syncMetaStorage.upsert(
                    key = SyncMetaStorage.KEY_LAST_SYNC_TIME,
                    value = response.serverTime,
                )

                BootstrapResult.Success
            } catch (cause: ClientRequestException) {
                if (cause.response.status == HttpStatusCode.Unauthorized) {
                    Napier.w("loadAndSaveBootstrap: 401 Unauthorized")
                    BootstrapResult.Unauthorized
                } else {
                    Napier.e(message = "loadAndSaveBootstrap failed", throwable = cause)
                    BootstrapResult.Failure(cause)
                }
            } catch (cause: CancellationException) {
                throw cause
            } catch (cause: Throwable) {
                Napier.e(message = "loadAndSaveBootstrap failed", throwable = cause)
                BootstrapResult.Failure(cause)
            }
        }

    private fun RemoteAssignmentStatus.toLocal(): LocalRouteAssignmentStatus = when (this) {
        RemoteAssignmentStatus.ASSIGNED -> LocalRouteAssignmentStatus.ASSIGNED
        RemoteAssignmentStatus.IN_PROGRESS -> LocalRouteAssignmentStatus.IN_PROGRESS
        RemoteAssignmentStatus.COMPLETED -> LocalRouteAssignmentStatus.COMPLETED
        RemoteAssignmentStatus.PARTIALLY_COMPLETED -> LocalRouteAssignmentStatus.PARTIALLY_COMPLETED
        RemoteAssignmentStatus.CANCELLED -> LocalRouteAssignmentStatus.CANCELLED
        RemoteAssignmentStatus.UNKNOWN -> {
            Napier.e(message = "Unknown RemoteAssignmentStatus received, defaulting to ASSIGNED")
            LocalRouteAssignmentStatus.ASSIGNED
        }
    }

    private fun RemoteUserRole.toDbValue(): String = when (this) {
        RemoteUserRole.EXECUTOR -> "executor"
        RemoteUserRole.ADMIN -> "admin"
        RemoteUserRole.UNKNOWN -> "unknown"
    }

    private fun RemoteAnswerType.toDbValue(): String = when (this) {
        RemoteAnswerType.BOOLEAN -> "boolean"
        RemoteAnswerType.NUMBER -> "number"
        RemoteAnswerType.TEXT -> "text"
        RemoteAnswerType.SELECT -> "select"
        RemoteAnswerType.CONFIRM -> "confirmation"
        RemoteAnswerType.UNKNOWN -> "unknown"
    }
}
