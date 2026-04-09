package dev.nonoxy.kmmtemplate.feature.demo.first.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import dev.nonoxy.kmmtemplate.feature.demo.first.api.store.DemoFeatureFirstStore
import dev.nonoxy.kmmtemplate.feature.demo.first.api.store.DemoFeatureFirstStore.Intent
import dev.nonoxy.kmmtemplate.feature.demo.first.presentation.mappers.UiDemoFeatureFirstLabelMapper
import dev.nonoxy.kmmtemplate.feature.demo.first.presentation.mappers.UiDemoFeatureFirstStateMapper
import dev.nonoxy.kmmtemplate.feature.demo.first.presentation.models.UiDemoFeatureFirstLabel
import dev.nonoxy.kmmtemplate.feature.demo.first.presentation.models.UiDemoFeatureFirstState
import dev.nonoxy.kmmtemplate.core.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.mapNotNull

class DemoFeatureFirstViewModel internal constructor(
    private val store: DemoFeatureFirstStore,
    private val stateMapper: UiDemoFeatureFirstStateMapper,
    private val labelMapper: UiDemoFeatureFirstLabelMapper,
) : BaseViewModel<UiDemoFeatureFirstState, UiDemoFeatureFirstLabel>(initialState = UiDemoFeatureFirstState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onNumberValueChange(value: String) = store.accept(Intent.OnNumberValueChange(value))

    fun onNextClick() = store.accept(Intent.NextButtonClick)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
