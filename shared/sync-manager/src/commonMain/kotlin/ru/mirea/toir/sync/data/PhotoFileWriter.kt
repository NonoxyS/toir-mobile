package ru.mirea.toir.sync.data

/**
 * Platform writer for restored photo bytes. Writes the bytes to a local file and returns
 * the URI string to store in `photos.file_uri`. The location mirrors the photo-capture
 * flow's storage (Android: `context.filesDir/photos/`; iOS: Documents/photos) so that
 * the photo viewer renders restored and freshly-captured photos identically.
 *
 * `photoId` is used as the file stem (unique and stable). `mimeType` is hint-only:
 * the extension is `.jpg` unless `mimeType` explicitly indicates PNG.
 *
 * Implementations throw on disk errors; the caller logs and continues with the next photo.
 */
internal interface PhotoFileWriter {
    fun write(photoId: String, bytes: ByteArray, mimeType: String?): String
}
