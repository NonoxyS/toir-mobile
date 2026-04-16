package ru.mirea.toir.feature.routes.list.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import ru.mirea.toir.feature.routes.list.presentation.models.UiRouteAssignment

@Composable
internal fun RoutesListContent(
    isLoading: Boolean,
    isError: Boolean,
    assignments: ImmutableList<UiRouteAssignment>,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onStartInspection: (assignmentId: String) -> Unit,
    onContinueInspection: (inspectionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading -> RoutesListLoading(modifier = modifier)
        isError -> RoutesListError(onRetry = onRetry, modifier = modifier)
        assignments.isEmpty() -> RoutesListEmpty(onRefresh = onRefresh, modifier = modifier)
        else -> RoutesListItems(
            assignments = assignments,
            onStartInspection = onStartInspection,
            onContinueInspection = onContinueInspection,
            modifier = modifier,
        )
    }
}

@Composable
internal fun RoutesListItems(
    assignments: ImmutableList<UiRouteAssignment>,
    onStartInspection: (assignmentId: String) -> Unit,
    onContinueInspection: (inspectionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
    ) {
        items(items = assignments, key = { it.assignmentId }) { item ->
            RouteAssignmentCard(
                item = item,
                onStartClick = { onStartInspection(item.assignmentId) },
                onContinueClick = { onContinueInspection(item.inspectionId.orEmpty()) },
            )
        }
    }
}
