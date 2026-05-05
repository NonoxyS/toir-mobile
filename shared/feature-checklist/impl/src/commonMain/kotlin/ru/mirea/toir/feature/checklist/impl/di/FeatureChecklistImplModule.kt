package ru.mirea.toir.feature.checklist.impl.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore
import ru.mirea.toir.feature.checklist.impl.data.repository.ChecklistRepositoryImpl
import ru.mirea.toir.feature.checklist.impl.domain.ChecklistStoreFactory
import ru.mirea.toir.feature.checklist.impl.domain.repository.ChecklistRepository

val featureChecklistImplModule = module {
    factory<ChecklistRepository> { new(::ChecklistRepositoryImpl) }

    factory<ChecklistStore> { params ->
        ChecklistStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            repository = get(),
            equipmentResultId = params.get(),
        ).create()
    }
}
