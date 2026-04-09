package dev.nonoxy.kmmtemplate.feature.demo.second.presentation.mappers

import dev.nonoxy.kmmtemplate.feature.demo.second.api.store.DemoFeatureSecondStore
import dev.nonoxy.kmmtemplate.feature.demo.second.presentation.models.UiDemoFeatureSecondLabel
import dev.nonoxy.kmmtemplate.common.mappers.Mapper

interface UiDemoFeatureSecondLabelMapper : Mapper<DemoFeatureSecondStore.Label, UiDemoFeatureSecondLabel>

internal class UiDemoFeatureSecondLabelMapperImpl : UiDemoFeatureSecondLabelMapper {

    override fun map(item: DemoFeatureSecondStore.Label): UiDemoFeatureSecondLabel = when (item) {
        else -> error("Unexpected label: $item")
    }
}
