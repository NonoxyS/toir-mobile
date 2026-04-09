package dev.nonoxy.kmmtemplate.feature.demo.first.presentation.mappers

import dev.nonoxy.kmmtemplate.feature.demo.first.api.store.DemoFeatureFirstStore
import dev.nonoxy.kmmtemplate.feature.demo.first.presentation.models.UiDemoFeatureFirstState
import dev.nonoxy.kmmtemplate.common.mappers.Mapper

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
