package ${packageName}.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ${packageName}.presentation.${featureName}ViewModel
import ${packageName}.presentation.models.Ui${featureName}Label
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ${featureName}Screen(
    // TODO: add navigation callbacks
    viewModel: ${featureName}ViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            // TODO: handle labels
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // TODO: implement UI
    }
}
