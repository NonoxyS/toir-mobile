package ru.mirea.toir.common.ui.compose.theme

import androidx.compose.runtime.Composable

/**
 * Подстраивает контент системных баров (статус-бар, навигационная панель) под тему.
 * Светлая тема — тёмные иконки, тёмная тема — светлые.
 */
@Composable
expect fun SystemBarsEffect(isDark: Boolean)
