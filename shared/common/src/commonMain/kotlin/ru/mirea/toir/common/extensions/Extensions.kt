package ru.mirea.toir.common.extensions

fun <T : Any> T?.requireNotNull(
    message: String? = null,
): T = message?.let {
    requireNotNull(this) { it }
} ?: requireNotNull(this)
