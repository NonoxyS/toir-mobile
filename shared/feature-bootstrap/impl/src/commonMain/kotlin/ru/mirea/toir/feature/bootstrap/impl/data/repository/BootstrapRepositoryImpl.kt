package ru.mirea.toir.feature.bootstrap.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.dao.ChecklistDao
import ru.mirea.toir.core.database.dao.EquipmentDao
import ru.mirea.toir.core.database.dao.RouteDao
import ru.mirea.toir.core.database.dao.SyncMetaDao
import ru.mirea.toir.core.database.dao.UserDao
import ru.mirea.toir.feature.bootstrap.impl.data.network.BootstrapApiClient
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository

internal class BootstrapRepositoryImpl(
    private val apiClient: BootstrapApiClient,
    private val userDao: UserDao,
    private val equipmentDao: EquipmentDao,
    private val routeDao: RouteDao,
    private val checklistDao: ChecklistDao,
    private val syncMetaDao: SyncMetaDao,
    private val coroutineDispatchers: CoroutineDispatchers,
) : BootstrapRepository {

    override suspend fun loadAndSaveBootstrap(): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val response = apiClient.fetchBootstrap().getOrThrow()

                    response.user?.let { remoteUser ->
                        userDao.upsert(
                            id = remoteUser.id.orEmpty(),
                            login = remoteUser.login.orEmpty(),
                            displayName = remoteUser.displayName.orEmpty(),
                            role = remoteUser.role.orEmpty(),
                        )
                    }

                    response.equipment.orEmpty().forEach { eq ->
                        equipmentDao.upsert(
                            id = eq.id.orEmpty(),
                            code = eq.code.orEmpty(),
                            name = eq.name.orEmpty(),
                            type = eq.type.orEmpty(),
                            locationId = eq.locationId.orEmpty(),
                        )
                    }

                    response.routes.orEmpty().forEach { route ->
                        routeDao.upsertRoute(
                            id = route.id.orEmpty(),
                            name = route.name.orEmpty(),
                            description = route.description,
                        )
                    }

                    response.routePoints.orEmpty().forEach { point ->
                        routeDao.upsertRoutePoint(
                            id = point.id.orEmpty(),
                            routeId = point.routeId.orEmpty(),
                            equipmentId = point.equipmentId.orEmpty(),
                            checklistId = point.checklistId.orEmpty(),
                            orderIndex = point.orderIndex?.toLong() ?: 0L,
                        )
                    }

                    response.assignments.orEmpty().forEach { assignment ->
                        routeDao.upsertAssignment(
                            id = assignment.id.orEmpty(),
                            routeId = assignment.routeId.orEmpty(),
                            userId = assignment.userId.orEmpty(),
                            status = assignment.status.orEmpty(),
                            assignedAt = assignment.assignedAt.orEmpty(),
                            dueDate = assignment.dueDate,
                        )
                    }

                    response.checklists.orEmpty().forEach { cl ->
                        checklistDao.upsertChecklist(
                            id = cl.id.orEmpty(),
                            name = cl.name.orEmpty(),
                            equipmentId = cl.equipmentId,
                        )
                    }

                    response.checklistItems.orEmpty().forEach { item ->
                        checklistDao.upsertItem(
                            id = item.id.orEmpty(),
                            checklistId = item.checklistId.orEmpty(),
                            title = item.title.orEmpty(),
                            description = item.description,
                            answerType = item.answerType.orEmpty(),
                            isRequired = if (item.isRequired == true) 1L else 0L,
                            requiresPhoto = if (item.requiresPhoto == true) 1L else 0L,
                            selectOptions = item.selectOptions?.let { Json.encodeToString(it) },
                            orderIndex = item.orderIndex?.toLong() ?: 0L,
                        )
                    }

                    syncMetaDao.upsert(
                        key = SyncMetaDao.KEY_LAST_SYNC_TIME,
                        value = response.serverTime.orEmpty(),
                    )

                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "loadAndSaveBootstrap failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
