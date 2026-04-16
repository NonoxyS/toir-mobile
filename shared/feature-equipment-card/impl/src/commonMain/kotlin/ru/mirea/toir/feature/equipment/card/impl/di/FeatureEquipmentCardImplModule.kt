package ru.mirea.toir.feature.equipment.card.impl.di

import org.koin.dsl.module
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.feature.equipment.card.api.store.EquipmentCardStore
import ru.mirea.toir.feature.equipment.card.impl.data.repository.EquipmentCardRepositoryImpl
import ru.mirea.toir.feature.equipment.card.impl.domain.EquipmentCardStoreFactory
import ru.mirea.toir.feature.equipment.card.impl.domain.repository.EquipmentCardRepository

val featureEquipmentCardImplModule = module {
    factory<EquipmentCardRepository> {
        EquipmentCardRepositoryImpl(
            inspectionStorage = get(),
            routeStorage = get(),
            equipmentStorage = get(),
            coroutineDispatchers = get(),
        )
    }

    factory<EquipmentCardStore> {
        EquipmentCardStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            repository = get(),
        ).create()
    }
}
