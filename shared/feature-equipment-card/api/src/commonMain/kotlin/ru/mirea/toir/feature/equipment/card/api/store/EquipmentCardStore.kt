package ru.mirea.toir.feature.equipment.card.api.store

import com.arkivanov.mvikotlin.core.store.Store
import ru.mirea.toir.feature.equipment.card.api.models.DomainEquipmentCard

interface EquipmentCardStore : Store<EquipmentCardStore.Intent, EquipmentCardStore.State, EquipmentCardStore.Label> {

    data class State(
        val card: DomainEquipmentCard? = null,
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data class Init(val inspectionId: String, val routePointId: String) : Intent
        data object OnOpenChecklist : Intent
    }

    sealed interface Label {
        data class NavigateToChecklist(val equipmentResultId: String) : Label
    }
}
