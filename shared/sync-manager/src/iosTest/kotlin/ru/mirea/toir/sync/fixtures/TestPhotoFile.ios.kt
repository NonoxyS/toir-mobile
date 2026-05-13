@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package ru.mirea.toir.sync.fixtures

import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.create
import platform.Foundation.writeToFile

internal actual fun writeFakeFile(bytes: ByteArray, fileName: String): String {
    val path = NSTemporaryDirectory() + fileName
    memScoped {
        val nsData = NSData.create(bytes = allocArrayOf(bytes), length = bytes.size.toULong())
        nsData.writeToFile(path, atomically = true)
    }
    return path
}

internal actual fun deleteFakeFile(path: String) {
    NSFileManager.defaultManager.removeItemAtPath(path, null)
}
