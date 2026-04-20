package ru.mirea.toir.sync.data

import androidx.core.net.toUri
import java.io.File

internal actual fun readFileBytes(fileUri: String): ByteArray {
    val uriPath = fileUri.toUri().path ?: error("Invalid URI: $fileUri")
    return File(uriPath).readBytes()
}
