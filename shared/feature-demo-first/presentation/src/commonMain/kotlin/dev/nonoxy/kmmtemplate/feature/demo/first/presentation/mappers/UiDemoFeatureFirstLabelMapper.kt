package dev.nonoxy.kmmtemplate.feature.demo.first.presentation.mappers

import dev.nonoxy.kmmtemplate.feature.demo.first.api.store.DemoFeatureFirstStore
import dev.nonoxy.kmmtemplate.feature.demo.first.presentation.models.UiDemoFeatureFirstLabel
import dev.nonoxy.kmmtemplate.common.mappers.Mapper

interface UiDemoFeatureFirstLabelMapper : Mapper<DemoFeatureFirstStore.Label, UiDemoFeatureFirstLabel>

internal class UiDemoFeatureFirstLabelMapperImpl : UiDemoFeatureFirstLabelMapper {

    override fun map(item: DemoFeatureFirstStore.Label): UiDemoFeatureFirstLabel = when (item) {
        DemoFeatureFirstStore.Label.NavigateToSecondScreen -> UiDemoFeatureFirstLabel.NavigateToSecondScreen
    }
}
