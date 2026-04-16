package ru.mirea.toir.feature.route.points.presentation.models

import dev.icerock.moko.resources.StringResource
import ru.mirea.toir.res.MR

enum class UiEquipmentResultStatus(val labelRes: StringResource) {
    NOT_STARTED(MR.strings.equipment_result_status_not_started),
    IN_PROGRESS(MR.strings.equipment_result_status_in_progress),
    COMPLETED(MR.strings.equipment_result_status_completed),
    SKIPPED(MR.strings.equipment_result_status_skipped),
}
