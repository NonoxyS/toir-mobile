package ru.mirea.toir.feature.route.points.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
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
import ru.mirea.toir.common.ui.compose.utils.Spacer4
import ru.mirea.toir.feature.route.points.presentation.RoutePointsViewModel
import ru.mirea.toir.feature.route.points.presentation.models.UiEquipmentResultStatus
import ru.mirea.toir.feature.route.points.presentation.models.UiRoutePointsLabel
import ru.mirea.toir.feature.route.points.presentation.models.UiRoutePointsState
import ru.mirea.toir.feature.route.points.ui.components.RoutePointCard
import ru.mirea.toir.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutePointsScreen(
    inspectionId: String,
    onNavigateToEquipmentCard: (inspectionId: String, routePointId: String) -> Unit,
    onInspectionFinish: () -> Unit,
    viewModel: RoutePointsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(inspectionId) {
        viewModel.init(inspectionId)
    }

    viewModel.label.CollectFlow { label ->
        when (label) {
            is UiRoutePointsLabel.NavigateToEquipmentCard ->
                onNavigateToEquipmentCard(label.inspectionId, label.routePointId)
            UiRoutePointsLabel.InspectionFinished -> onInspectionFinish()
        }
    }

    Scaffold(
        containerColor = ToirTheme.colors.background,
        topBar = { RoutePointsTopBar(state = state) },
        bottomBar = {
            if (state.canFinish) {
                RoutePointsFinishButton(onClick = viewModel::onFinishInspection)
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
                else -> RoutePointsContent(
                    state = state,
                    onPointClick = { routePointId -> viewModel.onPointClick(routePointId) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutePointsTopBar(state: UiRoutePointsState) {
    val colors = ToirTheme.colors
    Column {
        TopAppBar(
            title = {
                Text(
                    text = state.routeName,
                    style = ToirTheme.typography.headline,
                    color = colors.textPrimary,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.background,
            ),
        )
        RoutePointsProgressHeader(state = state)
    }
}

@Composable
private fun RoutePointsProgressHeader(state: UiRoutePointsState) {
    val colors = ToirTheme.colors
    val completed = state.points.count { it.status == UiEquipmentResultStatus.COMPLETED }
    val total = state.points.size
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = stringResource(MR.strings.route_points_progress, completed, total),
            style = ToirTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        Spacer4()
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(ToirTheme.shapes.pill),
            color = colors.success,
            trackColor = colors.border,
        )
    }
}

@Composable
private fun RoutePointsContent(
    state: UiRoutePointsState,
    onPointClick: (routePointId: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = state.points, key = { it.routePointId }) { point ->
            RoutePointCard(
                item = point,
                onClick = { onPointClick(point.routePointId) },
            )
        }
    }
}

@Composable
private fun RoutePointsFinishButton(onClick: () -> Unit) {
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
                text = stringResource(MR.strings.route_points_button_finish),
                style = ToirTheme.typography.label,
            )
        }
    }
}
