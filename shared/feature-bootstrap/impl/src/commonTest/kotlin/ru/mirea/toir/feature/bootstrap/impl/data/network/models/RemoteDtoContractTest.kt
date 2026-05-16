@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.feature.bootstrap.impl.data.network.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import ru.mirea.toir.sync.data.network.models.RemoteConfigChangesResponse

/**
 * Контракт между bootstrap- и delta-путями восстановления: оба модуля
 * (`feature-bootstrap` и `sync-manager`) объявляют свои копии 4 sync-DTO
 * (`Inspection`, `EquipmentResult`, `ChecklistItemResult`, `Photo`) —
 * см. комментарий в [RemoteConfigChangesInspection]. Дублирование
 * сделано намеренно, чтобы избежать кросс-модульной зависимости фичевых
 * модулей через общие DTO. Минус подхода: компилятор не поймает
 * расхождение полей, если в одном из модулей поле добавили/убрали.
 *
 * Этот тест прижимает форму: один и тот же JSON десериализуется обоими
 * `RemoteBootstrapResponse` (bootstrap) и `RemoteConfigChangesResponse`
 * (delta), а затем поле в поле сравниваются результаты. Любой дрейф
 * упадёт здесь, а не в проде при первой синхронизации после изменения
 * бэкенда.
 *
 * Расположение: `feature-bootstrap/commonTest`, потому что только этот
 * модуль зависит и от своих `Remote*`, и от sync-manager (sync-manager на
 * feature-bootstrap не зависит — обратное направление). `@file:Suppress`
 * на `INVISIBLE_REFERENCE/INVISIBLE_MEMBER` нужен, чтобы достать
 * `internal data class` через границу модуля.
 */
internal class RemoteDtoContractTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `inspection dto shape is identical between bootstrap and delta`() {
        val bootstrap = json.decodeFromString<RemoteBootstrapResponse>(FULL_PAYLOAD_JSON)
        val delta = json.decodeFromString<RemoteConfigChangesResponse>(FULL_PAYLOAD_JSON)

        assertEquals(1, bootstrap.inspections.size)
        assertEquals(1, delta.inspections.size)
        val b = bootstrap.inspections.single()
        val d = delta.inspections.single()

        assertEquals(b.id, d.id)
        assertEquals(b.routeAssignmentId, d.routeAssignmentId)
        assertEquals(b.routeId, d.routeId)
        assertEquals(b.status, d.status)
        assertEquals(b.startedAt, d.startedAt)
        assertEquals(b.completedAt, d.completedAt)
        assertEquals(b.createdAt, d.createdAt)
        assertEquals(b.updatedAt, d.updatedAt)
    }

    @Test
    fun `inspection equipment result dto shape is identical between bootstrap and delta`() {
        val bootstrap = json.decodeFromString<RemoteBootstrapResponse>(FULL_PAYLOAD_JSON)
        val delta = json.decodeFromString<RemoteConfigChangesResponse>(FULL_PAYLOAD_JSON)

        assertEquals(1, bootstrap.inspectionEquipmentResults.size)
        assertEquals(1, delta.inspectionEquipmentResults.size)
        val b = bootstrap.inspectionEquipmentResults.single()
        val d = delta.inspectionEquipmentResults.single()

        assertEquals(b.id, d.id)
        assertEquals(b.inspectionId, d.inspectionId)
        assertEquals(b.equipmentId, d.equipmentId)
        assertEquals(b.routePointId, d.routePointId)
        assertEquals(b.status, d.status)
        assertEquals(b.startedAt, d.startedAt)
        assertEquals(b.completedAt, d.completedAt)
        assertEquals(b.createdAt, d.createdAt)
        assertEquals(b.updatedAt, d.updatedAt)
    }

    @Test
    fun `checklist item result dto shape is identical between bootstrap and delta`() {
        val bootstrap = json.decodeFromString<RemoteBootstrapResponse>(FULL_PAYLOAD_JSON)
        val delta = json.decodeFromString<RemoteConfigChangesResponse>(FULL_PAYLOAD_JSON)

        assertEquals(1, bootstrap.checklistItemResults.size)
        assertEquals(1, delta.checklistItemResults.size)
        val b = bootstrap.checklistItemResults.single()
        val d = delta.checklistItemResults.single()

        assertEquals(b.id, d.id)
        assertEquals(b.inspectionEquipmentResultId, d.inspectionEquipmentResultId)
        assertEquals(b.checklistItemId, d.checklistItemId)
        assertEquals(b.valueText, d.valueText)
        assertEquals(b.valueNumber, d.valueNumber)
        assertEquals(b.valueBoolean, d.valueBoolean)
        assertEquals(b.selectedOption, d.selectedOption)
        assertEquals(b.comment, d.comment)
        assertEquals(b.createdAt, d.createdAt)
        assertEquals(b.updatedAt, d.updatedAt)
    }

    @Test
    fun `photo dto shape is identical between bootstrap and delta`() {
        val bootstrap = json.decodeFromString<RemoteBootstrapResponse>(FULL_PAYLOAD_JSON)
        val delta = json.decodeFromString<RemoteConfigChangesResponse>(FULL_PAYLOAD_JSON)

        assertEquals(1, bootstrap.photos.size)
        assertEquals(1, delta.photos.size)
        val b = bootstrap.photos.single()
        val d = delta.photos.single()

        assertEquals(b.id, d.id)
        assertEquals(b.checklistItemResultId, d.checklistItemResultId)
        assertEquals(b.fileName, d.fileName)
        assertEquals(b.mimeType, d.mimeType)
        assertEquals(b.sizeBytes, d.sizeBytes)
        assertEquals(b.checksum, d.checksum)
        assertEquals(b.createdAt, d.createdAt)
        assertEquals(b.uploadedAt, d.uploadedAt)
    }

    companion object {
        /**
         * Полный payload с одной записью на каждую sync-сущность. Поля заполнены
         * фиксированными значениями (не `null` там, где допустимо `null`), чтобы
         * любое расхождение в `@SerialName`/типе всплыло на десериализации.
         * Минимально-необходимый набор внешних полей (`serverTime`, `deletedIds`)
         * заполнен, чтобы `RemoteConfigChangesResponse` тоже распарсился —
         * `RemoteBootstrapResponse` к ним нейтрален (отсутствие поля игнорируется).
         */
        private const val FULL_PAYLOAD_JSON = """
        {
          "user": null,
          "device": null,
          "serverTime": "2026-05-15T10:00:00Z",
          "deletedIds": {
            "assignments": [],
            "routes": [],
            "routePoints": [],
            "equipment": [],
            "locations": [],
            "checklists": [],
            "checklistItems": []
          },
          "inspections": [
            {
              "id": "11111111-1111-1111-1111-111111111111",
              "routeAssignmentId": "22222222-2222-2222-2222-222222222222",
              "routeId": "33333333-3333-3333-3333-333333333333",
              "status": "in_progress",
              "startedAt": "2026-05-15T09:00:00Z",
              "completedAt": "2026-05-15T09:30:00Z",
              "createdAt": "2026-05-15T08:00:00Z",
              "updatedAt": "2026-05-15T09:30:00Z"
            }
          ],
          "inspectionEquipmentResults": [
            {
              "id": "44444444-4444-4444-4444-444444444444",
              "inspectionId": "11111111-1111-1111-1111-111111111111",
              "equipmentId": "55555555-5555-5555-5555-555555555555",
              "routePointId": "66666666-6666-6666-6666-666666666666",
              "status": "in_progress",
              "startedAt": "2026-05-15T09:05:00Z",
              "completedAt": "2026-05-15T09:25:00Z",
              "createdAt": "2026-05-15T08:05:00Z",
              "updatedAt": "2026-05-15T09:25:00Z"
            }
          ],
          "checklistItemResults": [
            {
              "id": "77777777-7777-7777-7777-777777777777",
              "inspectionEquipmentResultId": "44444444-4444-4444-4444-444444444444",
              "checklistItemId": "88888888-8888-8888-8888-888888888888",
              "valueText": "text value",
              "valueNumber": 42.5,
              "valueBoolean": true,
              "selectedOption": "option-A",
              "comment": "user comment",
              "createdAt": "2026-05-15T09:10:00Z",
              "updatedAt": "2026-05-15T09:20:00Z"
            }
          ],
          "photos": [
            {
              "id": "99999999-9999-9999-9999-999999999999",
              "checklistItemResultId": "77777777-7777-7777-7777-777777777777",
              "fileName": "photo.jpg",
              "mimeType": "image/jpeg",
              "sizeBytes": 1024,
              "checksum": "abc123",
              "createdAt": "2026-05-15T09:15:00Z",
              "uploadedAt": "2026-05-15T09:16:00Z"
            }
          ]
        }
        """
    }
}
