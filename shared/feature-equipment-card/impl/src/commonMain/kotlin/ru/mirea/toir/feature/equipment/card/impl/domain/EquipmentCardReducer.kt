package ru.mirea.toir.feature.equipment.card.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore
import ru.mirea.toir.feature.equipment.card.impl.domain.EquipmentCardStoreFactory.Message

internal class EquipmentCardReducer : Reducer<EquipmentCardStore.State, Message> {
    override fun EquipmentCardStore.State.reduce(msg: Message): EquipmentCardStore.State = when (msg) {
        Message.SetLoading -> copy(isLoading = true, errorMessage = null)
        is Message.SetError -> copy(isLoading = false, errorMessage = msg.message)
        is Message.SetCard -> copy(isLoading = false, card = msg.card)
    }
}
