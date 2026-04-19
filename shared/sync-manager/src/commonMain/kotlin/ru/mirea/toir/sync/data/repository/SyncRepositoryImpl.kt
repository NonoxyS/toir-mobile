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
import ru.mirea.toir.sync.data.network.SyncApiClient
import ru.mirea.toir.sync.data.network.models.RemoteSyncActionLog
import ru.mirea.toir.sync.data.network.models.RemoteSyncChecklistItemResult
import ru.mirea.toir.sync.data.network.models.RemoteSyncEquipmentResult
import ru.mirea.toir.sync.data.network.models.RemoteSyncInspection
import ru.mirea.toir.sync.data.network.models.RemoteSyncPushRequest
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
                        clientBatchId = Uuid.Companion.random().toString(),
                        deviceId = deviceId,
                        sentAt = Clock.System.now().toString(),
                        inspections = pendingInspections.map { inspection ->
                            RemoteSyncInspection(
                                id = inspection.id,
                                assignmentId = inspection.assignmentId,
                                routeId = inspection.routeId,
                                status = inspection.status.localValue,
                                startedAt = inspection.startedAt,
                                completedAt = inspection.completedAt,
                            )
                        }.takeIf { it.isNotEmpty() },
                        inspectionEquipmentResults = pendingEquipmentResults.map { result ->
                            RemoteSyncEquipmentResult(
                                id = result.id,
                                inspectionId = result.inspectionId,
                                routePointId = result.routePointId,
                                equipmentId = result.equipmentId,
                                status = result.status.localValue,
                                startedAt = result.startedAt,
                                completedAt = result.completedAt,
                            )
                        }.takeIf { it.isNotEmpty() },
                        checklistItemResults = pendingChecklistResults.map { r ->
                            RemoteSyncChecklistItemResult(
                                id = r.id,
                                inspectionEquipmentResultId = r.equipmentResultId,
                                checklistItemId = r.checklistItemId,
                                valueBoolean = r.valueBoolean?.let { it == 1L },
                                valueNumber = r.valueNumber,
                                valueText = r.valueText,
                                valueSelect = r.valueSelect,
                                isConfirmed = r.isConfirmed == 1L,
                                answeredAt = r.answeredAt,
                            )
                        }.takeIf { it.isNotEmpty() },
                        actionLogs = pendingLogs.map { log ->
                            RemoteSyncActionLog(
                                id = log.id,
                                inspectionId = log.inspectionId,
                                actionType = log.actionType,
                                metadata = log.metadata,
                                createdAt = log.createdAt,
                            )
                        }.takeIf { it.isNotEmpty() },
                    )

                    val response = syncApiClient.pushSync(request).getOrThrow()
                    val acceptedInspections = response.accepted.inspections
                    val acceptedEquipment = response.accepted.inspectionEquipmentResults
                    val acceptedChecklist = response.accepted.checklistItemResults
                    val acceptedLogs = response.accepted.actionLogs

                    acceptedInspections.forEach {
                        inspectionStorage.updateInspectionSyncStatus(it, LocalSyncStatus.SYNCED)
                    }
                    acceptedEquipment.forEach {
                        inspectionStorage.updateEquipmentResultSyncStatus(
                            id = it,
                            syncStatus = LocalSyncStatus.SYNCED
                        )
                    }
                    acceptedChecklist.forEach {
                        inspectionStorage.updateChecklistItemResultSyncStatus(it, LocalSyncStatus.SYNCED)
                    }
                    acceptedLogs.forEach { actionLogStorage.updateSyncStatus(it, LocalSyncStatus.SYNCED) }

                    val totalAccepted = acceptedInspections.size + acceptedEquipment.size +
                        acceptedChecklist.size + acceptedLogs.size
                    val totalRejected = response.rejected.size

                    SyncResult(acceptedCount = totalAccepted, rejectedCount = totalRejected).wrapResultSuccess()
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
                        ).onSuccess {
                            photoStorage.updateSyncStatus(photo.id, LocalSyncStatus.SYNCED, null)
                            uploaded++
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
    override suspend fun fetchDeltaChanges(): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val lastSync = syncMetaStorage.selectByKey(SyncMetaStorage.Companion.KEY_LAST_SYNC_TIME)
                        ?: "2000-01-01T00:00:00Z"
                    syncApiClient.fetchConfigChanges(lastSync).getOrThrow()
                    syncMetaStorage.upsert(SyncMetaStorage.Companion.KEY_LAST_SYNC_TIME, Clock.System.now().toString())
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "fetchDeltaChanges failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
