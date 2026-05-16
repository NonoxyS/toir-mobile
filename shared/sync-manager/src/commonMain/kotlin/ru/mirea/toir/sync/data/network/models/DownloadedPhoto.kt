package ru.mirea.toir.sync.data.network.models

/**
 * Raw bytes plus the Content-Type the server sent. The MIME type is informational —
 * the writer uses it to pick a file extension; if it is `null` or unrecognized,
 * `.jpg` is assumed (matches the capture flow which always writes JPEG).
 */
internal data class DownloadedPhoto(
    val bytes: ByteArray,
    val mimeType: String?,
)
