package ru.mirea.toir.feature.equipment.card.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.feature.equipment.card.presentation.EquipmentCardViewModel
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentCardLabel
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentCardState
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentResultStatus
import ru.mirea.toir.feature.equipment.card.ui.components.EquipmentCardContent
import ru.mirea.toir.feature.equipment.card.ui.components.EquipmentCardOpenChecklistButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EquipmentCardScreen(
    inspectionId: String,
    routePointId: String,
    onNavigateToChecklist: (equipmentResultId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: EquipmentCardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(inspectionId, routePointId) {
        viewModel.init(inspectionId, routePointId)
    }

    viewModel.label.CollectFlow { label ->
        when (label) {
            is UiEquipmentCardLabel.NavigateToChecklist ->
                onNavigateToChecklist(label.equipmentResultId)
        }
    }

    Scaffold(
        containerColor = ToirTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.name.ifEmpty { state.code },
                        style = ToirTheme.typography.headline,
                        color = ToirTheme.colors.textPrimary,
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(
                            text = "←",
                            style = ToirTheme.typography.bodyLarge,
                            color = ToirTheme.colors.textSecondary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ToirTheme.colors.background,
                ),
            )
        },
        bottomBar = {
            if (!state.isLoading && state.equipmentResultId != null) {
                EquipmentCardOpenChecklistButton(onClick = viewModel::onOpenChecklist)
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    color = ToirTheme.colors.textSecondary,
                )

                state.isError -> Text(
                    text = stringResource(MR.strings.error_generic),
                    style = ToirTheme.typography.bodyMedium,
                    color = ToirTheme.colors.error,
                )

                else -> EquipmentCardContent(
                    state = state,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewEquipmentCardScreenLoading() {
    ToirTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = ToirTheme.colors.textSecondary)
        }
    }
}

@Preview
@Composable
private fun PreviewEquipmentCardScreenContent() {
    ToirTheme {
        EquipmentCardContent(
            state = UiEquipmentCardState(
                code = "EQ-001",
                name = "Насос циркуляционный",
                type = "Насос",
                locationName = "Котельная, 2 этаж",
                status = UiEquipmentResultStatus.IN_PROGRESS,
                equipmentResultId = "res-001",
                isLoading = false,
                isError = false,
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        )
    }
}
