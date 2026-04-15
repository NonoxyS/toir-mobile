package ru.mirea.toir.feature.equipment.card.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.common.ui.compose.utils.Spacer8
import ru.mirea.toir.feature.equipment.card.presentation.EquipmentCardViewModel
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentCardLabel
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentCardState
import ru.mirea.toir.res.MR

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
                    androidx.compose.material3.TextButton(onClick = onNavigateBack) {
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
                state.errorMessage != null -> Text(
                    text = state.errorMessage.orEmpty(),
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

@Composable
private fun EquipmentCardContent(
    state: UiEquipmentCardState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EquipmentCardField(
            label = stringResource(MR.strings.equipment_card_code),
            value = state.code,
        )
        EquipmentCardField(
            label = stringResource(MR.strings.equipment_card_name),
            value = state.name,
        )
        EquipmentCardField(
            label = stringResource(MR.strings.equipment_card_type),
            value = state.type,
        )
        if (state.locationName.isNotEmpty()) {
            EquipmentCardField(
                label = stringResource(MR.strings.equipment_card_location),
                value = state.locationName,
            )
        }
        if (state.statusLabel.isNotEmpty()) {
            EquipmentCardField(
                label = "Статус",
                value = state.statusLabel,
            )
        }
    }
}

@Composable
private fun EquipmentCardField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ToirTheme.shapes.md)
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = ToirTheme.typography.caption,
            color = colors.textSecondary,
        )
        Spacer8()
        Text(
            text = value.ifEmpty { "—" },
            style = ToirTheme.typography.bodyLarge,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun EquipmentCardOpenChecklistButton(onClick: () -> Unit) {
    val colors = ToirTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.ctaPrimary,
                contentColor = colors.textOnAccent,
            ),
        ) {
            Text(
                text = stringResource(MR.strings.equipment_card_button_open_checklist),
                style = ToirTheme.typography.label,
            )
        }
    }
}
