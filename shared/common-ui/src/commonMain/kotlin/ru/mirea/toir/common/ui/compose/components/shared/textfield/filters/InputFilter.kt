package ru.mirea.toir.common.ui.compose.components.shared.textfield.filters

fun interface InputFilter {
    fun apply(raw: String): String
}
