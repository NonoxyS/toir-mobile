package ru.mirea.toir.sync.data

import android.content.Context
import androidx.core.net.toUri
import java.io.FileNotFoundException

internal class AndroidFileReader(
    private val context: Context,
) : FileReader {

    override fun read(fileUri: String): ByteArray {
        val uri = fileUri.toUri()
        return context.contentResolver.openInputStream(uri)
            ?.use { it.readBytes() }
            ?: throw FileNotFoundException("Cannot open input stream for URI: $fileUri")
    }
}
