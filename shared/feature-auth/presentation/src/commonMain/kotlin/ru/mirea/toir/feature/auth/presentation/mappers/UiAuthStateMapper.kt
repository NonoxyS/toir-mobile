package ru.mirea.toir.feature.auth.presentation.mappers

import ru.mirea.toir.feature.auth.api.store.AuthStore
import ru.mirea.toir.feature.auth.presentation.models.UiAuthState

interface UiAuthStateMapper {
    fun map(state: AuthStore.State): UiAuthState
}

internal class UiAuthStateMapperImpl : UiAuthStateMapper {
    override fun map(state: AuthStore.State): UiAuthState = with(state) {
        UiAuthState(
            login = login,
            password = password,
            isLoading = isLoading,
            errorMessage = errorMessage,
            passwordVisible = passwordVisible,
        )
    }
}
