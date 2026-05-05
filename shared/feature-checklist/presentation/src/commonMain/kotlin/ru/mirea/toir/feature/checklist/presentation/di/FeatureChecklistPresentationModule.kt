package ru.mirea.toir.feature.checklist.presentation.di

import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import ru.mirea.toir.feature.checklist.presentation.ChecklistViewModel
import ru.mirea.toir.feature.checklist.presentation.mappers.UiChecklistLabelMapper
import ru.mirea.toir.feature.checklist.presentation.mappers.UiChecklistLabelMapperImpl
import ru.mirea.toir.feature.checklist.presentation.mappers.UiChecklistStateMapper
import ru.mirea.toir.feature.checklist.presentation.mappers.UiChecklistStateMapperImpl

val featureChecklistPresentationModule = module {
    factory<UiChecklistStateMapper> { new(::UiChecklistStateMapperImpl) }
    factory<UiChecklistLabelMapper> { new(::UiChecklistLabelMapperImpl) }
    viewModel { params ->
        val equipmentResultId: String = params.get()
        ChecklistViewModel(
            store = get { parametersOf(equipmentResultId) },
            stateMapper = get(),
            labelMapper = get(),
        )
    }
}
