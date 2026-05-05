package ru.mirea.toir.sync.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.auth.data.storage.TokenStorage
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.core.database.storage.action_log.ActionLogStorage
import ru.mirea.toir.core.database.storage.inspection.InspectionStorage
import ru.mirea.toir.core.database.storage.photo.PhotoStorage
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorage
import ru.mirea.toir.sync.data.applier.ConfigChangesApplier
import ru.mirea.toir.sync.data.network.SyncApiClient
import ru.mirea.toir.sync.data.network.models.RemoteSyncActionLog
import ru.mirea.toir.sync.data.network.models.RemoteSyncChecklistItemResult
import ru.mirea.toir.sync.data.network.models.RemoteSyncEquipmentResult
import ru.mirea.toir.sync.data.network.models.RemoteSyncInspection
import ru.mirea.toir.sync.data.network.models.RemoteSyncPushRequest
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejected
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedEntityType
import ru.mirea.toir.sync.data.readFileBytes
import ru.mirea.toir.sync.domain.models.SyncResult
import ru.mirea.toir.sync.domain.repository.SyncRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class SyncRepositoryImpl(
    private val syncApiClient: SyncApiClient,
    private val inspectionStorage: InspectionStorage,
    private val photoStorage: PhotoStorage,
    private val actionLogStorage: ActionLogStorage,
    private val syncMetaStorage: SyncMetaStorage,
    private val tokenStorage: TokenStorage,
    private val configChangesApplier: ConfigChangesApplier,
    private val coroutineDispatchers: CoroutineDispatchers,
) : SyncRepository {

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun pushPendingData(): Result<SyncResult> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val deviceId = tokenStorage.getOrCreateDeviceCode()
                    val pendingInspections = inspectionStorage.selectPendingInspections()
                    val pendingEquipmentResults = inspectionStorage.selectPendingEquipmentResults()
                    val pendingChecklistResults = inspectionStorage.selectPendingChecklistItemResults()
                    val pendingLogs = actionLogStorage.selectPending()

                    if (pendingInspections.isEmpty() && pendingEquipmentResults.isEmpty() &&
                        pendingChecklistResults.isEmpty() && pendingLogs.isEmpty()
                    ) {
                        return@coRunCatching SyncResult(
                            acceptedCount = 0,
                            rejectedCount = 0,
                        ).wrapResultSuccess()
                    }

                    val request = RemoteSyncPushRequest(
                        clientBatchId = Uuid.random().toString(),
                        deviceId = deviceId,
                        sentAt = Clock.System.now().toString(),
                        inspections = pendingInspections.map { inspection ->
                            RemoteSyncInspection(
                                id = inspection.id,
                                routeAssignmentId = inspection.assignmentId,
                                routeId = inspection.routeId,
                                status = inspection.status.localValue,
                                startedAt = inspection.startedAt,
                                completedAt = inspection.completedAt,
                                createdAt = inspection.createdAt,
                                updatedAt = inspection.updatedAt,
                            )
                        },
                        inspectionEquipmentResults = pendingEquipmentResults.map { result ->
                            RemoteSyncEquipmentResult(
                                id = result.id,
                                inspectionId = result.inspectionId,
                                routePointId = result.routePointId,
                                equipmentId = result.equipmentId,
                                status = result.status.localValue,
                                startedAt = result.startedAt,
                                completedAt = result.completedAt,
                                createdAt = result.createdAt,
                                updatedAt = result.updatedAt,
                            )
                        },
                        checklistItemResults = pendingChecklistResults.map { r ->
                            RemoteSyncChecklistItemResult(
                                id = r.id,
                                inspectionEquipmentResultId = r.equipmentResultId,
                                checklistItemId = r.checklistItemId,
                                valueText = r.valueText,
                                valueNumber = r.valueNumber,
                                valueBoolean = r.valueBoolean?.let { it == 1L },
                                selectedOption = r.selectedOption,
                                comment = r.comment,
                                createdAt = r.createdAt,
                                updatedAt = r.updatedAt,
                            )
                        },
                        actionLogs = pendingLogs.map { log ->
                            RemoteSyncActionLog(
                                id = log.id,
                                actionType = log.actionType,
                                entityType = log.entityType,
                                entityId = log.entityId,
                                payloadJson = log.payloadJson,
                                actionTime = log.actionTime,
                            )
                        },
                    )

                    val response = syncApiClient.pushSync(request).getOrThrow()

                    response.accepted.inspections.forEach {
                        inspectionStorage.updateInspectionSyncStatus(it, LocalSyncStatus.SYNCED)
                    }
                    response.accepted.inspectionEquipmentResults.forEach {
                        inspectionStorage.updateEquipmentResultSyncStatus(it, LocalSyncStatus.SYNCED)
                    }
                    response.accepted.checklistItemResults.forEach {
                        inspectionStorage.updateChecklistItemResultSyncStatus(it, LocalSyncStatus.SYNCED)
                    }
                    response.accepted.actionLogs.forEach {
                        actionLogStorage.updateSyncStatus(it, LocalSyncStatus.SYNCED)
                    }

                    response.rejected.forEach { rejected ->
                        markRejectedAsFailed(rejected)
                    }

                    val totalAccepted = response.accepted.inspections.size +
                        response.accepted.inspectionEquipmentResults.size +
                        response.accepted.checklistItemResults.size +
                        response.accepted.actionLogs.size

                    SyncResult(
                        acceptedCount = totalAccepted,
                        rejectedCount = response.rejected.size,
                    ).wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "pushPendingData failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override suspend fun uploadPendingPhotos(): Result<Int> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val pending = photoStorage.selectPending()
                    var uploaded = 0
                    pending.forEach { photo ->
                        val bytes = readFileBytes(photo.fileUri)
                        syncApiClient.uploadPhoto(
                            photoId = photo.id,
                            checklistItemResultId = photo.checklistItemResultId,
                            fileBytes = bytes,
                        ).onSuccess { response ->
                            photoStorage.updateSyncStatus(
                                id = photo.id,
                                syncStatus = LocalSyncStatus.SYNCED,
                                storageKey = response.storageKey,
                            )
                            uploaded++
                        }.onFailure { throwable ->
                            Napier.e(
                                message = "uploadPhoto failed for id=${photo.id}",
                                throwable = throwable,
                            )
                        }
                    }
                    uploaded.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "uploadPendingPhotos failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    @OptIn(ExperimentalTime::class)
    override suspend fun fetchAndApplyDeltaChanges(): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val lastSync = syncMetaStorage.selectByKey(SyncMetaStorage.KEY_LAST_SYNC_TIME)
                        ?: "2000-01-01T00:00:00Z"
                    val response = syncApiClient.fetchConfigChanges(lastSync).getOrThrow()
                    configChangesApplier.apply(response)
                    syncMetaStorage.upsert(
                        key = SyncMetaStorage.KEY_LAST_SYNC_TIME,
                        value = response.serverTime,
                    )
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "fetchAndApplyDeltaChanges failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    private fun markRejectedAsFailed(rejected: RemoteSyncRejected) {
        Napier.w(
            message = "Sync rejected: ${rejected.entityType} id=${rejected.entityId} reason=${rejected.reason}",
        )
        when (rejected.entityType) {
            RemoteSyncRejectedEntityType.INSPECTION ->
                inspectionStorage.updateInspectionSyncStatus(rejected.entityId, LocalSyncStatus.FAILED)
            RemoteSyncRejectedEntityType.INSPECTION_EQUIPMENT_RESULT ->
                inspectionStorage.updateEquipmentResultSyncStatus(rejected.entityId, LocalSyncStatus.FAILED)
            RemoteSyncRejectedEntityType.CHECKLIST_ITEM_RESULT ->
                inspectionStorage.updateChecklistItemResultSyncStatus(rejected.entityId, LocalSyncStatus.FAILED)
            RemoteSyncRejectedEntityType.UNKNOWN ->
                Napier.e("Sync rejected: unknown entityType for id=${rejected.entityId}")
        }
    }
}
