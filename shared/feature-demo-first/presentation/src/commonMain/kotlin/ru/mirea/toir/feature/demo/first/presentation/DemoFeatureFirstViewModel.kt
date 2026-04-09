package ru.mirea.toir.feature.demo.first.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import ru.mirea.toir.feature.demo.first.api.store.DemoFeatureFirstStore
import ru.mirea.toir.feature.demo.first.api.store.DemoFeatureFirstStore.Intent
import ru.mirea.toir.feature.demo.first.presentation.mappers.UiDemoFeatureFirstLabelMapper
import ru.mirea.toir.feature.demo.first.presentation.mappers.UiDemoFeatureFirstStateMapper
import ru.mirea.toir.feature.demo.first.presentation.models.UiDemoFeatureFirstLabel
import ru.mirea.toir.feature.demo.first.presentation.models.UiDemoFeatureFirstState
import ru.mirea.toir.core.presentation.viewmodel.BaseViewModel
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
