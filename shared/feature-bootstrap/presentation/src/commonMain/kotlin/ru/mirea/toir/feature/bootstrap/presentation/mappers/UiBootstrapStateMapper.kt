package ru.mirea.toir.feature.bootstrap.presentation.mappers

import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapState

interface UiBootstrapStateMapper {
    fun map(state: BootstrapStore.State): UiBootstrapState
}

internal class UiBootstrapStateMapperImpl : UiBootstrapStateMapper {
    override fun map(state: BootstrapStore.State): UiBootstrapState = with(state) {
        UiBootstrapState(isLoading = isLoading, errorMessage = errorMessage)
    }
}
