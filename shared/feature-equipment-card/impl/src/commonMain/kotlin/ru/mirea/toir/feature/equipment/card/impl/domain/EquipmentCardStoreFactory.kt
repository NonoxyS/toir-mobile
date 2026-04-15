package ru.mirea.toir.feature.equipment.card.impl.domain

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.equipment.card.api.models.DomainEquipmentCard
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore.Intent
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore.Label
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore.State
import ru.mirea.toir.feature.equipment.card.impl.domain.repository.EquipmentCardRepository

internal class EquipmentCardStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: EquipmentCardRepository,
    private val mainDispatcher: CoroutineDispatcher,
) {
    fun create(): EquipmentCardStore =
        object :
            EquipmentCardStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = EquipmentCardStore::class.simpleName,
                initialState = State(),
                bootstrapper = null,
                executorFactory = { EquipmentCardExecutor(repository, mainDispatcher) },
                reducer = EquipmentCardReducer(),
            ) {}

    internal sealed interface Message {
        data object SetLoading : Message
        data class SetError(val message: String) : Message
        data class SetCard(val card: DomainEquipmentCard) : Message
    }
}
