package ru.mirea.toir.feature.bootstrap.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.feature.bootstrap.presentation.BootstrapViewModel
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapLabel
import ru.mirea.toir.res.MR

@Composable
internal fun BootstrapScreen(
    onNavigateToRoutesList: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: BootstrapViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiBootstrapLabel.NavigateToRoutesList -> onNavigateToRoutesList()
            UiBootstrapLabel.NavigateToLogin -> onNavigateToLogin()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.errorMessage != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(MR.strings.bootstrap_error_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.errorMessage.orEmpty())
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = viewModel::onRetry) {
                    Text(text = stringResource(MR.strings.bootstrap_button_retry))
                }
            }
        }
    }
}
