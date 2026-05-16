package ru.mirea.toir.sync.fixtures

import ru.mirea.toir.sync.data.PhotoFileWriter

/**
 * In-memory writer that records every `write` call without touching disk. Tests can
 * inspect [writes] (photoId -> (bytes, mimeType, returnedUri)) and the returned URI is
 * a deterministic synthetic `file://test/...` string keyed by photoId.
 *
 * Use [throwForId] to make a specific photoId throw, simulating disk errors.
 */
internal class TestPhotoFileWriter : PhotoFileWriter {

    data class Recorded(val bytes: ByteArray, val mimeType: String?, val returnedUri: String)

    val writes: MutableMap<String, Recorded> = mutableMapOf()
    var throwForId: String? = null

    override fun write(photoId: String, bytes: ByteArray, mimeType: String?): String {
        if (photoId == throwForId) error("Simulated disk write failure for $photoId")
        val uri = "file:///test/photos/$photoId.jpg"
        writes[photoId] = Recorded(bytes = bytes, mimeType = mimeType, returnedUri = uri)
        return uri
    }
}
