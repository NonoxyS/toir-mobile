package ru.mirea.toir.sync.data

/**
 * Reads bytes of a locally stored photo so the upload pipeline can attach them to the
 * outgoing multipart request. Captured-on-device photos live behind `content://` URIs
 * produced by `FileProvider`, so the Android implementation must go through
 * `ContentResolver` — turning the URI into a raw file path with `Uri.path` does not
 * yield a real filesystem location and was the source of the "no connection" sync bug.
 */
internal interface FileReader {
    fun read(fileUri: String): ByteArray
}
