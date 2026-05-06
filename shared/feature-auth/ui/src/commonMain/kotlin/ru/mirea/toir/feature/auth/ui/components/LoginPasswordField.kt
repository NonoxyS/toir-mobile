package ru.mirea.toir.feature.auth.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.components.shared.textfield.ToirOutlinedTextField
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@Composable
internal fun LoginPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    enabled: Boolean,
    passwordVisible: Boolean,
    onDone: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
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
        label = stringResource(MR.strings.auth_password_hint),
        isError = isError,
        enabled = enabled,
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisibility) {
                Icon(
                    painter = painterResource(
                        if (passwordVisible) MR.images.visibility_off else MR.images.visibility,
                    ),
                    contentDescription = null,
                    tint = ToirTheme.colors.textSecondary,
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
    )
}

@Preview
@Composable
private fun PreviewLoginPasswordFieldEmpty() {
    ToirTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LoginPasswordField(
                value = "",
                onValueChange = {},
                isError = false,
                enabled = true,
                passwordVisible = false,
                onDone = {},
                onTogglePasswordVisibility = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewLoginPasswordFieldError() {
    ToirTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LoginPasswordField(
                value = "pass",
                onValueChange = {},
                isError = true,
                enabled = true,
                passwordVisible = false,
                onDone = {},
                onTogglePasswordVisibility = {},
            )
        }
    }
}
