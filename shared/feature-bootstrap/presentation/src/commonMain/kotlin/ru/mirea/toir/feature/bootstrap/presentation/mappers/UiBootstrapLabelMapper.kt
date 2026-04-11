package ru.mirea.toir.feature.bootstrap.presentation.mappers

import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapLabel

interface UiBootstrapLabelMapper : Mapper<BootstrapStore.Label, UiBootstrapLabel>

internal class UiBootstrapLabelMapperImpl : UiBootstrapLabelMapper {
    override fun map(item: BootstrapStore.Label): UiBootstrapLabel = when (item) {
        BootstrapStore.Label.NavigateToRoutesList -> UiBootstrapLabel.NavigateToRoutesList
        BootstrapStore.Label.NavigateToLogin -> UiBootstrapLabel.NavigateToLogin
    }
}
