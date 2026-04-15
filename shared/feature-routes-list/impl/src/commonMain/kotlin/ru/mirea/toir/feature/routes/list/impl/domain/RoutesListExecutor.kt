package ru.mirea.toir.feature.routes.list.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.Intent
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.Label
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore.State
import ru.mirea.toir.feature.routes.list.impl.domain.RoutesListStoreFactory.Message
import ru.mirea.toir.feature.routes.list.impl.domain.repository.RoutesListRepository

internal class RoutesListExecutor(
    private val repository: RoutesListRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<Intent, Unit, State, Message, Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteAction(action: Unit) {
        loadAssignments()
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            Intent.Refresh -> loadAssignments()
            is Intent.OnStartInspection -> startInspection(intent.assignmentId)
            is Intent.OnContinueInspection -> publish(
                Label.NavigateToRoutePoints(intent.inspectionId)
            )
        }
    }

    private suspend fun loadAssignments() {
        dispatch(Message.SetLoading)
        repository.getAssignments().fold(
            onSuccess = { list ->
                dispatch(Message.SetAssignments(list))
            },
            onFailure = { throwable ->
                dispatch(Message.SetError(throwable.message ?: "Ошибка загрузки"))
            },
        )
    }

    private suspend fun startInspection(assignmentId: String) {
        repository.startInspection(assignmentId).fold(
            onSuccess = { inspectionId ->
                publish(Label.NavigateToRoutePoints(inspectionId))
            },
            onFailure = { throwable ->
                dispatch(Message.SetError(throwable.message ?: "Ошибка старта обхода"))
            },
        )
    }
}
