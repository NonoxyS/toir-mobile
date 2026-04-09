package ru.mirea.toir.feature.demo.second.presentation.mappers

import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore
import ru.mirea.toir.feature.demo.second.presentation.models.UiDemoFeatureSecondState
import ru.mirea.toir.common.mappers.Mapper

interface UiDemoFeatureSecondStateMapper : Mapper<DemoFeatureSecondStore.State, UiDemoFeatureSecondState>

internal class UiDemoFeatureSecondStateMapperImpl : UiDemoFeatureSecondStateMapper {

    override fun map(item: DemoFeatureSecondStore.State): UiDemoFeatureSecondState = with(item) {
        UiDemoFeatureSecondState(
            joke = joke,
            isLoading = isLoading,
            isError = isError,
        )
    }
}
