package dev.nonoxy.kmmtemplate.feature.demo.second.presentation.mappers

import dev.nonoxy.kmmtemplate.feature.demo.second.api.store.DemoFeatureSecondStore
import dev.nonoxy.kmmtemplate.feature.demo.second.presentation.models.UiDemoFeatureSecondState
import dev.nonoxy.kmmtemplate.common.mappers.Mapper

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
