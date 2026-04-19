package ru.mirea.toir.feature.routes.list.api.store

import com.arkivanov.mvikotlin.core.store.Store
import ru.mirea.toir.feature.routes.list.api.models.DomainRouteAssignment
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.Intent
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.Label
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.State

interface RoutesListStore : Store<Intent, State, Label> {

    data class State(
        val assignments: List<DomainRouteAssignment> = emptyList(),
        val isLoading: Boolean = true,
        val isError: Boolean = false,
    )

    sealed interface Intent {
        data object Refresh : Intent
        data class OnStartInspection(val assignmentId: String) : Intent
        data class OnContinueInspection(val inspectionId: String) : Intent
    }

    sealed interface Label {
        data class NavigateToRoutePoints(val inspectionId: String) : Label
    }
}
