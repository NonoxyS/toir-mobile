package ru.mirea.toir.core.database.storage.inspection.models

import ru.mirea.toir.core.database.models.LocalEnum

enum class LocalEquipmentResultStatus(
    override val localValue: String
) : LocalEnum {

    NOT_STARTED("not_started"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    SKIPPED("skipped");
}
