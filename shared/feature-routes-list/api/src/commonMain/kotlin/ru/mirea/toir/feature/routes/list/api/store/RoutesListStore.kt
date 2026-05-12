package ru.mirea.toir.feature.routes.list.api.store

import com.arkivanov.mvikotlin.core.store.Store
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.api.models.RoutesListSyncIndicator
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.Intent
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.Label
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.State

interface RoutesListStore : Store<Intent, State, Label> {

    data class State(
        val assignments: List<DomainRouteAssignment> = emptyList(),
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val syncIndicator: RoutesListSyncIndicator = RoutesListSyncIndicator(
            isRunning = false,
            hasPending = false,
            pendingInspections = emptyList(),
            lastError = null,
        ),
        val syncLastSuccessAt: String? = null,
        val syncLastFailedAt: String? = null,
        val isSyncSheetVisible: Boolean = false,
    )

    sealed interface Intent {
        data object Refresh : Intent
        data class OnStartInspection(val assignmentId: String) : Intent
        data class OnContinueInspection(val inspectionId: String) : Intent
        data object OnSyncIndicatorClicked : Intent
        data object OnSyncSheetDismissed : Intent
        data object OnSyncNowClicked : Intent
    }

    sealed interface Label {
        data class NavigateToRoutePoints(val inspectionId: String) : Label
    }
}
