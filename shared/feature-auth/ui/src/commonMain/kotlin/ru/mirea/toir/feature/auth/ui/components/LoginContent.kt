package ru.mirea.toir.feature.auth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import ru.mirea.toir.common.ui.compose.components.shared.button.ToirPrimaryButton
import ru.mirea.toir.common.ui.compose.components.shared.textfield.ToirOutlinedTextField
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

            ToirPrimaryButton(
                onClick = onLoginClick,
                text = stringResource(MR.strings.auth_button_login),
                modifier = Modifier.fillMaxWidth(.3f),
                enabled = !isLoading && login.isNotBlank() && password.isNotBlank(),
                isLoading = isLoading,
            )
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
    var localValue by remember(value) { mutableStateOf(value) }
    ToirOutlinedTextField(
        value = localValue,
        onValueChange = { newValue ->
            localValue = newValue
            onValueChange(newValue)
        },
        modifier = modifier.fillMaxWidth(),
        label = stringResource(MR.strings.auth_login_hint),
        isError = isError,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
}

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
