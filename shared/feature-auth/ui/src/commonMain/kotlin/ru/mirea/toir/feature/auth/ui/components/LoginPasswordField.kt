package ru.mirea.toir.feature.auth.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
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
    val colors = ToirTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = stringResource(MR.strings.auth_password_hint)) },
        singleLine = true,
        isError = isError,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = ToirTheme.shapes.sm,
        colors = loginTextFieldColors(colors),
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisibility) {
                Icon(
                    painter = painterResource(
                        imageResource = if (passwordVisible) {
                            MR.images.visibility_off
                        } else {
                            MR.images.visibility
                        }
                    ),
                    contentDescription = null,
                    tint = colors.textSecondary,
                )
            }
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
