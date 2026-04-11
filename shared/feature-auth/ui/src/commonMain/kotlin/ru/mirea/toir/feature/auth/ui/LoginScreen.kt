package ru.mirea.toir.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.theme.ToirColorScheme
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.common.ui.compose.utils.Spacer12
import ru.mirea.toir.common.ui.compose.utils.Spacer16
import ru.mirea.toir.common.ui.compose.utils.Spacer24
import ru.mirea.toir.common.ui.compose.utils.Spacer32
import ru.mirea.toir.common.ui.compose.utils.Spacer8
import ru.mirea.toir.feature.auth.presentation.AuthViewModel
import ru.mirea.toir.feature.auth.presentation.models.UiAuthLabel
import ru.mirea.toir.res.MR

@Composable
internal fun LoginScreen(
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    val colors = ToirTheme.colors
    val typography = ToirTheme.typography

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiAuthLabel.NavigateToMain -> onNavigateToMain()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(MR.strings.auth_title),
                style = typography.displayLarge,
                color = colors.textPrimary,
            )
            Spacer8()
            Text(
                text = stringResource(MR.strings.auth_subtitle),
                style = typography.bodyMedium,
                color = colors.textSecondary,
            )
            Spacer32()
            OutlinedTextField(
                value = state.login,
                onValueChange = viewModel::onLoginChange,
                label = { Text(text = stringResource(MR.strings.auth_login_hint)) },
                singleLine = true,
                isError = state.errorMessage != null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                shape = ToirTheme.shapes.sm,
                colors = authTextFieldColors(colors),
            )
            Spacer12()
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(text = stringResource(MR.strings.auth_password_hint)) },
                singleLine = true,
                isError = state.errorMessage != null,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { viewModel.onLoginClick() }),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                shape = ToirTheme.shapes.sm,
                colors = authTextFieldColors(colors),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                IconVisibility
                            } else {
                                IconVisibilityOff
                            },
                            contentDescription = null,
                            tint = colors.textSecondary,
                        )
                    }
                },
            )
            if (state.errorMessage != null) {
                Spacer12()
                Text(
                    text = state.errorMessage.orEmpty(),
                    style = typography.caption,
                    color = colors.error,
                )
            }
            Spacer24()
            Button(
                onClick = viewModel::onLoginClick,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .height(48.dp),
                enabled = !state.isLoading && state.login.isNotBlank() && state.password.isNotBlank(),
                shape = ToirTheme.shapes.sm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.ctaPrimary,
                    contentColor = colors.textOnAccent,
                    disabledContainerColor = colors.ctaPrimary.copy(alpha = 0.5f),
                    disabledContentColor = colors.textOnAccent.copy(alpha = 0.5f),
                ),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.textOnAccent,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(MR.strings.auth_button_login),
                        style = typography.label,
                    )
                }
            }
            Spacer16()
            // TODO: navigate to password recovery when backend API is ready
            Text(
                text = stringResource(MR.strings.auth_forgot_password),
                style = typography.caption,
                color = colors.textSecondary,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

@Composable
private fun authTextFieldColors(colors: ToirColorScheme) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = colors.surface2,
    unfocusedContainerColor = colors.surface2,
    disabledContainerColor = colors.surface2,
    errorContainerColor = colors.surface2,
    focusedBorderColor = colors.focusBorder,
    unfocusedBorderColor = colors.border,
    errorBorderColor = colors.error,
    disabledBorderColor = colors.border.copy(alpha = 0.5f),
    focusedTextColor = colors.textPrimary,
    unfocusedTextColor = colors.textPrimary,
    disabledTextColor = colors.textDisabled,
    errorTextColor = colors.textPrimary,
    focusedLabelColor = colors.textSecondary,
    unfocusedLabelColor = colors.textSecondary,
    disabledLabelColor = colors.textDisabled,
    errorLabelColor = colors.error,
    cursorColor = colors.textPrimary,
    errorCursorColor = colors.error,
)
