package ru.mirea.toir.sync.data

import android.content.Context
import androidx.core.net.toUri
import java.io.File

/**
 * Writes restored photo bytes into `context.filesDir/photos/` — the same directory the
 * camera-capture flow writes to (see `CameraLauncher.android.kt` `createPhotoUri`).
 * The returned URI is a `file://` URI pointing at the absolute path, which coil3 can
 * render via `AsyncImage(model = uri)` without extra config.
 */
internal class AndroidPhotoFileWriter(
    private val context: Context,
) : PhotoFileWriter {

    override fun write(photoId: String, bytes: ByteArray, mimeType: String?): String {
        val photosDir = File(context.filesDir, PHOTOS_DIR_NAME).also { it.mkdirs() }
        val extension = pickExtension(mimeType)
        val file = File(photosDir, "$photoId.$extension")
        file.writeBytes(bytes)
        return file.toUri().toString()
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
