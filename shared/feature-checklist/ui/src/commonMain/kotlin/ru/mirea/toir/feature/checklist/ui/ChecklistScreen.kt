package ru.mirea.toir.feature.checklist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.mirea.toir.common.ui.compose.components.shared.button.ToirPrimaryButton
import ru.mirea.toir.common.ui.compose.components.shared.button.ToirSecondaryButton
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.feature.checklist.presentation.ChecklistViewModel
import ru.mirea.toir.feature.checklist.presentation.models.UiAnswerType
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistLabel
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistState
import ru.mirea.toir.feature.checklist.ui.items.BooleanChecklistItem
import ru.mirea.toir.feature.checklist.ui.items.ConfirmChecklistItem
import ru.mirea.toir.feature.checklist.ui.items.NumberChecklistItem
import ru.mirea.toir.feature.checklist.ui.items.SelectChecklistItem
import ru.mirea.toir.feature.checklist.ui.items.TextChecklistItem
import ru.mirea.toir.res.MR

@Composable
internal fun ChecklistScreen(
    equipmentResultId: String,
    onNavigateToPhotoCapture: (checklistItemResultId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ChecklistViewModel = koinViewModel { parametersOf(equipmentResultId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            is UiChecklistLabel.NavigateToPhotoCapture ->
                onNavigateToPhotoCapture(label.checklistItemResultId)

            UiChecklistLabel.ChecklistCompleted -> onNavigateBack()
        }
    }

    ChecklistScreenContent(
        state = state,
        onBooleanAnswer = viewModel::onBooleanAnswer,
        onNumberAnswer = viewModel::onNumberAnswer,
        onTextAnswer = viewModel::onTextAnswer,
        onSelectAnswer = viewModel::onSelectAnswer,
        onConfirm = viewModel::onConfirm,
        onAddPhoto = viewModel::onAddPhoto,
        onFinishChecklist = viewModel::onFinishChecklist,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChecklistScreenContent(
    state: UiChecklistState,
    onBooleanAnswer: (String, Boolean?) -> Unit,
    onNumberAnswer: (String, String) -> Unit,
    onTextAnswer: (String, String) -> Unit,
    onSelectAnswer: (String, String) -> Unit,
    onConfirm: (String, Boolean) -> Unit,
    onAddPhoto: (String) -> Unit,
    onFinishChecklist: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = ToirTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(MR.strings.checklist_title),
                        style = ToirTheme.typography.headline,
                        color = ToirTheme.colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Image(
                            painter = painterResource(MR.images.ic_arrow_back),
                            contentDescription = stringResource(
                                MR.strings.checklist_back_content_description,
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
            if (!state.isLoading && !state.isError) {
                ChecklistFinishBar(
                    isValidationError = state.isValidationError,
                    isPhotoValidationError = state.isPhotoValidationError,
                    onFinishChecklist = onFinishChecklist,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.isLoading -> ChecklistLoading()
                state.isError -> ChecklistError()
                else -> ChecklistList(
                    state = state,
                    onBooleanAnswer = onBooleanAnswer,
                    onNumberAnswer = onNumberAnswer,
                    onTextAnswer = onTextAnswer,
                    onSelectAnswer = onSelectAnswer,
                    onConfirm = onConfirm,
                    onAddPhoto = onAddPhoto,
                )
            }
        }
    }
}

@Composable
private fun ChecklistFinishBar(
    isValidationError: Boolean,
    isPhotoValidationError: Boolean,
    onFinishChecklist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ToirTheme.colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        ) {
            if (isValidationError) {
                Text(
                    text = stringResource(MR.strings.checklist_validation_error_required),
                    style = ToirTheme.typography.bodyMedium,
                    color = ToirTheme.colors.error,
                )
            }
            if (isPhotoValidationError) {
                Text(
                    text = stringResource(MR.strings.checklist_validation_error_photo),
                    style = ToirTheme.typography.bodyMedium,
                    color = ToirTheme.colors.error,
                )
            }
            ToirPrimaryButton(
                onClick = onFinishChecklist,
                text = stringResource(MR.strings.checklist_button_finish),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ChecklistLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ToirTheme.colors.textSecondary)
    }
}

@Composable
private fun ChecklistError() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(MR.strings.error_generic),
            style = ToirTheme.typography.bodyMedium,
            color = ToirTheme.colors.error,
        )
    }
}

@Composable
private fun ChecklistList(
    state: UiChecklistState,
    onBooleanAnswer: (String, Boolean?) -> Unit,
    onNumberAnswer: (String, String) -> Unit,
    onTextAnswer: (String, String) -> Unit,
    onSelectAnswer: (String, String) -> Unit,
    onConfirm: (String, Boolean) -> Unit,
    onAddPhoto: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        items(items = state.items, key = { it.id }) { item ->
            ChecklistItemRow(
                item = item,
                onBooleanAnswer = onBooleanAnswer,
                onNumberAnswer = onNumberAnswer,
                onTextAnswer = onTextAnswer,
                onSelectAnswer = onSelectAnswer,
                onConfirm = onConfirm,
                onAddPhoto = onAddPhoto,
            )
        }
    }
}

@Composable
private fun ChecklistItemRow(
    item: UiChecklistItem,
    onBooleanAnswer: (String, Boolean?) -> Unit,
    onNumberAnswer: (String, String) -> Unit,
    onTextAnswer: (String, String) -> Unit,
    onSelectAnswer: (String, String) -> Unit,
    onConfirm: (String, Boolean) -> Unit,
    onAddPhoto: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        when (item.answerType) {
            is UiAnswerType.Boolean -> BooleanChecklistItem(
                item = item,
                onValueChange = { value -> onBooleanAnswer(item.id, value) },
            )

            is UiAnswerType.Number -> NumberChecklistItem(
                item = item,
                onValueChange = { value -> onNumberAnswer(item.id, value) },
            )

            is UiAnswerType.Text -> TextChecklistItem(
                item = item,
                onValueChange = { value -> onTextAnswer(item.id, value) },
            )

            is UiAnswerType.Select -> SelectChecklistItem(
                item = item,
                onSelectOption = { value -> onSelectAnswer(item.id, value) },
            )

            is UiAnswerType.Confirm -> ConfirmChecklistItem(
                item = item,
                onConfirmChange = { value -> onConfirm(item.id, value) },
            )
        }

        if (item.requiresPhoto) {
            ChecklistPhotoSection(
                item = item,
                onAddPhoto = onAddPhoto,
            )
        }
    }
}

@Composable
private fun ChecklistPhotoSection(
    item: UiChecklistItem,
    onAddPhoto: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    ) {
        ToirSecondaryButton(
            onClick = { onAddPhoto(item.id) },
            text = stringResource(MR.strings.checklist_button_add_photo),
            enabled = item.resultId != null,
            modifier = Modifier.fillMaxWidth(),
        )
        if (item.photoCount > 0) {
            Text(
                text = stringResource(MR.strings.checklist_photo_count, item.photoCount),
                style = ToirTheme.typography.bodyMedium,
                color = ToirTheme.colors.textSecondary,
            )
        }
    }
}
