package ru.mirea.toir.feature.checklist.ui.items

import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem

internal fun UiChecklistItem.titleWithRequiredMarker(): String =
    if (isRequired) "$title *" else title
