package ru.mirea.toir.feature.routes.list.presentation.models

import dev.icerock.moko.resources.StringResource
import ru.mirea.toir.res.MR

enum class UiRouteStatus(
    val stringResource: StringResource,
) {
    ASSIGNED(stringResource = MR.strings.routes_list_status_assigned,),
    IN_PROGRESS(stringResource = MR.strings.routes_list_status_in_progress),
    COMPLETED(stringResource = MR.strings.routes_list_status_completed),
}
