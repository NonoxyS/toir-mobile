package ru.mirea.toir.common.ui.compose.utils

import androidx.compose.runtime.Composable

interface PlatformActions {
    fun openUrl(url: String, onFail: () -> Unit = NoOp)
    fun openEmailClient(email: String, onFail: () -> Unit = NoOp)
    fun openMap(address: String, onFail: () -> Unit = NoOp)
    fun openPhonebook(phone: String, onFail: () -> Unit = NoOp)
}

private val NoOp: () -> Unit = {}

@Composable
expect fun rememberPlatformActions(): PlatformActions
