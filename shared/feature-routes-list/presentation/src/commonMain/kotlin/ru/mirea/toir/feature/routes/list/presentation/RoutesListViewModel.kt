package ru.mirea.toir.feature.routes.list.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.mapNotNull
import ru.mirea.toir.core.presentation.viewmodel.BaseViewModel
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import ru.mirea.toir.feature.routes.list.presentation.mappers.UiRoutesListLabelMapper
import ru.mirea.toir.feature.routes.list.presentation.mappers.UiRoutesListStateMapper
import ru.mirea.toir.feature.routes.list.presentation.models.UiRoutesListLabel
import ru.mirea.toir.feature.routes.list.presentation.models.UiRoutesListState

class RoutesListViewModel internal constructor(
    private val store: RoutesListStore,
    private val stateMapper: UiRoutesListStateMapper,
    private val labelMapper: UiRoutesListLabelMapper,
) : BaseViewModel<UiRoutesListState, UiRoutesListLabel>(initialState = UiRoutesListState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onRefresh() = store.accept(RoutesListStore.Intent.Refresh)
    fun onStartInspection(assignmentId: String) = store.accept(RoutesListStore.Intent.OnStartInspection(assignmentId))
    fun onContinueInspection(inspectionId: String) = store.accept(RoutesListStore.Intent.OnContinueInspection(inspectionId))

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
