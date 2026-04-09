package ${packageName}.presentation.mappers

import ${packageName}.api.store.${featureName}Store
import ${packageName}.presentation.models.Ui${featureName}State
import dev.nonoxy.kmmtemplate.common.mappers.Mapper

interface Ui${featureName}StateMapper : Mapper<${featureName}Store.State, Ui${featureName}State>

internal class Ui${featureName}StateMapperImpl : Ui${featureName}StateMapper {

    override fun map(item: ${featureName}Store.State): Ui${featureName}State = with(item) {
        Ui${featureName}State(
            isLoading = isLoading,
            isError = isError,
        )
    }
}
