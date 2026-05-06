package ru.mirea.toir.feature.equipment.card.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.feature.equipment.card.presentation.EquipmentCardViewModel
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentCardLabel
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentCardState
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentResultStatus
import ru.mirea.toir.feature.equipment.card.ui.components.EquipmentCardContent
import ru.mirea.toir.feature.equipment.card.ui.components.EquipmentCardLayout
import ru.mirea.toir.feature.equipment.card.ui.components.EquipmentCardOpenChecklistButton
import ru.mirea.toir.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EquipmentCardScreen(
    inspectionId: String,
    routePointId: String,
    onNavigateToChecklist: (equipmentResultId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: EquipmentCardViewModel = koinViewModel { parametersOf(inspectionId, routePointId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                    IconButton(onClick = onNavigateBack) {
                        Image(
                            painter = painterResource(MR.images.ic_arrow_back),
                            contentDescription = stringResource(
                                MR.strings.equipment_card_back_content_description,
                            ),
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(ToirTheme.colors.textSecondary),
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
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ToirTheme.colors.textSecondary)
            }

            state.isError -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(MR.strings.error_generic),
                    style = ToirTheme.typography.bodyMedium,
                    color = ToirTheme.colors.error,
                )
            }

            else -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                EquipmentCardLayout(
                    status = state.status,
                    modifier = Modifier.widthIn(max = 480.dp),
                ) {
                    EquipmentCardContent(state = state)
                }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            EquipmentCardLayout(
                status = UiEquipmentResultStatus.IN_PROGRESS,
                modifier = Modifier.widthIn(max = 480.dp),
            ) {
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
                )
            }
        }
    }
}
