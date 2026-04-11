package ru.mirea.toir.feature.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.feature.auth.presentation.AuthViewModel
import ru.mirea.toir.feature.auth.presentation.models.UiAuthLabel
import ru.mirea.toir.feature.auth.ui.components.LoginContent

@Composable
internal fun LoginScreen(
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiAuthLabel.NavigateToMain -> onNavigateToMain()
        }
    }

    LoginContent(
        login = state.login,
        password = state.password,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        passwordVisible = state.passwordVisible,
        onLoginChange = viewModel::onLoginChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onLoginClick,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
    )
}
