package ru.mirea.toir.feature.routes.list.impl.domain

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
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
    private var subscriptionJob: Job? = null

    override suspend fun suspendExecuteAction(action: Unit) {
        subscribeToAssignments()
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            Intent.Refresh -> subscribeToAssignments()
            is Intent.OnStartInspection -> startInspection(intent.assignmentId)
            is Intent.OnContinueInspection -> publish(
                Label.NavigateToRoutePoints(intent.inspectionId)
            )
        }
    }

    private fun subscribeToAssignments() {
        subscriptionJob?.cancel()
        subscriptionJob = repository.observeAssignments()
            .onStart { dispatch(Message.SetLoading) }
            .onEach { dispatch(Message.SetAssignments(it)) }
            .catch { throwable ->
                Napier.e(message = "observeAssignments failed", throwable = throwable)
                dispatch(Message.SetError)
            }
            .launchIn(scope)
    }

    private suspend fun startInspection(assignmentId: String) {
        repository.startInspection(assignmentId).fold(
            onSuccess = { inspectionId ->
                publish(Label.NavigateToRoutePoints(inspectionId))
            },
            onFailure = { throwable ->
                Napier.e(message = "startInspection failed", throwable = throwable)
                dispatch(Message.SetError)
            },
        )
    }
}
