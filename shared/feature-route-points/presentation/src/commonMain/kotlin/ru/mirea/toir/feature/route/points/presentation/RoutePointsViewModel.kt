package ru.mirea.toir.feature.route.points.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.mapNotNull
import ru.mirea.toir.core.presentation.viewmodel.BaseViewModel
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore
import ru.mirea.toir.feature.route.points.api.store.RoutePointsStore.Intent
import ru.mirea.toir.feature.route.points.presentation.mappers.UiRoutePointsLabelMapper
import ru.mirea.toir.feature.route.points.presentation.mappers.UiRoutePointsStateMapper
import ru.mirea.toir.feature.route.points.presentation.models.UiRoutePointsLabel
import ru.mirea.toir.feature.route.points.presentation.models.UiRoutePointsState

class RoutePointsViewModel internal constructor(
    private val store: RoutePointsStore,
    stateMapper: UiRoutePointsStateMapper,
    labelMapper: UiRoutePointsLabelMapper,
) : BaseViewModel<UiRoutePointsState, UiRoutePointsLabel>(initialState = UiRoutePointsState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun init(inspectionId: String) = store.accept(Intent.Init(inspectionId))
    fun onPointClick(routePointId: String) = store.accept(Intent.OnPointClick(routePointId))
    fun onFinishInspection() = store.accept(Intent.OnFinishInspection)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
