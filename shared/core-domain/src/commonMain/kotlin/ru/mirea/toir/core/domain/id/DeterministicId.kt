package ru.mirea.toir.core.domain.id

import app.softwork.uuid.generateUuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Деривация id сущностей инспекции из их бизнес-ключа (UUID v5, name-based).
 *
 * id считается чистой функцией от бизнес-ключа, поэтому любое устройство и любой повторный
 * ввод дают один и тот же id. Push апсертит by id — дубликаты на один логический объект
 * физически не могут возникнуть (в т.ч. при входе в один профиль с двух устройств). Это
 * закрывает весь сценарий «продолжить инспекцию с другого телефона»: серверные unique-ключи
 * (`ier_inspection_route_point_unique`, `cir_ier_item_unique`) не нарушаются.
 *
 * Цепочка детерминируется сверху вниз: assignment → inspection → equipmentResult → itemResult.
 * Случайный [Uuid.random] такой гарантии не давал.
 */
@OptIn(ExperimentalUuidApi::class)
object DeterministicId {

    // Фиксированные namespace по типу сущности. Произвольные, но НЕИЗМЕННЫЕ константы:
    // менять нельзя — иначе ранее созданные id перестанут совпадать с новыми.
    private val INSPECTION_NS = Uuid.parse("9f3d8c2a-1b47-5e6f-a0d1-2c3b4a5d6e7f")
    private val EQUIPMENT_RESULT_NS = Uuid.parse("b7e2f1c4-3a58-5d9e-8c0f-1a2b3c4d5e6f")
    private val CHECKLIST_ITEM_RESULT_NS = Uuid.parse("c4d5e6f7-8a9b-5c0d-9e1f-2a3b4c5d6e7f")

    fun forInspection(assignmentId: String): String =
        Uuid.generateUuid(INSPECTION_NS, assignmentId).toString()

    fun forEquipmentResult(inspectionId: String, routePointId: String): String =
        Uuid.generateUuid(EQUIPMENT_RESULT_NS, "$inspectionId:$routePointId").toString()

    fun forChecklistItemResult(equipmentResultId: String, checklistItemId: String): String =
        Uuid.generateUuid(CHECKLIST_ITEM_RESULT_NS, "$equipmentResultId:$checklistItemId").toString()
}
