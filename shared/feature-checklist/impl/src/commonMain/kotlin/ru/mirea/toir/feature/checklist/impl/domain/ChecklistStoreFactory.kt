package ru.mirea.toir.feature.checklist.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore.Intent
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore.Label
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore.State
import ru.mirea.toir.feature.checklist.impl.domain.repository.ChecklistRepository

internal class ChecklistStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: ChecklistRepository,
    private val mainDispatcher: CoroutineDispatcher,
    private val equipmentResultId: String,
) {
    fun create(): ChecklistStore =
        object :
            ChecklistStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = ChecklistStore::class.simpleName,
                initialState = State(equipmentResultId = equipmentResultId),
                bootstrapper = SimpleBootstrapper(Action.Load),
                executorFactory = { ChecklistExecutor(repository, mainDispatcher) },
                reducer = ChecklistReducer(),
            ) {}

    internal sealed interface Action {
        data object Load : Action
    }

    internal sealed interface Message {
        data object SetLoading : Message
        data object SetError : Message
        data class SetItems(val items: ImmutableList<DomainChecklistItem>) : Message
        data class UpdateItem(val item: DomainChecklistItem) : Message
        data class SetNumberDraft(val itemId: String, val raw: String) : Message
        data object SetValidationRequiredError : Message
        data object SetValidationPhotoError : Message
        data object ClearValidationError : Message
        data object SetCompleted : Message
    }
}
