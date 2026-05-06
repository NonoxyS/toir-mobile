package ru.mirea.toir.feature.bootstrap.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.components.shared.button.ToirPrimaryButton
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.feature.bootstrap.presentation.BootstrapViewModel
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapLabel
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapState
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

    BootstrapContent(state = state, onRetry = viewModel::onRetry)
}

@Composable
private fun BootstrapContent(
    state: UiBootstrapState,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ToirTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp)) // spacing.xxl от safe area
        BootstrapHeader()
        Spacer(Modifier.height(48.dp)) // spacing.xxl до состояния
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Crossfade(
                targetState = state,
                animationSpec = tween(durationMillis = 200),
                label = "bootstrap-state-crossfade",
            ) { current ->
                when {
                    current.isError -> BootstrapError(onRetry = onRetry)
                    current.isLoading -> BootstrapLoading()
                    else -> Spacer(Modifier.size(0.dp)) // success: no UI
                }
            }
        }
    }
}

@Composable
private fun BootstrapHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(MR.strings.bootstrap_title),
            style = ToirTheme.typography.displayLarge,
            color = ToirTheme.colors.textPrimary,
        )
        Text(
            text = stringResource(MR.strings.bootstrap_subtitle),
            style = ToirTheme.typography.bodyMedium,
            color = ToirTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun BootstrapLoading() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = ToirTheme.colors.textSecondary,
        )
        Text(
            text = stringResource(MR.strings.bootstrap_loading),
            style = ToirTheme.typography.caption,
            color = ToirTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun BootstrapError(onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(MR.images.ic_cloud_off),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                colorFilter = ColorFilter.tint(ToirTheme.colors.error),
            )
            Text(
                text = stringResource(MR.strings.bootstrap_error_title),
                style = ToirTheme.typography.bodyLarge,
                color = ToirTheme.colors.textPrimary,
            )
            Text(
                text = stringResource(MR.strings.bootstrap_error_subtitle),
                style = ToirTheme.typography.bodyMedium,
                color = ToirTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        ToirPrimaryButton(
            onClick = onRetry,
            text = stringResource(MR.strings.bootstrap_button_retry),
            modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun PreviewBootstrapLoading() {
    ToirTheme {
        BootstrapContent(state = UiBootstrapState(isLoading = true), onRetry = {})
    }
}

@Preview
@Composable
private fun PreviewBootstrapError() {
    ToirTheme {
        BootstrapContent(
            state = UiBootstrapState(isLoading = false, isError = true),
            onRetry = {},
        )
    }
}
