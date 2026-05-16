package ru.mirea.toir.sync.data

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile

/**
 * Writes restored photo bytes into the app's `Documents/photos` directory. iOS camera
 * capture is not implemented yet (see `CameraLauncher.ios.kt`) — once it lands, both
 * flows should target the same directory. The returned URI is `file://...` and is
 * loadable by coil3.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class IosPhotoFileWriter : PhotoFileWriter {

    override fun write(photoId: String, bytes: ByteArray, mimeType: String?): String {
        val photosDir = ensurePhotosDir()
        val extension = pickExtension(mimeType)
        val path = "$photosDir/$photoId.$extension"
        memScoped {
            val data = NSData.create(bytes = allocArrayOf(bytes), length = bytes.size.toULong())
            val ok = data.writeToFile(path, atomically = true)
            if (!ok) error("Failed to write photo bytes to $path")
        }
        return NSURL.fileURLWithPath(path).absoluteString ?: "file://$path"
    }

    private fun ensurePhotosDir(): String {
        val docs = NSSearchPathForDirectoriesInDomains(
            directory = NSDocumentDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true,
        ).firstOrNull() as? String
            ?: error("NSDocumentDirectory not available")
        val dir = "$docs/$PHOTOS_DIR_NAME"
        NSFileManager.defaultManager.createDirectoryAtPath(
            path = dir,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
        return dir
    }

    private fun pickExtension(mimeType: String?): String = when {
        mimeType == null -> DEFAULT_EXTENSION
        mimeType.contains(PNG_MIME, ignoreCase = true) -> "png"
        else -> DEFAULT_EXTENSION
    }

    private companion object {
        const val PHOTOS_DIR_NAME = "photos"
        const val DEFAULT_EXTENSION = "jpg"
        const val PNG_MIME = "image/png"
    }
}
