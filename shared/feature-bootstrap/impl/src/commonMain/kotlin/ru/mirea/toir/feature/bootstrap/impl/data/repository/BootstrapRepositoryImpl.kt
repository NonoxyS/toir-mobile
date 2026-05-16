package ru.mirea.toir.feature.bootstrap.impl.data.repository

import io.github.aakira.napier.Napier
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.withContext
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.core.database.TransactionRunner
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalRouteAssignmentStatus
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorage
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorage
import ru.mirea.toir.core.database.storage.inspection.InspectionStorage
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.location.LocationStorage
import ru.mirea.toir.core.database.storage.photo.PhotoStorage
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
    private val inspectionStorage: InspectionStorage,
    private val photoStorage: PhotoStorage,
    private val transactionRunner: TransactionRunner,
    private val coroutineDispatchers: CoroutineDispatchers,
) : BootstrapRepository {

    override suspend fun loadAndSaveBootstrap(): BootstrapResult =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val response = apiClient.fetchBootstrap().getOrThrow()

                    // Одна транзакция на конфиг + restore: параллельный push из SyncManager
                    // не должен сменить sync_status между чтением и записью.
                    transactionRunner.transactional {
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
                                    "No checklist for equipmentId=${point.equipmentId}, " +
                                        "skipping route point ${point.id}"
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

                        response.inspections.forEach { inspection ->
                            inspectionStorage.applyServerInspection(
                                id = inspection.id,
                                assignmentId = inspection.routeAssignmentId,
                                routeId = inspection.routeId,
                                status = inspection.status.toLocalInspectionStatus(),
                                startedAt = inspection.startedAt,
                                completedAt = inspection.completedAt,
                                createdAt = inspection.createdAt,
                                updatedAt = inspection.updatedAt,
                            )
                        }

                        response.inspectionEquipmentResults.forEach { ier ->
                            inspectionStorage.applyServerEquipmentResult(
                                id = ier.id,
                                inspectionId = ier.inspectionId,
                                equipmentId = ier.equipmentId,
                                routePointId = ier.routePointId,
                                status = ier.status.toLocalEquipmentResultStatus(),
                                startedAt = ier.startedAt,
                                completedAt = ier.completedAt,
                                createdAt = ier.createdAt,
                                updatedAt = ier.updatedAt,
                            )
                        }

                        response.checklistItemResults.forEach { cir ->
                            inspectionStorage.applyServerChecklistItemResult(
                                id = cir.id,
                                inspectionEquipmentResultId = cir.inspectionEquipmentResultId,
                                checklistItemId = cir.checklistItemId,
                                valueText = cir.valueText,
                                valueNumber = cir.valueNumber,
                                valueBoolean = cir.valueBoolean?.let { if (it) 1L else 0L },
                                selectedOption = cir.selectedOption,
                                comment = cir.comment,
                                createdAt = cir.createdAt,
                                updatedAt = cir.updatedAt,
                            )
                        }

                        response.photos.forEach { photo ->
                            photoStorage.insertRestoredPhoto(
                                id = photo.id,
                                checklistItemResultId = photo.checklistItemResultId,
                                takenAt = photo.createdAt,
                                fileName = photo.fileName,
                                mimeType = photo.mimeType,
                                sizeBytes = photo.sizeBytes,
                                checksum = photo.checksum,
                            )
                        }
                    }

                    BootstrapResult.Success
                },
                catchBlock = { cause ->
                    if (cause is ClientRequestException &&
                        cause.response.status == HttpStatusCode.Unauthorized
                    ) {
                        Napier.w("loadAndSaveBootstrap: 401 Unauthorized")
                        BootstrapResult.Unauthorized
                    } else {
                        Napier.e(message = "loadAndSaveBootstrap failed", throwable = cause)
                        BootstrapResult.Failure(cause)
                    }
                },
            )
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

    /**
     * Backend `InspectionSyncDto.status` — строка (см. SyncPushRequest.kt в toir-backend).
     * Имена совпадают с `LocalInspectionStatus.localValue`. Неизвестная строка → PLANNED
     * с warning-логом: безопаснее «вернуть как запланирован», чем уронить bootstrap.
     */
    private fun String.toLocalInspectionStatus(): LocalInspectionStatus =
        LocalInspectionStatus.entries.firstOrNull { it.localValue == this }
            ?: run {
                Napier.w("Unknown InspectionStatus '$this' from server, defaulting to PLANNED")
                LocalInspectionStatus.PLANNED
            }

    /** Same convention as [toLocalInspectionStatus]. */
    private fun String.toLocalEquipmentResultStatus(): LocalEquipmentResultStatus =
        LocalEquipmentResultStatus.entries.firstOrNull { it.localValue == this }
            ?: run {
                Napier.w(
                    "Unknown EquipmentResultStatus '$this' from server, defaulting to NOT_STARTED"
                )
                LocalEquipmentResultStatus.NOT_STARTED
            }
}
