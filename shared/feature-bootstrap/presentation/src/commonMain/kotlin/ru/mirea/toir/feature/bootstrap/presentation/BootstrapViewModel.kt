package ru.mirea.toir.feature.bootstrap.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.mapNotNull
import ru.mirea.toir.core.presentation.viewmodel.BaseViewModel
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import ru.mirea.toir.feature.bootstrap.presentation.mappers.UiBootstrapLabelMapper
import ru.mirea.toir.feature.bootstrap.presentation.mappers.UiBootstrapStateMapper
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapLabel
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapState

class BootstrapViewModel internal constructor(
    private val store: BootstrapStore,
    private val stateMapper: UiBootstrapStateMapper,
    private val labelMapper: UiBootstrapLabelMapper,
) : BaseViewModel<UiBootstrapState, UiBootstrapLabel>(initialState = UiBootstrapState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onRetry() = store.accept(BootstrapStore.Intent.Retry)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
