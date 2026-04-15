package ru.mirea.toir.feature.equipment.card.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.mapNotNull
import ru.mirea.toir.core.presentation.viewmodel.BaseViewModel
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore.Intent
import ru.mirea.toir.feature.equipment.card.presentation.mappers.UiEquipmentCardLabelMapper
import ru.mirea.toir.feature.equipment.card.presentation.mappers.UiEquipmentCardStateMapper
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentCardLabel
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentCardState

class EquipmentCardViewModel internal constructor(
    private val store: EquipmentCardStore,
    stateMapper: UiEquipmentCardStateMapper,
    labelMapper: UiEquipmentCardLabelMapper,
) : BaseViewModel<UiEquipmentCardState, UiEquipmentCardLabel>(initialState = UiEquipmentCardState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun init(inspectionId: String, routePointId: String) =
        store.accept(Intent.Init(inspectionId, routePointId))

    fun onOpenChecklist() = store.accept(Intent.OnOpenChecklist)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
