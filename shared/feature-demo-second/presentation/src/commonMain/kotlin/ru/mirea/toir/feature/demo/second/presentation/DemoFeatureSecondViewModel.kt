package ru.mirea.toir.feature.demo.second.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore
import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore.Intent
import ru.mirea.toir.feature.demo.second.presentation.mappers.UiDemoFeatureSecondLabelMapper
import ru.mirea.toir.feature.demo.second.presentation.mappers.UiDemoFeatureSecondStateMapper
import ru.mirea.toir.feature.demo.second.presentation.models.UiDemoFeatureSecondLabel
import ru.mirea.toir.feature.demo.second.presentation.models.UiDemoFeatureSecondState
import ru.mirea.toir.core.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.mapNotNull

class DemoFeatureSecondViewModel internal constructor(
    private val store: DemoFeatureSecondStore,
    private val stateMapper: UiDemoFeatureSecondStateMapper,
    private val labelMapper: UiDemoFeatureSecondLabelMapper,
) : BaseViewModel<UiDemoFeatureSecondState, UiDemoFeatureSecondLabel>(initialState = UiDemoFeatureSecondState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onRefreshClick() = store.accept(Intent.RefreshClick)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
