package ru.mirea.toir.sync.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL

@OptIn(ExperimentalForeignApi::class)
internal actual fun readFileBytes(fileUri: String): ByteArray {
    val url = NSURL(string = fileUri)
    val data = NSData.dataWithContentsOfURL(url) ?: error("Cannot read file: $fileUri")
    return data.bytes?.readBytes(data.length.toInt()) ?: ByteArray(0)
}
