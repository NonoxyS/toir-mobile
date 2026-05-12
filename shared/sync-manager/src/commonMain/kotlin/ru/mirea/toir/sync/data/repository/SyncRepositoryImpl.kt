package ru.mirea.toir.sync.data.repository

import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.withContext
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.auth.data.storage.TokenStorage
import ru.mirea.toir.core.database.TransactionRunner
import ru.mirea.toir.core.database.storage.action_log.ActionLogStorage
import ru.mirea.toir.core.database.storage.action_log.LocalActionLog
import ru.mirea.toir.core.database.storage.inspection.InspectionStorage
import ru.mirea.toir.core.database.storage.inspection.models.LocalChecklistItemResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalInspection
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
import ru.mirea.toir.sync.domain.SyncFailureReason
import ru.mirea.toir.sync.domain.models.SyncResult
import ru.mirea.toir.sync.domain.repository.SyncRepository
import ru.mirea.toir.sync.domain.retry.BackoffPolicy
import ru.mirea.toir.sync.domain.toSyncFailureReason

@OptIn(ExperimentalTime::class)
internal class SyncRepositoryImpl(
    private val syncApiClient: SyncApiClient,
    private val inspectionStorage: InspectionStorage,
    private val photoStorage: PhotoStorage,
    private val actionLogStorage: ActionLogStorage,
    private val syncMetaStorage: SyncMetaStorage,
    private val tokenStorage: TokenStorage,
    private val configChangesApplier: ConfigChangesApplier,
    private val transactionRunner: TransactionRunner,
    private val coroutineDispatchers: CoroutineDispatchers,
) : SyncRepository {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun pushPendingData(): Result<SyncResult> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val now = Clock.System.now()
                    val nowIso = now.toString()
                    val deviceId = tokenStorage.getOrCreateDeviceCode()

                    val pendingInspections = inspectionStorage.selectPendingInspections(nowIso)
                    val pendingEquipmentResults = inspectionStorage.selectPendingEquipmentResults(nowIso)
                    val pendingChecklistResults =
                        inspectionStorage.selectPendingChecklistItemResults(nowIso)
                    val pendingLogs = actionLogStorage.selectPending(nowIso)

                    if (pendingInspections.isEmpty() && pendingEquipmentResults.isEmpty() &&
                        pendingChecklistResults.isEmpty() && pendingLogs.isEmpty()
                    ) {
                        return@coRunCatching SyncResult(
                            acceptedCount = 0,
                            rejectedCount = 0,
                        ).wrapResultSuccess()
                    }

                    val request = buildPushRequest(
                        clientBatchId = Uuid.random().toString(),
                        deviceId = deviceId,
                        sentAt = nowIso,
                        inspections = pendingInspections,
                        equipmentResults = pendingEquipmentResults,
                        checklistResults = pendingChecklistResults,
                        logs = pendingLogs,
                    )

                    val response = try {
                        syncApiClient.pushSync(request).getOrThrow()
                    } catch (throwable: Throwable) {
                        scheduleBatchRetry(
                            now = now,
                            reason = throwable.toSyncFailureReason(),
                            inspections = pendingInspections,
                            equipmentResults = pendingEquipmentResults,
                            checklistResults = pendingChecklistResults,
                            logs = pendingLogs,
                        )
                        throw throwable
                    }

                    val inspectionsById = pendingInspections.associateBy { it.id }
                    val equipmentById = pendingEquipmentResults.associateBy { it.id }
                    val checklistById = pendingChecklistResults.associateBy { it.id }

                    transactionRunner.transactional {
                        response.accepted.inspections.forEach { id ->
                            inspectionStorage.markInspectionSynced(id)
                        }
                        response.accepted.inspectionEquipmentResults.forEach { id ->
                            inspectionStorage.markEquipmentResultSynced(id)
                        }
                        response.accepted.checklistItemResults.forEach { id ->
                            inspectionStorage.markChecklistItemResultSynced(id)
                        }
                        response.accepted.actionLogs.forEach { id ->
                            actionLogStorage.markSynced(id)
                        }
                        response.rejected.forEach { rejected ->
                            scheduleSingleRetry(
                                rejected = rejected,
                                now = now,
                                inspectionsById = inspectionsById,
                                equipmentResultsById = equipmentById,
                                checklistResultsById = checklistById,
                            )
                        }
                    }

                    val acceptedCount = response.accepted.inspections.size +
                        response.accepted.inspectionEquipmentResults.size +
                        response.accepted.checklistItemResults.size +
                        response.accepted.actionLogs.size

                    SyncResult(
                        acceptedCount = acceptedCount,
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
                    val now = Clock.System.now()
                    val pending = photoStorage.selectPendingPhotos(now.toString())
                    var uploaded = 0
                    pending.forEach { photo ->
                        val bytes = readFileBytes(photo.fileUri)
                        syncApiClient.uploadPhoto(
                            photoId = photo.id,
                            checklistItemResultId = photo.checklistItemResultId,
                            fileBytes = bytes,
                        ).onSuccess { response ->
                            photoStorage.markPhotoSynced(
                                id = photo.id,
                                storageKey = response.storageKey,
                            )
                            uploaded++
                        }.onFailure { throwable ->
                            Napier.e(
                                message = "uploadPhoto failed for id=${photo.id}",
                                throwable = throwable,
                            )
                            val newAttempt = photo.syncAttemptCount + 1
                            photoStorage.markPhotoRetryScheduled(
                                id = photo.id,
                                attemptCount = newAttempt,
                                nextAttemptAt = nextAttemptIso(now, newAttempt),
                                lastError = throwable.toSyncFailureReason().name,
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

    override suspend fun fetchAndApplyDeltaChanges(): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val lastSync = syncMetaStorage.selectByKey(SyncMetaStorage.KEY_LAST_SYNC_TIME)
                        ?: DEFAULT_DELTA_START
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

    private fun buildPushRequest(
        clientBatchId: String,
        deviceId: String,
        sentAt: String,
        inspections: List<LocalInspection>,
        equipmentResults: List<LocalEquipmentResult>,
        checklistResults: List<LocalChecklistItemResult>,
        logs: List<LocalActionLog>,
    ) = RemoteSyncPushRequest(
        clientBatchId = clientBatchId,
        deviceId = deviceId,
        sentAt = sentAt,
        inspections = inspections.map { inspection ->
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
        inspectionEquipmentResults = equipmentResults.map { result ->
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
        checklistItemResults = checklistResults.map { r ->
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
        actionLogs = logs.map { log ->
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

    private fun scheduleBatchRetry(
        now: Instant,
        reason: SyncFailureReason,
        inspections: List<LocalInspection>,
        equipmentResults: List<LocalEquipmentResult>,
        checklistResults: List<LocalChecklistItemResult>,
        logs: List<LocalActionLog>,
    ) {
        val total =
            inspections.size + equipmentResults.size + checklistResults.size + logs.size
        Napier.w("Batch push failed (reason=$reason); scheduling retry for $total records")
        transactionRunner.transactional {
            inspections.forEach { item ->
                val newAttempt = item.syncAttemptCount + 1
                inspectionStorage.markInspectionRetryScheduled(
                    id = item.id,
                    attemptCount = newAttempt,
                    nextAttemptAt = nextAttemptIso(now, newAttempt),
                    lastError = reason.name,
                )
            }
            equipmentResults.forEach { item ->
                val newAttempt = item.syncAttemptCount + 1
                inspectionStorage.markEquipmentResultRetryScheduled(
                    id = item.id,
                    attemptCount = newAttempt,
                    nextAttemptAt = nextAttemptIso(now, newAttempt),
                    lastError = reason.name,
                )
            }
            checklistResults.forEach { item ->
                val newAttempt = item.syncAttemptCount + 1
                inspectionStorage.markChecklistItemResultRetryScheduled(
                    id = item.id,
                    attemptCount = newAttempt,
                    nextAttemptAt = nextAttemptIso(now, newAttempt),
                    lastError = reason.name,
                )
            }
            logs.forEach { item ->
                val newAttempt = item.syncAttemptCount + 1
                actionLogStorage.markRetryScheduled(
                    id = item.id,
                    attemptCount = newAttempt,
                    nextAttemptAt = nextAttemptIso(now, newAttempt),
                    lastError = reason.name,
                )
            }
        }
    }

    private fun scheduleSingleRetry(
        rejected: RemoteSyncRejected,
        now: Instant,
        inspectionsById: Map<String, LocalInspection>,
        equipmentResultsById: Map<String, LocalEquipmentResult>,
        checklistResultsById: Map<String, LocalChecklistItemResult>,
    ) {
        Napier.w(
            message = "Sync rejected: ${rejected.entityType} id=${rejected.entityId} reason=${rejected.reason}",
        )
        when (rejected.entityType) {
            RemoteSyncRejectedEntityType.INSPECTION -> {
                val current = inspectionsById[rejected.entityId]?.syncAttemptCount ?: 0L
                val next = current + 1
                inspectionStorage.markInspectionRetryScheduled(
                    id = rejected.entityId,
                    attemptCount = next,
                    nextAttemptAt = nextAttemptIso(now, next),
                    lastError = rejected.reason.name,
                )
            }
            RemoteSyncRejectedEntityType.INSPECTION_EQUIPMENT_RESULT -> {
                val current = equipmentResultsById[rejected.entityId]?.syncAttemptCount ?: 0L
                val next = current + 1
                inspectionStorage.markEquipmentResultRetryScheduled(
                    id = rejected.entityId,
                    attemptCount = next,
                    nextAttemptAt = nextAttemptIso(now, next),
                    lastError = rejected.reason.name,
                )
            }
            RemoteSyncRejectedEntityType.CHECKLIST_ITEM_RESULT -> {
                val current = checklistResultsById[rejected.entityId]?.syncAttemptCount ?: 0L
                val next = current + 1
                inspectionStorage.markChecklistItemResultRetryScheduled(
                    id = rejected.entityId,
                    attemptCount = next,
                    nextAttemptAt = nextAttemptIso(now, next),
                    lastError = rejected.reason.name,
                )
            }
            RemoteSyncRejectedEntityType.UNKNOWN ->
                Napier.e("Sync rejected: unknown entityType for id=${rejected.entityId}")
        }
    }

    private fun nextAttemptIso(now: Instant, attemptCount: Long): String =
        (now + BackoffPolicy.nextDelay(attemptCount)).toString()

    private companion object {
        const val DEFAULT_DELTA_START = "2000-01-01T00:00:00Z"
    }
}
