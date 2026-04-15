package ru.mirea.toir.feature.routes.list.presentation.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class UiRoutesListState(
    val assignments: ImmutableList<UiRouteAssignment> = persistentListOf(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
