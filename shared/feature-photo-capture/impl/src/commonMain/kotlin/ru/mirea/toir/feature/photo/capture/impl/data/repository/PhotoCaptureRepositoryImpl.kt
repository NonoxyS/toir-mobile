package ru.mirea.toir.feature.photo.capture.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.storage.action_log.ActionLogEntityType
import ru.mirea.toir.core.database.storage.action_log.ActionLogType
import ru.mirea.toir.core.database.storage.action_log.ActionLogger
import ru.mirea.toir.core.database.storage.photo.PhotoStorage
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore
import ru.mirea.toir.feature.photo.capture.impl.data.files.PhotoFileDeleter
import ru.mirea.toir.feature.photo.capture.impl.domain.repository.PhotoCaptureRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class PhotoCaptureRepositoryImpl(
    private val photoStorage: PhotoStorage,
    private val photoFileDeleter: PhotoFileDeleter,
    private val actionLogger: ActionLogger,
    private val coroutineDispatchers: CoroutineDispatchers,
) : PhotoCaptureRepository {

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun savePhoto(checklistItemResultId: String, fileUri: String): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val photoId = Uuid.random().toString()
                    photoStorage.insert(
                        id = photoId,
                        checklistItemResultId = checklistItemResultId,
                        fileUri = fileUri,
                        takenAt = Clock.System.now().toString(),
                    )
                    actionLogger.log(
                        actionType = ActionLogType.PHOTO_ATTACHED,
                        entityType = ActionLogEntityType.CHECKLIST_ITEM_RESULT,
                        entityId = checklistItemResultId,
                        payloadJson = """{"photoId":"$photoId"}""",
                    )
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "savePhoto failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    // Reactive read: SQLDelight asFlow re-emits on every photos-table change, so
    // file_uri NULL → URL transitions from sync manager light up the UI without
    // re-navigation. Errors propagate to the collector (rare for local SQLite reads).
    override fun observePhotos(
        checklistItemResultId: String,
    ): Flow<List<PhotoCaptureStore.PhotoEntry>> =
        photoStorage.observePhotosByChecklistItemResultId(checklistItemResultId)
            .map { list ->
                list.map { PhotoCaptureStore.PhotoEntry(id = it.id, fileUri = it.fileUri) }
            }

    override suspend fun deletePhoto(
        checklistItemResultId: String,
        fileUri: String,
    ): Result<Unit> = withContext(coroutineDispatchers.io) {
        coRunCatching(
            tryBlock = {
                val photo = photoStorage
                    .selectByChecklistItemResultId(checklistItemResultId)
                    .firstOrNull { it.fileUri == fileUri }
                    ?: error("Photo not found for uri=$fileUri")
                photoStorage.delete(photo.id)
                photoFileDeleter.delete(fileUri)
                actionLogger.log(
                    actionType = ActionLogType.PHOTO_DELETED,
                    entityType = ActionLogEntityType.CHECKLIST_ITEM_RESULT,
                    entityId = checklistItemResultId,
                    payloadJson = """{"photoId":"${photo.id}","fileUri":"$fileUri"}""",
                )
                Unit.wrapResultSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(message = "deletePhoto failed", throwable = throwable)
                throwable.wrapResultFailure()
            },
        )
    }
}
