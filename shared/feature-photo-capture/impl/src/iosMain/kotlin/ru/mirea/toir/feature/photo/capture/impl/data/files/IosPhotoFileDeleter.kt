package ru.mirea.toir.feature.photo.capture.impl.data.files

import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
internal class IosPhotoFileDeleter : PhotoFileDeleter {

    override fun delete(uri: String): Boolean {
        val url = NSURL.URLWithString(uri) ?: run {
            Napier.e(message = "PhotoFileDeleter: invalid URI $uri")
            return false
        }
        val path = url.path ?: run {
            Napier.e(message = "PhotoFileDeleter: URI has no path: $uri")
            return false
        }
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(path)) return false
        return fileManager.removeItemAtPath(path, error = null)
    }
}
