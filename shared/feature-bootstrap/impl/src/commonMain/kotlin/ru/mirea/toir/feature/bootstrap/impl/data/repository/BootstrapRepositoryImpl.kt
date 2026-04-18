package ru.mirea.toir.feature.bootstrap.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.models.LocalRouteStatus
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorage
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorage
import ru.mirea.toir.core.database.storage.route.RouteStorage
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorage
import ru.mirea.toir.core.database.storage.user.UserStorage
import ru.mirea.toir.feature.bootstrap.impl.data.network.BootstrapApiClient
import ru.mirea.toir.feature.bootstrap.impl.data.network.models.enums.RemoteAssignmentStatus
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository

internal class BootstrapRepositoryImpl(
    private val apiClient: BootstrapApiClient,
    private val userStorage: UserStorage,
    private val equipmentStorage: EquipmentStorage,
    private val routeStorage: RouteStorage,
    private val checklistStorage: ChecklistStorage,
    private val syncMetaStorage: SyncMetaStorage,
    private val coroutineDispatchers: CoroutineDispatchers,
) : BootstrapRepository {

    override suspend fun loadAndSaveBootstrap(): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val response = apiClient.fetchBootstrap().getOrThrow()

                    response.user?.let { remoteUser ->
                        userStorage.upsert(
                            id = remoteUser.id,
                            login = remoteUser.login,
                            displayName = remoteUser.displayName,
                            role = remoteUser.role.name.lowercase(),
                        )
                    }

                    response.equipment.forEach { eq ->
                        equipmentStorage.upsert(
                            id = eq.id,
                            code = eq.code,
                            name = eq.name,
                            type = eq.type,
                            locationId = eq.locationId,
                        )
                    }

                    response.routes.forEach { route ->
                        routeStorage.upsertRoute(
                            id = route.id,
                            name = route.name,
                            description = route.description,
                        )
                    }

                    response.routePoints.forEach { point ->
                        routeStorage.upsertRoutePoint(
                            id = point.id,
                            routeId = point.routeId,
                            equipmentId = point.equipmentId,
                            checklistId = point.checklistId,
                            orderIndex = point.orderIndex.toLong(),
                        )
                    }

                    response.assignments.forEach { assignment ->
                        routeStorage.upsertAssignment(
                            id = assignment.id,
                            routeId = assignment.routeId,
                            userId = assignment.userId,
                            status = assignment.status.toLocal(),
                            assignedAt = assignment.assignedAt,
                            dueDate = assignment.dueDate,
                        )
                    }

                    response.checklists.forEach { cl ->
                        checklistStorage.upsertChecklist(
                            id = cl.id,
                            name = cl.name,
                            equipmentId = cl.equipmentId,
                        )
                    }

                    response.checklistItems.forEach { item ->
                        checklistStorage.upsertItem(
                            id = item.id,
                            checklistId = item.checklistId,
                            title = item.title,
                            description = item.description,
                            answerType = item.answerType.name.lowercase(),
                            isRequired = if (item.isRequired) 1L else 0L,
                            requiresPhoto = if (item.requiresPhoto) 1L else 0L,
                            selectOptions = item.selectOptions?.let { Json.encodeToString(it) },
                            orderIndex = item.orderIndex.toLong(),
                        )
                    }

                    syncMetaStorage.upsert(
                        key = SyncMetaStorage.KEY_LAST_SYNC_TIME,
                        value = response.serverTime,
                    )

                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "loadAndSaveBootstrap failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    private fun RemoteAssignmentStatus.toLocal(): LocalRouteStatus = when (this) {
        RemoteAssignmentStatus.ASSIGNED -> LocalRouteStatus.ASSIGNED
        RemoteAssignmentStatus.IN_PROGRESS -> LocalRouteStatus.IN_PROGRESS
        RemoteAssignmentStatus.COMPLETED -> LocalRouteStatus.COMPLETED
        RemoteAssignmentStatus.UNKNOWN -> LocalRouteStatus.ASSIGNED
    }
}
