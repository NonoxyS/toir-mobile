package ru.mirea.toir.sync.data.applier

import io.github.aakira.napier.Napier
import ru.mirea.toir.core.database.TransactionRunner
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalRouteAssignmentStatus
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorage
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorage
import ru.mirea.toir.core.database.storage.inspection.InspectionStorage
import ru.mirea.toir.core.database.storage.inspection.models.LocalEquipmentResultStatus
import ru.mirea.toir.core.database.storage.location.LocationStorage
import ru.mirea.toir.core.database.storage.photo.PhotoStorage
import ru.mirea.toir.core.database.storage.route.RouteStorage
import ru.mirea.toir.sync.data.network.models.RemoteConfigChangesResponse

internal class ConfigChangesApplier(
    private val routeStorage: RouteStorage,
    private val equipmentStorage: EquipmentStorage,
    private val locationStorage: LocationStorage,
    private val checklistStorage: ChecklistStorage,
    private val inspectionStorage: InspectionStorage,
    private val photoStorage: PhotoStorage,
    private val transactionRunner: TransactionRunner,
) {

    fun apply(response: RemoteConfigChangesResponse) = transactionRunner.transactional {
        response.locations.forEach { loc ->
            locationStorage.upsert(
                id = loc.id,
                code = loc.code,
                name = loc.name,
                description = loc.description,
                parentLocationId = loc.parentLocationId,
            )
        }

        response.equipment.forEach { eq ->
            equipmentStorage.upsert(
                id = eq.id,
                code = eq.code,
                name = eq.name,
                type = eq.type,
                locationId = eq.locationId,
                qrCode = eq.qrCode,
            )
        }

        response.checklists.forEach { cl ->
            checklistStorage.upsertChecklist(
                id = cl.id,
                code = cl.code,
                name = cl.name,
                equipmentType = cl.equipmentType,
                description = cl.description,
            )
        }

        response.checklistItems.forEach { item ->
            checklistStorage.upsertItem(
                id = item.id,
                checklistId = item.checklistId,
                title = item.title,
                description = item.description,
                answerType = item.responseType,
                isRequired = if (item.isRequired) 1L else 0L,
                requiresPhoto = if (item.requirePhoto) 1L else 0L,
                selectOptions = item.optionsJson,
                numericMin = item.numericMin,
                numericMax = item.numericMax,
                orderIndex = item.orderIndex.toLong(),
            )
        }

        response.routes.forEach { route ->
            routeStorage.upsertRoute(
                id = route.id,
                code = route.code,
                name = route.name,
                description = route.description,
            )
        }

        response.routePoints.forEach { point ->
            val equipment = equipmentStorage.selectById(point.equipmentId)
            val checklist = equipment?.let {
                checklistStorage.selectChecklistByEquipmentType(it.type)
            }
            if (checklist == null) {
                Napier.w(
                    message = """
                        config-changes: no checklist for equipmentId=${point.equipmentId},
                        skipping route point ${point.id}
                    """.trimIndent()
                )
                return@forEach
            }
            routeStorage.upsertRoutePoint(
                id = point.id,
                routeId = point.routeId,
                equipmentId = point.equipmentId,
                checklistId = checklist.id,
                orderIndex = point.orderIndex.toLong(),
            )
        }

        response.assignments.forEach { assignment ->
            routeStorage.upsertAssignment(
                id = assignment.id,
                routeId = assignment.routeId,
                userId = assignment.userId,
                status = parseAssignmentStatus(assignment.status),
                assignedAt = assignment.assignmentDate,
                shiftCode = assignment.shiftCode,
                updatedAt = assignment.updatedAt,
            )
        }

        // Порядок применения сверху вниз по FK: inspections → IER → CIR → photos.
        response.inspections.forEach { inspection ->
            inspectionStorage.applyServerInspection(
                id = inspection.id,
                assignmentId = inspection.routeAssignmentId,
                routeId = inspection.routeId,
                status = inspection.status.toLocalInspectionStatus(),
                startedAt = inspection.startedAt,
                completedAt = inspection.completedAt,
                createdAt = inspection.createdAt,
                updatedAt = inspection.updatedAt,
            )
        }

        response.inspectionEquipmentResults.forEach { ier ->
            inspectionStorage.applyServerEquipmentResult(
                id = ier.id,
                inspectionId = ier.inspectionId,
                equipmentId = ier.equipmentId,
                routePointId = ier.routePointId,
                status = ier.status.toLocalEquipmentResultStatus(),
                startedAt = ier.startedAt,
                completedAt = ier.completedAt,
                createdAt = ier.createdAt,
                updatedAt = ier.updatedAt,
            )
        }

        response.checklistItemResults.forEach { cir ->
            inspectionStorage.applyServerChecklistItemResult(
                id = cir.id,
                inspectionEquipmentResultId = cir.inspectionEquipmentResultId,
                checklistItemId = cir.checklistItemId,
                valueText = cir.valueText,
                valueNumber = cir.valueNumber,
                valueBoolean = cir.valueBoolean?.let { if (it) 1L else 0L },
                selectedOption = cir.selectedOption,
                comment = cir.comment,
                createdAt = cir.createdAt,
                updatedAt = cir.updatedAt,
            )
        }

        response.photos.forEach { photo ->
            photoStorage.insertRestoredPhoto(
                id = photo.id,
                checklistItemResultId = photo.checklistItemResultId,
                takenAt = photo.createdAt,
                fileName = photo.fileName,
                mimeType = photo.mimeType,
                sizeBytes = photo.sizeBytes,
                checksum = photo.checksum,
            )
        }

        response.deletedIds.assignments.forEach { routeStorage.deleteAssignmentById(it) }
        response.deletedIds.routePoints.forEach { routeStorage.deleteRoutePointById(it) }
        response.deletedIds.routes.forEach { routeStorage.deleteRouteById(it) }
        response.deletedIds.equipment.forEach { equipmentStorage.deleteById(it) }
        response.deletedIds.locations.forEach { locationStorage.deleteById(it) }
        response.deletedIds.checklistItems.forEach { checklistStorage.deleteItemById(it) }
        response.deletedIds.checklists.forEach { checklistStorage.deleteChecklistById(it) }
    }

    private fun parseAssignmentStatus(raw: String): LocalRouteAssignmentStatus =
        LocalRouteAssignmentStatus.entries.firstOrNull { it.localValue == raw }
            ?: run {
                Napier.w("config-changes: unknown assignment status '$raw', defaulting to ASSIGNED")
                LocalRouteAssignmentStatus.ASSIGNED
            }

    /**
     * Неизвестная строка статуса → PLANNED с warning-логом: безопаснее вернуть как запланирован,
     * чем уронить delta. Дублирует mapper в `BootstrapRepositoryImpl` — три строки на модуль
     * дешевле кросс-модульной зависимости.
     */
    private fun String.toLocalInspectionStatus(): LocalInspectionStatus =
        LocalInspectionStatus.entries.firstOrNull { it.localValue == this }
            ?: run {
                Napier.w("config-changes: unknown InspectionStatus '$this', defaulting to PLANNED")
                LocalInspectionStatus.PLANNED
            }

    /** Same convention as [toLocalInspectionStatus]. */
    private fun String.toLocalEquipmentResultStatus(): LocalEquipmentResultStatus =
        LocalEquipmentResultStatus.entries.firstOrNull { it.localValue == this }
            ?: run {
                Napier.w(
                    "config-changes: unknown EquipmentResultStatus '$this', defaulting to NOT_STARTED"
                )
                LocalEquipmentResultStatus.NOT_STARTED
            }
}
