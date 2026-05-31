package ru.mirea.toir.feature.routes.list.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.feature.routes.list.presentation.RoutesListViewModel
import ru.mirea.toir.feature.routes.list.presentation.models.UiRouteAssignment
import ru.mirea.toir.feature.routes.list.presentation.models.UiRouteStatus
import ru.mirea.toir.feature.routes.list.presentation.models.UiRoutesListLabel
import ru.mirea.toir.feature.routes.list.presentation.models.UiSyncIndicator
import ru.mirea.toir.feature.routes.list.ui.components.RoutesListContent
import ru.mirea.toir.feature.routes.list.ui.components.SyncIndicatorIcon
import ru.mirea.toir.feature.routes.list.ui.components.SyncStatusBottomSheet
import ru.mirea.toir.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutesListScreen(
    onNavigateToRoutePoints: (inspectionId: String) -> Unit,
    viewModel: RoutesListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            is UiRoutesListLabel.NavigateToRoutePoints -> onNavigateToRoutePoints(label.inspectionId)
        }
    }

    Scaffold(
        topBar = {
            RoutesListTopBar(
                syncIndicator = state.syncIndicator,
                onSyncIconClick = viewModel::onSyncIndicatorClicked,
            )
        },
        containerColor = ToirTheme.colors.background,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars)
    ) { paddingValues ->
        RoutesListContent(
            isLoading = state.isLoading,
            isError = state.isError,
            assignments = state.assignments,
            onRetry = viewModel::onRefresh,
            onRefresh = viewModel::onRefresh,
            onStartInspection = viewModel::onStartInspection,
            onContinueInspection = viewModel::onContinueInspection,
            onSyncRequiredAction = viewModel::onSyncNowClicked,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }

    if (state.isSyncSheetVisible) {
        SyncStatusBottomSheet(
            indicator = state.syncIndicator,
            lastSuccessAt = state.syncLastSuccessAt,
            lastFailedAt = state.syncLastFailedAt,
            onSyncNow = viewModel::onSyncNowClicked,
            onDismiss = viewModel::onSyncSheetDismissed,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutesListTopBar(
    syncIndicator: UiSyncIndicator = UiSyncIndicator(),
    onSyncIconClick: () -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(MR.strings.routes_list_title),
                style = ToirTheme.typography.headline,
                color = ToirTheme.colors.textPrimary,
            )
        },
        actions = {
            SyncIndicatorIcon(
                indicator = syncIndicator,
                onClick = onSyncIconClick,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ToirTheme.colors.background),
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewRoutesListTopBar() {
    ToirTheme {
        RoutesListTopBar()
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRoutesListScreenLoading() {
    ToirTheme {
        Scaffold(
            topBar = { RoutesListTopBar() },
            containerColor = ToirTheme.colors.background,
        ) { paddingValues ->
            RoutesListContent(
                isLoading = true,
                isError = false,
                assignments = persistentListOf(),
                onRetry = {},
                onRefresh = {},
                onStartInspection = {},
                onContinueInspection = {},
                onSyncRequiredAction = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

private val previewAssignments = persistentListOf(
    UiRouteAssignment(
        assignmentId = "assignment-001",
        routeName = "Обход северного крыла",
        routeNumber = "ASS-001",
        status = UiRouteStatus.IN_PROGRESS,
        completedPoints = 3,
        totalPoints = 10,
        progress = 0.3f,
        assignedAt = "12.04.2026",
        inspectionId = "inspection-001",
        hasPendingSync = false,
    ),
    UiRouteAssignment(
        assignmentId = "assignment-002",
        routeName = "Плановая проверка оборудования",
        routeNumber = "ASS-002",
        status = UiRouteStatus.ASSIGNED,
        completedPoints = 0,
        totalPoints = 5,
        progress = 0f,
        assignedAt = "12.04.2026",
        inspectionId = null,
        hasPendingSync = false,
    ),
    UiRouteAssignment(
        assignmentId = "assignment-003",
        routeName = "Завершённый маршрут",
        routeNumber = "ASS-003",
        status = UiRouteStatus.COMPLETED,
        completedPoints = 8,
        totalPoints = 8,
        progress = 1f,
        assignedAt = "11.04.2026",
        inspectionId = "inspection-003",
        hasPendingSync = true,
    ),
)

@Preview(showBackground = true)
@Composable
private fun PreviewRoutesListScreenContent() {
    ToirTheme {
        Scaffold(
            topBar = { RoutesListTopBar() },
            containerColor = ToirTheme.colors.background,
        ) { paddingValues ->
            RoutesListContent(
                isLoading = false,
                isError = false,
                assignments = previewAssignments,
                onRetry = {},
                onRefresh = {},
                onStartInspection = {},
                onContinueInspection = {},
                onSyncRequiredAction = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRoutesListScreenError() {
    ToirTheme {
        Scaffold(
            topBar = { RoutesListTopBar() },
            containerColor = ToirTheme.colors.background,
        ) { paddingValues ->
            RoutesListContent(
                isLoading = false,
                isError = true,
                assignments = persistentListOf(),
                onRetry = {},
                onRefresh = {},
                onStartInspection = {},
                onContinueInspection = {},
                onSyncRequiredAction = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}
