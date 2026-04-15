package ru.mirea.toir.feature.route.points.presentation.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class UiRoutePointsState(
    val routeName: String = "",
    val points: ImmutableList<UiRoutePoint> = persistentListOf(),
    val isLoading: Boolean = true,
    val canFinish: Boolean = false,
    val errorMessage: String? = null,
)
