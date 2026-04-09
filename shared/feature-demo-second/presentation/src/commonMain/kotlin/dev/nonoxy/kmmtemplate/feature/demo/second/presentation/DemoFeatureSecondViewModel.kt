package dev.nonoxy.kmmtemplate.feature.demo.second.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import dev.nonoxy.kmmtemplate.feature.demo.second.api.store.DemoFeatureSecondStore
import dev.nonoxy.kmmtemplate.feature.demo.second.api.store.DemoFeatureSecondStore.Intent
import dev.nonoxy.kmmtemplate.feature.demo.second.presentation.mappers.UiDemoFeatureSecondLabelMapper
import dev.nonoxy.kmmtemplate.feature.demo.second.presentation.mappers.UiDemoFeatureSecondStateMapper
import dev.nonoxy.kmmtemplate.feature.demo.second.presentation.models.UiDemoFeatureSecondLabel
import dev.nonoxy.kmmtemplate.feature.demo.second.presentation.models.UiDemoFeatureSecondState
import dev.nonoxy.kmmtemplate.core.presentation.viewmodel.BaseViewModel
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
