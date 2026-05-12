package ru.mirea.toir.feature.routes.list.presentation.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class UiRoutesListState(
    val assignments: ImmutableList<UiRouteAssignment> = persistentListOf(),
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val syncIndicator: UiSyncIndicator = UiSyncIndicator(),
    val syncLastSuccessAt: String? = null,
    val syncLastFailedAt: String? = null,
    val isSyncSheetVisible: Boolean = false,
)

data class UiSyncIndicator(
    val isRunning: Boolean = false,
    val pendingCount: Int = 0,
    val lastError: UiSyncFailure? = null,
)

enum class UiSyncFailure { NETWORK, AUTH, SERVER, UNKNOWN }
