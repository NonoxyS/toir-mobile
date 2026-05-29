package ru.mirea.toir.feature.route.points.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.text.style.TextAlign
import ru.mirea.toir.common.ui.compose.components.shared.button.ToirPrimaryButton
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.common.ui.compose.utils.Spacer16
import ru.mirea.toir.common.ui.compose.utils.Spacer24
import ru.mirea.toir.common.ui.compose.utils.Spacer4
import ru.mirea.toir.common.ui.compose.utils.Spacer8
import ru.mirea.toir.feature.route.points.presentation.RoutePointsViewModel
import ru.mirea.toir.feature.route.points.presentation.models.UiEquipmentResultStatus
import ru.mirea.toir.feature.route.points.presentation.models.UiRoutePoint
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
    onNavigateBack: () -> Unit,
    viewModel: RoutePointsViewModel = koinViewModel { parametersOf(inspectionId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            is UiRoutePointsLabel.NavigateToEquipmentCard ->
                onNavigateToEquipmentCard(label.inspectionId, label.routePointId)

            UiRoutePointsLabel.InspectionFinished -> onInspectionFinish()
        }
    }

    Scaffold(
        containerColor = ToirTheme.colors.background,
        topBar = { RoutePointsTopBar(state = state, onNavigateBack = onNavigateBack) },
        bottomBar = {
            if (state.canFinish && !state.isError) {
                RoutePointsFinishButton(
                    onClick = viewModel::onFinishInspection,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
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

                state.isError -> RoutePointsError(onRetry = viewModel::onRetry)

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
private fun RoutePointsTopBar(
    state: UiRoutePointsState,
    onNavigateBack: () -> Unit,
) {
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
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Image(
                        painter = painterResource(MR.images.ic_arrow_back),
                        contentDescription = stringResource(
                            MR.strings.route_points_back_content_description,
                        ),
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(colors.textSecondary),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.background,
            ),
        )
        if (!state.isLoading && !state.isError && state.points.isNotEmpty()) {
            RoutePointsProgressHeader(state = state)
        }
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
            drawStopIndicator = {},
        )
    }
}

@Composable
internal fun RoutePointsContent(
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
private fun RoutePointsError(onRetry: () -> Unit) {
    val colors = ToirTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(ToirTheme.shapes.pill)
                .background(colors.errorSubtle),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(MR.images.ic_error_outline),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                colorFilter = ColorFilter.tint(colors.error),
            )
        }
        Spacer16()
        Text(
            text = stringResource(MR.strings.route_points_error_title),
            style = ToirTheme.typography.headline,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer8()
        Text(
            text = stringResource(MR.strings.route_points_error_subtitle),
            style = ToirTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer24()
        ToirPrimaryButton(
            onClick = onRetry,
            text = stringResource(MR.strings.route_points_button_retry),
        )
    }
}

@Composable
private fun RoutePointsFinishButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        ToirPrimaryButton(
            onClick = onClick,
            text = stringResource(MR.strings.route_points_button_finish),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private val previewPoints = persistentListOf(
    UiRoutePoint(
        routePointId = "1",
        equipmentCode = "EQ-001",
        equipmentName = "Насос циркуляционный",
        locationName = "Котельная",
        status = UiEquipmentResultStatus.COMPLETED,
        hasIssues = false,
        equipmentResultId = "res-1",
    ),
    UiRoutePoint(
        routePointId = "2",
        equipmentCode = "EQ-002",
        equipmentName = "Вентилятор приточный",
        locationName = "Машинное отделение",
        status = UiEquipmentResultStatus.IN_PROGRESS,
        hasIssues = false,
        equipmentResultId = "res-2",
    ),
    UiRoutePoint(
        routePointId = "3",
        equipmentCode = "EQ-003",
        equipmentName = "Трансформатор ТМ-100",
        locationName = "",
        status = UiEquipmentResultStatus.NOT_STARTED,
        hasIssues = false,
        equipmentResultId = null,
    ),
)

@Preview
@Composable
private fun PreviewRoutePointsScreenLoading() {
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
private fun PreviewRoutePointsScreenContent() {
    ToirTheme {
        RoutePointsContent(
            state = UiRoutePointsState(
                routeName = "Обход северного крыла",
                points = previewPoints,
                isLoading = false,
                canFinish = false,
                isError = false,
            ),
            onPointClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewRoutePointsScreenContentCanFinish() {
    val points = persistentListOf(
        UiRoutePoint(
            routePointId = "1",
            equipmentCode = "EQ-001",
            equipmentName = "Насос циркуляционный",
            locationName = "Котельная",
            status = UiEquipmentResultStatus.COMPLETED,
            hasIssues = false,
            equipmentResultId = "res-1",
        ),
    )
    ToirTheme {
        RoutePointsContent(
            state = UiRoutePointsState(
                routeName = "Мини-маршрут",
                points = points,
                isLoading = false,
                canFinish = true,
                isError = false,
            ),
            onPointClick = {},
        )
    }
}
