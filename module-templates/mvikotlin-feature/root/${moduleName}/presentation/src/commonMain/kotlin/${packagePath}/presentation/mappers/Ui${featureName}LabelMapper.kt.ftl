package ${packageName}.presentation.mappers

import ${packageName}.api.store.${featureName}Store
import ${packageName}.presentation.models.Ui${featureName}Label
import ru.mirea.toir.common.mappers.Mapper

interface Ui${featureName}LabelMapper : Mapper<${featureName}Store.Label, Ui${featureName}Label>

internal class Ui${featureName}LabelMapperImpl : Ui${featureName}LabelMapper {

    override fun map(item: ${featureName}Store.Label): Ui${featureName}Label = when (item) {
        else -> TODO() // Add your label mappings here
    }
}
