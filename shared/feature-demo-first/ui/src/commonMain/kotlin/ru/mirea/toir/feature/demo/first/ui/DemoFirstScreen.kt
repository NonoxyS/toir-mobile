package ru.mirea.toir.feature.demo.first.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.feature.demo.first.presentation.DemoFeatureFirstViewModel
import ru.mirea.toir.feature.demo.first.presentation.models.UiDemoFeatureFirstLabel
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.res.MR
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DemoFirstScreen(
    onNavigateToSecond: () -> Unit,
    viewModel: DemoFeatureFirstViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiDemoFeatureFirstLabel.NavigateToSecondScreen -> onNavigateToSecond()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(MR.strings.demo_first_title),
            style = ToirTheme.typography.headlineMD,
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.numberValue,
            onValueChange = viewModel::onNumberValueChange,
            label = { Text(stringResource(MR.strings.demo_first_number_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = viewModel::onNextClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(MR.strings.demo_first_next_button))
        }
    }
}
