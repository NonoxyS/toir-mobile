package ru.mirea.toir.sync.fixtures

import ru.mirea.toir.sync.data.FileReader

/**
 * In-memory `FileReader` that returns pre-registered byte arrays for a given URI without
 * touching disk. Tests register payloads via [putContent] before triggering uploads, and
 * may call [throwForUri] to simulate read failures (e.g. a missing file, a content URI
 * with no provider behind it).
 */
internal class TestFileReader : FileReader {

    private val contents: MutableMap<String, ByteArray> = mutableMapOf()
    private val errors: MutableMap<String, Throwable> = mutableMapOf()

    fun putContent(fileUri: String, bytes: ByteArray) {
        contents[fileUri] = bytes
    }

    fun throwForUri(fileUri: String, error: Throwable) {
        errors[fileUri] = error
    }

    override fun read(fileUri: String): ByteArray {
        errors[fileUri]?.let { throw it }
        return contents[fileUri]
            ?: error("TestFileReader has no content registered for $fileUri")
    }
}
