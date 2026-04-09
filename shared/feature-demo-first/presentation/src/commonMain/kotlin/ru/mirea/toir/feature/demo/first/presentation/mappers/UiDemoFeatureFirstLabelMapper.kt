package ru.mirea.toir.feature.demo.first.presentation.mappers

import ru.mirea.toir.feature.demo.first.api.store.DemoFeatureFirstStore
import ru.mirea.toir.feature.demo.first.presentation.models.UiDemoFeatureFirstLabel
import ru.mirea.toir.common.mappers.Mapper

interface UiDemoFeatureFirstLabelMapper : Mapper<DemoFeatureFirstStore.Label, UiDemoFeatureFirstLabel>

internal class UiDemoFeatureFirstLabelMapperImpl : UiDemoFeatureFirstLabelMapper {

    override fun map(item: DemoFeatureFirstStore.Label): UiDemoFeatureFirstLabel = when (item) {
        DemoFeatureFirstStore.Label.NavigateToSecondScreen -> UiDemoFeatureFirstLabel.NavigateToSecondScreen
    }
}
