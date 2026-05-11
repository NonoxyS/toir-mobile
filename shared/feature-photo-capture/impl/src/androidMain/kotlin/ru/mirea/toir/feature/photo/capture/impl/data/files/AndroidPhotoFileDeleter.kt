package ru.mirea.toir.feature.photo.capture.impl.data.files

import android.content.Context
import android.net.Uri
import io.github.aakira.napier.Napier

internal class AndroidPhotoFileDeleter(
    private val context: Context,
) : PhotoFileDeleter {

    override fun delete(uri: String): Boolean {
        val parsed = runCatching { Uri.parse(uri) }.getOrElse {
            Napier.e(message = "PhotoFileDeleter: invalid URI $uri", throwable = it)
            return false
        }
        return runCatching {
            context.contentResolver.delete(parsed, null, null) > 0
        }.getOrElse { throwable ->
            Napier.e(message = "PhotoFileDeleter: delete failed for $uri", throwable = throwable)
            false
        }
    }
}
