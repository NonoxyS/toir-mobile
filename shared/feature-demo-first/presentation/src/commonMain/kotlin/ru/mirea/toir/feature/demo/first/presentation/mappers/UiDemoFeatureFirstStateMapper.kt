package ru.mirea.toir.feature.demo.first.presentation.mappers

import ru.mirea.toir.feature.demo.first.api.store.DemoFeatureFirstStore
import ru.mirea.toir.feature.demo.first.presentation.models.UiDemoFeatureFirstState
import ru.mirea.toir.common.mappers.Mapper

interface UiDemoFeatureFirstStateMapper : Mapper<DemoFeatureFirstStore.State, UiDemoFeatureFirstState>

internal class UiDemoFeatureFirstStateMapperImpl : UiDemoFeatureFirstStateMapper {

    override fun map(item: DemoFeatureFirstStore.State): UiDemoFeatureFirstState = with(item) {
        UiDemoFeatureFirstState(
            numberValue = numberValue,
            isLoading = isLoading,
            isError = isError,
        )
    }
}
