package ru.mirea.toir.feature.equipment.card.presentation.di

import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.mirea.toir.feature.equipment.card.presentation.EquipmentCardViewModel
import ru.mirea.toir.feature.equipment.card.presentation.mappers.UiEquipmentCardLabelMapper
import ru.mirea.toir.feature.equipment.card.presentation.mappers.UiEquipmentCardLabelMapperImpl
import ru.mirea.toir.feature.equipment.card.presentation.mappers.UiEquipmentCardStateMapper
import ru.mirea.toir.feature.equipment.card.presentation.mappers.UiEquipmentCardStateMapperImpl

val featureEquipmentCardPresentationModule = module {
    factory<UiEquipmentCardStateMapper> { new(::UiEquipmentCardStateMapperImpl) }
    factory<UiEquipmentCardLabelMapper> { new(::UiEquipmentCardLabelMapperImpl) }
    viewModelOf(::EquipmentCardViewModel)
}
