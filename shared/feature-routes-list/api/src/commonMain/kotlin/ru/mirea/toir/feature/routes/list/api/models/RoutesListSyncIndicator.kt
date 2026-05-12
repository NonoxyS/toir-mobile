package ru.mirea.toir.feature.routes.list.api.models

data class RoutesListSyncIndicator(
    val isRunning: Boolean,
    val pendingCount: Int,
    val lastError: RoutesListSyncFailure?,
)

enum class RoutesListSyncFailure { NETWORK, AUTH, SERVER, UNKNOWN }
