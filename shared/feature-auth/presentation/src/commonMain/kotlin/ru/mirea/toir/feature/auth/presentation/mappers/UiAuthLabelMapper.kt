package ru.mirea.toir.feature.auth.presentation.mappers

import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.auth.api.store.AuthStore
import ru.mirea.toir.feature.auth.presentation.models.UiAuthLabel

interface UiAuthLabelMapper : Mapper<AuthStore.Label, UiAuthLabel>

internal class UiAuthLabelMapperImpl : UiAuthLabelMapper {
    override fun map(item: AuthStore.Label): UiAuthLabel = when (item) {
        AuthStore.Label.NavigateToMain -> UiAuthLabel.NavigateToMain
    }
}
