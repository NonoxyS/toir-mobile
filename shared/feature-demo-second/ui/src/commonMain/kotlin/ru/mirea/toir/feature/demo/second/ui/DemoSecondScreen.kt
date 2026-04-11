package ru.mirea.toir.feature.demo.second.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.feature.demo.second.presentation.DemoFeatureSecondViewModel
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DemoSecondScreen(
    onNavigateBack: () -> Unit,
    viewModel: DemoFeatureSecondViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(MR.strings.demo_second_title),
            style = ToirTheme.typography.displayLarge,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.isError -> Text(
                    text = stringResource(MR.strings.demo_second_error),
                    style = ToirTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                state.joke != null -> Text(
                    text = state.joke!!,
                    style = ToirTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = viewModel::onRefreshClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(MR.strings.demo_second_refresh_button))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(MR.strings.demo_second_back_button))
        }
    }
}
