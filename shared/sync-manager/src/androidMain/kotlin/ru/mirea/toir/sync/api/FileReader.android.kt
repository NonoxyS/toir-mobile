package ru.mirea.toir.sync.api

internal actual fun readFileBytes(fileUri: String): ByteArray =
    java.io.File(android.net.Uri.parse(fileUri).path ?: error("Invalid URI: $fileUri")).readBytes()
