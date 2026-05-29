package ru.mirea.toir.common.ui.compose.components.shared.textfield

import ru.mirea.toir.common.ui.compose.components.shared.textfield.filters.InputFilter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

/**
 * Outlined text field wrapper that injects [ToirTheme] tokens into Material3's
 * [OutlinedTextField].
 *
 * The label is rendered as a separate [Text] above the field — we intentionally diverge
 * from the Material3 floating label pattern. When [isRequired] is true, a red asterisk
 * is rendered next to the label.
 *
 * The optional [supportingText] is rendered below the field; when [isError] is true it
 * is tinted with [ToirTheme.colors.error], otherwise [ToirTheme.colors.textSecondary].
 *
 * Supports `trailingIcon`, `keyboardActions`, and `visualTransformation` for password /
 * search-style fields.
 */
@Suppress("LongParameterList") // mirrors the Material3 OutlinedTextField surface
@Composable
fun ToirOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    isRequired: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    inputFilter: InputFilter? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    labelTrailingContent: (@Composable () -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (label != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = label,
                    style = ToirTheme.typography.bodyMedium,
                    color = ToirTheme.colors.textSecondary,
                )
                if (isRequired) {
                    Text(
                        text = "*",
                        style = ToirTheme.typography.bodyMedium,
                        color = ToirTheme.colors.error,
                    )
                }
                if (labelTrailingContent != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    labelTrailingContent()
                }
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                val processed = inputFilter?.apply(newValue) ?: newValue
                onValueChange(processed)
            },
            modifier = modifier,
            enabled = enabled,
            textStyle = ToirTheme.typography.bodyLarge,
            placeholder = if (placeholder != null) {
                { Text(text = placeholder, style = ToirTheme.typography.bodyLarge) }
            } else {
                null
            },
            isError = isError,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            shape = ToirTheme.shapes.sm,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ToirTheme.colors.surface2,
                unfocusedContainerColor = ToirTheme.colors.surface2,
                disabledContainerColor = ToirTheme.colors.surface2,
                errorContainerColor = ToirTheme.colors.surface2,
                focusedBorderColor = ToirTheme.colors.focusBorder,
                unfocusedBorderColor = ToirTheme.colors.border,
                disabledBorderColor = ToirTheme.colors.borderSubtle,
                errorBorderColor = ToirTheme.colors.error,
                cursorColor = ToirTheme.colors.textPrimary,
                focusedTextColor = ToirTheme.colors.textPrimary,
                unfocusedTextColor = ToirTheme.colors.textPrimary,
                disabledTextColor = ToirTheme.colors.textDisabled,
                errorTextColor = ToirTheme.colors.textPrimary,
                focusedPlaceholderColor = ToirTheme.colors.textSecondary,
                unfocusedPlaceholderColor = ToirTheme.colors.textSecondary,
            ),
        )
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = ToirTheme.typography.caption,
                color = if (isError) ToirTheme.colors.error else ToirTheme.colors.textSecondary,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirOutlinedTextFieldEmpty() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirOutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = "Серийный номер",
                placeholder = "Введите серийный номер",
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirOutlinedTextFieldWithValue() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirOutlinedTextField(
                value = "SN-123456",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = "Серийный номер",
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirOutlinedTextFieldError() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirOutlinedTextField(
                value = "abc",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = "Серийный номер",
                isError = true,
                supportingText = "Должно содержать только цифры",
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirOutlinedTextFieldRequired() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirOutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = "Серийный номер",
                placeholder = "Введите серийный номер",
                isRequired = true,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirOutlinedTextFieldDisabled() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirOutlinedTextField(
                value = "SN-123456",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = "Серийный номер",
                enabled = false,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirOutlinedTextFieldMultiline() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirOutlinedTextField(
                value = "Многострочный комментарий с переносом\nна несколько строк",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = "Комментарий",
                singleLine = false,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirOutlinedTextFieldWithTrailingIcon() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirOutlinedTextField(
                value = "secret",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = "Пароль",
                trailingIcon = {
                    Icon(
                        painter = painterResource(MR.images.visibility),
                        contentDescription = null,
                        tint = ToirTheme.colors.textSecondary,
                    )
                },
            )
        }
    }
}
