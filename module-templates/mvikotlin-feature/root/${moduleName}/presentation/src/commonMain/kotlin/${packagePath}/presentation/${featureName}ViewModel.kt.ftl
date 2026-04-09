package ${packageName}.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.mapNotNull
import dev.nonoxy.kmmtemplate.core.presentation.viewmodel.BaseViewModel
import ${packageName}.api.store.${featureName}Store
import ${packageName}.api.store.${featureName}Store.Intent
import ${packageName}.presentation.mappers.Ui${featureName}LabelMapper
import ${packageName}.presentation.mappers.Ui${featureName}StateMapper
import ${packageName}.presentation.models.Ui${featureName}Label
import ${packageName}.presentation.models.Ui${featureName}State

class ${featureName}ViewModel internal constructor(
    private val store: ${featureName}Store,
    private val stateMapper: Ui${featureName}StateMapper,
    private val labelMapper: Ui${featureName}LabelMapper,
) : BaseViewModel<Ui${featureName}State, Ui${featureName}Label>(initialState = Ui${featureName}State()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }

        loadData()
    }

    private fun loadData() = store.accept(Intent.LoadData)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
