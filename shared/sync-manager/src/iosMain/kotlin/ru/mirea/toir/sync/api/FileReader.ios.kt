package ru.mirea.toir.sync.api

import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes

@OptIn(ExperimentalForeignApi::class)
internal actual fun readFileBytes(fileUri: String): ByteArray {
    val url = NSURL(string = fileUri)
    val data = NSData.dataWithContentsOfURL(url) ?: error("Cannot read file: $fileUri")
    return data.bytes?.readBytes(data.length.toInt()) ?: ByteArray(0)
}
