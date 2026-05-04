package ru.mirea.toir.feature.auth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.theme.ToirColorScheme
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.Spacer12
import ru.mirea.toir.common.ui.compose.utils.Spacer16
import ru.mirea.toir.common.ui.compose.utils.Spacer24
import ru.mirea.toir.common.ui.compose.utils.Spacer32
import ru.mirea.toir.common.ui.compose.utils.Spacer8
import ru.mirea.toir.res.MR

@Composable
internal fun LoginContent(
    login: String,
    password: String,
    isLoading: Boolean,
    isError: Boolean,
    passwordVisible: Boolean,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
) {
    val colors = ToirTheme.colors
    val typography = ToirTheme.typography

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

            LoginField(
                value = login,
                onValueChange = onLoginChange,
                isError = isError,
                enabled = !isLoading,
            )
            Spacer12()

            LoginPasswordField(
                value = password,
                onValueChange = onPasswordChange,
                isError = isError,
                enabled = !isLoading,
                passwordVisible = passwordVisible,
                onDone = onLoginClick,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
            )

            if (isError) {
                Spacer12()
                Text(
                    text = stringResource(MR.strings.auth_error_invalid_credentials),
                    style = typography.caption,
                    color = colors.error,
                )
            }
            Spacer24()

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth(.3f)
                    .heightIn(max = 48.dp),
                enabled = !isLoading && login.isNotBlank() && password.isNotBlank(),
                shape = ToirTheme.shapes.sm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.ctaPrimary,
                    contentColor = colors.textOnAccent,
                    disabledContainerColor = colors.ctaPrimary.copy(alpha = 0.5f),
                    disabledContentColor = colors.textOnAccent.copy(alpha = 0.5f),
                ),
            ) {
                if (isLoading) {
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
        }
    }
}

@Composable
private fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var localValue by remember { mutableStateOf(value) }
    val colors = ToirTheme.colors

    OutlinedTextField(
        value = localValue,
        onValueChange = { newValue ->
            localValue = newValue
            onValueChange(newValue)
        },
        label = { Text(text = stringResource(MR.strings.auth_login_hint)) },
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = ToirTheme.shapes.sm,
        colors = loginTextFieldColors(colors),
    )
}

@Composable
internal fun loginTextFieldColors(colors: ToirColorScheme) = OutlinedTextFieldDefaults.colors(
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

@Preview
@Composable
private fun PreviewLoginContent() {
    ToirTheme {
        LoginContent(
            login = "user@example.com",
            password = "password",
            isLoading = false,
            isError = false,
            passwordVisible = false,
            onLoginChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onTogglePasswordVisibility = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLoginContentError() {
    ToirTheme {
        LoginContent(
            login = "user@example.com",
            password = "password",
            isLoading = false,
            isError = true,
            passwordVisible = false,
            onLoginChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onTogglePasswordVisibility = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLoginContentLoading() {
    ToirTheme {
        LoginContent(
            login = "user@example.com",
            password = "password",
            isLoading = true,
            isError = false,
            passwordVisible = false,
            onLoginChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onTogglePasswordVisibility = {},
        )
    }
}
