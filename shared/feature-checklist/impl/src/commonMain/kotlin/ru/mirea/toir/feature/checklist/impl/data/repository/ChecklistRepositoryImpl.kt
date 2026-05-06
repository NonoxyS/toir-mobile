package ru.mirea.toir.feature.checklist.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.storage.action_log.ActionLogEntityType
import ru.mirea.toir.core.database.storage.action_log.ActionLogType
import ru.mirea.toir.core.database.storage.action_log.ActionLogger
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorage
import ru.mirea.toir.core.database.storage.checklist.models.LocalChecklistItem
import ru.mirea.toir.core.database.storage.inspection.InspectionStorage
import ru.mirea.toir.core.database.storage.inspection.models.LocalChecklistItemResult
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.photo.PhotoStorage
import ru.mirea.toir.core.database.storage.route.RouteStorage
import ru.mirea.toir.feature.checklist.api.models.DomainAnswerType
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import ru.mirea.toir.feature.checklist.impl.domain.repository.ChecklistRepository

private const val ANSWER_TYPE_BOOLEAN = "boolean"
private const val ANSWER_TYPE_NUMBER = "number"
private const val ANSWER_TYPE_TEXT = "text"
private const val ANSWER_TYPE_SELECT = "select"
private const val ANSWER_TYPE_CONFIRMATION = "confirmation"

internal class ChecklistRepositoryImpl(
    private val inspectionStorage: InspectionStorage,
    private val checklistStorage: ChecklistStorage,
    private val routeStorage: RouteStorage,
    private val photoStorage: PhotoStorage,
    private val actionLogger: ActionLogger,
    private val coroutineDispatchers: CoroutineDispatchers,
) : ChecklistRepository {

    private val json: Json = Json { ignoreUnknownKeys = true }

    override suspend fun getChecklistItems(
        equipmentResultId: String,
    ): Result<List<DomainChecklistItem>> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val equipmentResult = inspectionStorage.selectEquipmentResultById(equipmentResultId)
                        ?: error("EquipmentResult not found: $equipmentResultId")
                    val routePoint = routeStorage.selectPointById(equipmentResult.routePointId)
                        ?: error("RoutePoint not found: ${equipmentResult.routePointId}")

                    val checklistItems = checklistStorage.selectItemsByChecklistId(routePoint.checklistId)
                    val existingResults = inspectionStorage
                        .selectChecklistItemResultsByEquipmentResult(equipmentResultId)
                        .associateBy { it.checklistItemId }

                    checklistItems
                        .map { localItem ->
                            val resultEntry = existingResults[localItem.id]
                            val photoCount = resultEntry
                                ?.let { photoStorage.selectByChecklistItemResultId(it.id).size }
                                ?: 0
                            localItem.toDomain(resultEntry, photoCount)
                        }
                        .wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "getChecklistItems failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override suspend fun saveBooleanAnswer(
        equipmentResultId: String,
        itemId: String,
        value: Boolean?,
    ): Result<Unit> = saveAnswer(
        equipmentResultId = equipmentResultId,
        itemId = itemId,
        valueBoolean = value?.let { if (it) 1L else 0L },
    )

    override suspend fun saveNumberAnswer(
        equipmentResultId: String,
        itemId: String,
        value: Double,
    ): Result<Unit> = saveAnswer(
        equipmentResultId = equipmentResultId,
        itemId = itemId,
        valueNumber = value,
    )

    override suspend fun saveTextAnswer(
        equipmentResultId: String,
        itemId: String,
        value: String,
    ): Result<Unit> = saveAnswer(
        equipmentResultId = equipmentResultId,
        itemId = itemId,
        valueText = value,
    )

    override suspend fun saveSelectAnswer(
        equipmentResultId: String,
        itemId: String,
        value: String,
    ): Result<Unit> = saveAnswer(
        equipmentResultId = equipmentResultId,
        itemId = itemId,
        selectedOption = value,
    )

    override suspend fun saveConfirm(
        equipmentResultId: String,
        itemId: String,
        value: Boolean,
    ): Result<Unit> = saveAnswer(
        equipmentResultId = equipmentResultId,
        itemId = itemId,
        valueBoolean = if (value) 1L else null,
    )

    @OptIn(ExperimentalTime::class)
    override suspend fun finishChecklist(equipmentResultId: String): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val existing = inspectionStorage.selectEquipmentResultById(equipmentResultId)
                        ?: error("EquipmentResult not found: $equipmentResultId")
                    val now = Clock.System.now().toString()
                    inspectionStorage.updateEquipmentResultStatus(
                        id = equipmentResultId,
                        status = LocalEquipmentResultStatus.COMPLETED,
                        startedAt = existing.startedAt,
                        completedAt = now,
                        updatedAt = now,
                    )
                    actionLogger.log(
                        actionType = ActionLogType.EQUIPMENT_CHECK_COMPLETED,
                        entityType = ActionLogEntityType.EQUIPMENT_RESULT,
                        entityId = equipmentResultId,
                    )
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "finishChecklist failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    @Suppress("LongParameterList")
    private suspend fun saveAnswer(
        equipmentResultId: String,
        itemId: String,
        valueBoolean: Long? = null,
        valueNumber: Double? = null,
        valueText: String? = null,
        selectedOption: String? = null,
    ): Result<Unit> = withContext(coroutineDispatchers.io) {
        coRunCatching(
            tryBlock = {
                val existing = inspectionStorage.selectChecklistItemResult(
                    checklistItemId = itemId,
                    equipmentResultId = equipmentResultId,
                )
                val resultId = existing?.id ?: Uuid.random().toString()
                val now = Clock.System.now().toString()
                val createdAt = existing?.createdAt ?: now
                inspectionStorage.insertOrReplaceChecklistItemResult(
                    id = resultId,
                    equipmentResultId = equipmentResultId,
                    checklistItemId = itemId,
                    valueBoolean = valueBoolean,
                    valueNumber = valueNumber,
                    valueText = valueText,
                    selectedOption = selectedOption,
                    comment = existing?.comment,
                    createdAt = createdAt,
                    updatedAt = now,
                )
                actionLogger.log(
                    actionType = ActionLogType.CHECKLIST_ITEM_UPDATED,
                    entityType = ActionLogEntityType.CHECKLIST_ITEM_RESULT,
                    entityId = resultId,
                )
                Unit.wrapResultSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(message = "saveAnswer failed", throwable = throwable)
                throwable.wrapResultFailure()
            },
        )
    }

    private fun LocalChecklistItem.toDomain(
        result: LocalChecklistItemResult?,
        photoCount: Int,
    ): DomainChecklistItem {
        val type = answerType.toDomainAnswerType(parseSelectOptions(selectOptions))
        val isConfirmed = type == DomainAnswerType.Confirm && (result?.valueBoolean == 1L)
        return DomainChecklistItem(
            id = id,
            title = title,
            description = description,
            answerType = type,
            isRequired = isRequired == 1L,
            requiresPhoto = requiresPhoto == 1L,
            resultId = result?.id,
            valueBoolean = result?.valueBoolean?.let { it == 1L },
            valueNumber = result?.valueNumber,
            valueText = result?.valueText,
            valueSelect = result?.selectedOption,
            isConfirmed = isConfirmed,
            photoCount = photoCount,
            numericMin = numericMin,
            numericMax = numericMax,
        )
    }

    private fun String.toDomainAnswerType(options: List<String>): DomainAnswerType =
        when (lowercase()) {
            ANSWER_TYPE_BOOLEAN -> DomainAnswerType.Boolean
            ANSWER_TYPE_NUMBER -> DomainAnswerType.Number
            ANSWER_TYPE_TEXT -> DomainAnswerType.Text
            ANSWER_TYPE_SELECT -> DomainAnswerType.Select(options.toImmutableList())
            ANSWER_TYPE_CONFIRMATION -> DomainAnswerType.Confirm
            else -> DomainAnswerType.Text
        }

    private fun parseSelectOptions(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(String.serializer()), raw)
        }.getOrElse { throwable ->
            Napier.e(message = "Failed to parse select_options: $raw", throwable = throwable)
            emptyList()
        }
    }
}
