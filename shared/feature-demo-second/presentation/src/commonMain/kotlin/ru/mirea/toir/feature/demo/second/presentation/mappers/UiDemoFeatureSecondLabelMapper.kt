package ru.mirea.toir.feature.demo.second.presentation.mappers

import ru.mirea.toir.feature.demo.second.api.store.DemoFeatureSecondStore
import ru.mirea.toir.feature.demo.second.presentation.models.UiDemoFeatureSecondLabel
import ru.mirea.toir.common.mappers.Mapper

interface UiDemoFeatureSecondLabelMapper : Mapper<DemoFeatureSecondStore.Label, UiDemoFeatureSecondLabel>

internal class UiDemoFeatureSecondLabelMapperImpl : UiDemoFeatureSecondLabelMapper {

    override fun map(item: DemoFeatureSecondStore.Label): UiDemoFeatureSecondLabel = when (item) {
        else -> error("Unexpected label: $item")
    }
}
