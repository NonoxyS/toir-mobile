package ru.mirea.toir.core.database.storage.action_log

import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object ActionLogType {
    const val INSPECTION_STARTED = "inspection_started"
    const val ROUTE_POINT_OPENED = "route_point_opened"
    const val EQUIPMENT_OPENED = "equipment_opened"
    const val CHECKLIST_ITEM_UPDATED = "checklist_item_updated"
    const val PHOTO_ATTACHED = "photo_attached"
    const val EQUIPMENT_CHECK_COMPLETED = "equipment_check_completed"
    const val INSPECTION_COMPLETED = "inspection_completed"
    const val SYNC_STARTED = "sync_started"
    const val SYNC_COMPLETED = "sync_completed"
    const val SYNC_FAILED = "sync_failed"
}

object ActionLogEntityType {
    const val INSPECTION = "inspection"
    const val ROUTE_POINT = "route_point"
    const val EQUIPMENT = "equipment"
    const val EQUIPMENT_RESULT = "inspection_equipment_result"
    const val CHECKLIST_ITEM_RESULT = "checklist_item_result"
    const val PHOTO = "photo"
    const val SYNC_BATCH = "sync_batch"
}

class ActionLogger internal constructor(
    private val storage: ActionLogStorage,
) {

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    fun log(
        actionType: String,
        entityType: String? = null,
        entityId: String? = null,
        payloadJson: String? = null,
    ) {
        runCatching {
            storage.insert(
                id = Uuid.random().toString(),
                actionType = actionType,
                entityType = entityType,
                entityId = entityId,
                payloadJson = payloadJson,
                actionTime = Clock.System.now().toString(),
            )
        }.onFailure { throwable ->
            Napier.e("ActionLogger: failed to write log $actionType", throwable = throwable)
        }
    }
}
