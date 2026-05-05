package ru.mirea.toir.feature.photo.capture.impl.data.repository

import io.github.aakira.napier.Napier
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
import ru.mirea.toir.feature.photo.capture.impl.domain.repository.PhotoCaptureRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class PhotoCaptureRepositoryImpl(
    private val photoStorage: PhotoStorage,
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

    override suspend fun getPhotos(checklistItemResultId: String): Result<List<String>> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    photoStorage.selectByChecklistItemResultId(checklistItemResultId)
                        .map { it.fileUri }
                        .wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "getPhotos failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
