package ru.mirea.toir.feature.routes.list.api.models

data class RoutesListSyncIndicator(
    val isRunning: Boolean,
    val hasPending: Boolean,
    val pendingInspections: List<RoutesListPendingInspection>,
    val lastError: RoutesListSyncFailure?,
)

enum class RoutesListSyncFailure { NETWORK, AUTH, SERVER, UNKNOWN }
