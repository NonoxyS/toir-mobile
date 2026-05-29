package ru.mirea.toir.sync.data.repository

import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
import ru.mirea.toir.sync.data.mappers.toDomain
import ru.mirea.toir.sync.data.mappers.toLocal
import ru.mirea.toir.sync.data.network.SyncApiClient
import ru.mirea.toir.sync.data.network.models.RemoteSyncActionLog
import ru.mirea.toir.sync.data.network.models.RemoteSyncChecklistItemResult
import ru.mirea.toir.sync.data.network.models.RemoteSyncEquipmentResult
import ru.mirea.toir.sync.data.network.models.RemoteSyncInspection
import ru.mirea.toir.sync.data.network.models.RemoteSyncPushRequest
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejected
import ru.mirea.toir.sync.data.network.models.RemoteSyncRejectedEntityType
import ru.mirea.toir.sync.data.FileReader
import ru.mirea.toir.sync.data.PhotoFileWriter
import ru.mirea.toir.sync.domain.DomainPendingInspection
import ru.mirea.toir.sync.domain.SyncFailureReason
import ru.mirea.toir.sync.domain.models.SyncResult
import ru.mirea.toir.sync.domain.repository.SyncRepository
import ru.mirea.toir.sync.domain.retry.BackoffPolicy
import ru.mirea.toir.sync.domain.toSyncFailureReason

private enum class RetryEntity {
    INSPECTION,
    EQUIPMENT_RESULT,
    CHECKLIST_ITEM_RESULT,
    ACTION_LOG,
}

@OptIn(ExperimentalTime::class)
internal class SyncRepositoryImpl(
    private val syncApiClient: SyncApiClient,
    private val inspectionStorage: InspectionStorage,
    private val photoStorage: PhotoStorage,
    private val actionLogStorage: ActionLogStorage,
    private val syncMetaStorage: SyncMetaStorage,
    private val tokenStorage: TokenStorage,
    private val configChangesApplier: ConfigChangesApplier,
    private val photoFileWriter: PhotoFileWriter,
    private val fileReader: FileReader,
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
                    val deviceId = tokenStorage.getDeviceId()
                        ?: error("Device is not registered. Re-login required.")

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
                            inspections = pendingInspections,
                            equipmentResults = pendingEquipmentResults,
                            checklistResults = pendingChecklistResults,
                            logs = pendingLogs,
                        )
                        throw throwable
                    }

                    val attemptsById = buildAttemptsLookup(
                        inspections = pendingInspections,
                        equipmentResults = pendingEquipmentResults,
                        checklistResults = pendingChecklistResults,
                        logs = pendingLogs,
                    )

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
                            handleRejected(rejected = rejected, now = now, attemptsById = attemptsById)
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

    override suspend fun uploadPendingPhotos(): Result<Long> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val now = Clock.System.now()
                    val pending = photoStorage.selectPendingPhotos(now.toString())
                    var uploaded = 0L
                    pending.forEach { photo ->
                        // A pending photo must have a local file_uri set by the capture flow.
                        // Restored photos are inserted with file_uri = null but sync_status = 'synced',
                        // so they never appear here. Defensive null-skip keeps the type system honest.
                        val fileUri = photo.fileUri ?: run {
                            Napier.w(message = "Pending photo without fileUri skipped: id=${photo.id}")
                            return@forEach
                        }
                        // Reading the local file can fail independently of the network (file
                        // removed by user/system, wrong URI scheme, …). Isolate the failure
                        // to this photo: mark it for retry and continue, so one broken file
                        // does not abort push/delta/download for the rest of the cycle.
                        val bytes = runCatching { fileReader.read(fileUri) }.getOrElse { throwable ->
                            Napier.e(
                                message = "readPhotoBytes failed for id=${photo.id}",
                                throwable = throwable,
                            )
                            val newAttempt = photo.syncAttemptCount + 1
                            photoStorage.markPhotoRetryScheduled(
                                id = photo.id,
                                attemptCount = newAttempt,
                                nextAttemptAt = nextAttemptIso(now, newAttempt),
                                lastError = throwable.toSyncFailureReason().name,
                            )
                            return@forEach
                        }
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

    override suspend fun downloadMissingPhotos(): Result<Long> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val missing = photoStorage.selectMissingFiles()
                    var downloaded = 0L
                    missing.forEach { photo ->
                        // Per-photo isolation: any failure (HTTP / disk / parse) is logged and we
                        // continue with the next photo. The row stays in selectMissingFiles until
                        // file_uri is set, so the next sync cycle retries it automatically.
                        syncApiClient.downloadPhoto(photo.id)
                            .mapCatching { remote ->
                                val localUri = photoFileWriter.write(
                                    photoId = photo.id,
                                    bytes = remote.bytes,
                                    mimeType = remote.mimeType,
                                )
                                photoStorage.setFileUri(id = photo.id, fileUri = localUri)
                                downloaded++
                            }
                            .onFailure { throwable ->
                                Napier.e(
                                    message = "downloadPhoto/write failed for id=${photo.id}",
                                    throwable = throwable,
                                )
                            }
                    }
                    downloaded.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "downloadMissingPhotos failed", throwable = throwable)
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
                    transactionRunner.transactional {
                        configChangesApplier.apply(response)
                        syncMetaStorage.upsert(
                            key = SyncMetaStorage.KEY_LAST_SYNC_TIME,
                            value = response.serverTime,
                        )
                    }
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "fetchAndApplyDeltaChanges failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override fun observeHasPending(): Flow<Boolean> =
        inspectionStorage.observeHasPending()

    override fun observePendingInspections(): Flow<List<DomainPendingInspection>> =
        inspectionStorage.observePendingInspections()
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun recordSuccessfulRun(finishedAt: Instant) {
        withContext(coroutineDispatchers.io) {
            syncMetaStorage.upsert(
                key = SyncMetaStorage.KEY_LAST_SYNC_AT_SUCCESS,
                value = finishedAt.toString(),
            )
        }
    }

    override suspend fun recordFailedRun(finishedAt: Instant, reason: SyncFailureReason) {
        withContext(coroutineDispatchers.io) {
            transactionRunner.transactional {
                syncMetaStorage.upsert(
                    key = SyncMetaStorage.KEY_LAST_SYNC_ERROR_REASON,
                    value = reason.name,
                )
                syncMetaStorage.upsert(
                    key = SyncMetaStorage.KEY_LAST_SYNC_ERROR_AT,
                    value = finishedAt.toString(),
                )
            }
        }
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

    private fun buildAttemptsLookup(
        inspections: List<LocalInspection>,
        equipmentResults: List<LocalEquipmentResult>,
        checklistResults: List<LocalChecklistItemResult>,
        logs: List<LocalActionLog>,
    ): Map<Pair<RetryEntity, String>, Long> = buildMap {
        inspections.forEach { put(RetryEntity.INSPECTION to it.id, it.syncAttemptCount) }
        equipmentResults.forEach { put(RetryEntity.EQUIPMENT_RESULT to it.id, it.syncAttemptCount) }
        checklistResults.forEach { put(RetryEntity.CHECKLIST_ITEM_RESULT to it.id, it.syncAttemptCount) }
        logs.forEach { put(RetryEntity.ACTION_LOG to it.id, it.syncAttemptCount) }
    }

    private fun scheduleBatchRetry(
        now: Instant,
        inspections: List<LocalInspection>,
        equipmentResults: List<LocalEquipmentResult>,
        checklistResults: List<LocalChecklistItemResult>,
        logs: List<LocalActionLog>,
    ) {
        val total =
            inspections.size + equipmentResults.size + checklistResults.size + logs.size
        Napier.w("Batch push failed; scheduling retry for $total records")
        transactionRunner.transactional {
            inspections.forEach {
                scheduleRetry(RetryEntity.INSPECTION, it.id, it.syncAttemptCount, now)
            }
            equipmentResults.forEach {
                scheduleRetry(RetryEntity.EQUIPMENT_RESULT, it.id, it.syncAttemptCount, now)
            }
            checklistResults.forEach {
                scheduleRetry(RetryEntity.CHECKLIST_ITEM_RESULT, it.id, it.syncAttemptCount, now)
            }
            logs.forEach {
                scheduleRetry(RetryEntity.ACTION_LOG, it.id, it.syncAttemptCount, now)
            }
        }
    }

    private fun handleRejected(
        rejected: RemoteSyncRejected,
        now: Instant,
        attemptsById: Map<Pair<RetryEntity, String>, Long>,
    ) {
        Napier.w(
            "Sync rejected: ${rejected.entityType} id=${rejected.entityId} reason=${rejected.reason}",
        )
        val entity = when (rejected.entityType) {
            RemoteSyncRejectedEntityType.INSPECTION -> RetryEntity.INSPECTION
            RemoteSyncRejectedEntityType.INSPECTION_EQUIPMENT_RESULT -> RetryEntity.EQUIPMENT_RESULT
            RemoteSyncRejectedEntityType.CHECKLIST_ITEM_RESULT -> RetryEntity.CHECKLIST_ITEM_RESULT
            RemoteSyncRejectedEntityType.UNKNOWN -> {
                Napier.e("Sync rejected with unknown entityType id=${rejected.entityId}; skipping")
                return
            }
        }
        val current = attemptsById[entity to rejected.entityId] ?: run {
            Napier.e(
                "Server rejected id=${rejected.entityId} (entity=$entity) which was not in pushed batch; skipping",
            )
            return
        }
        val localReason = rejected.reason.toLocal()
        val nextAttempt = current + 1
        val nextAt = nextAttemptIso(now, nextAttempt)
        when (entity) {
            RetryEntity.INSPECTION -> inspectionStorage.markInspectionRejected(
                id = rejected.entityId,
                attemptCount = nextAttempt,
                nextAttemptAt = nextAt,
                reason = localReason,
            )
            RetryEntity.EQUIPMENT_RESULT -> inspectionStorage.markEquipmentResultRejected(
                id = rejected.entityId,
                attemptCount = nextAttempt,
                nextAttemptAt = nextAt,
                reason = localReason,
            )
            RetryEntity.CHECKLIST_ITEM_RESULT -> inspectionStorage.markChecklistItemResultRejected(
                id = rejected.entityId,
                attemptCount = nextAttempt,
                nextAttemptAt = nextAt,
                reason = localReason,
            )
            RetryEntity.ACTION_LOG -> Napier.e(
                "ActionLog reject not expected; skipping id=${rejected.entityId}",
            )
        }
    }

    private fun scheduleRetry(
        entity: RetryEntity,
        id: String,
        currentAttempt: Long,
        now: Instant,
    ) {
        val newAttempt = currentAttempt + 1
        val nextAt = nextAttemptIso(now, newAttempt)
        when (entity) {
            RetryEntity.INSPECTION -> inspectionStorage.markInspectionRetryScheduled(
                id = id,
                attemptCount = newAttempt,
                nextAttemptAt = nextAt,
            )
            RetryEntity.EQUIPMENT_RESULT -> inspectionStorage.markEquipmentResultRetryScheduled(
                id = id,
                attemptCount = newAttempt,
                nextAttemptAt = nextAt,
            )
            RetryEntity.CHECKLIST_ITEM_RESULT -> inspectionStorage.markChecklistItemResultRetryScheduled(
                id = id,
                attemptCount = newAttempt,
                nextAttemptAt = nextAt,
            )
            RetryEntity.ACTION_LOG -> actionLogStorage.markRetryScheduled(
                id = id,
                attemptCount = newAttempt,
                nextAttemptAt = nextAt,
                lastError = null,
            )
        }
    }

    private fun nextAttemptIso(now: Instant, attemptCount: Long): String =
        (now + BackoffPolicy.nextDelay(attemptCount)).toString()

    private companion object {
        const val DEFAULT_DELTA_START = "2000-01-01T00:00:00Z"
    }
}
