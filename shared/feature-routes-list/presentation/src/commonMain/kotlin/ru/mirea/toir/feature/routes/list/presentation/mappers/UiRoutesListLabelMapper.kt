package ru.mirea.toir.feature.routes.list.presentation.mappers

import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import ru.mirea.toir.feature.routes.list.presentation.models.UiRoutesListLabel

interface UiRoutesListLabelMapper : Mapper<RoutesListStore.Label, UiRoutesListLabel>

internal class UiRoutesListLabelMapperImpl : UiRoutesListLabelMapper {
    override fun map(item: RoutesListStore.Label): UiRoutesListLabel = when (item) {
        is RoutesListStore.Label.NavigateToRoutePoints ->
            UiRoutesListLabel.NavigateToRoutePoints(item.inspectionId)
    }
}
