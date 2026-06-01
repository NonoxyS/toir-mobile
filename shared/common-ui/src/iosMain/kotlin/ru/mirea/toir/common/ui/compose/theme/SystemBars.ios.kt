package ru.mirea.toir.common.ui.compose.theme

import androidx.compose.runtime.Composable

/**
 * iOS: статус-бар управляется хост-контроллером (ComposeUIViewController), стиль `.default`
 * сам подстраивается под userInterfaceStyle системы. Отдельный эффект не нужен.
 */
@Composable
actual fun SystemBarsEffect(isDark: Boolean) {
    // No-op: iOS статус-бар наследует системную тему через хост-контроллер.
}
