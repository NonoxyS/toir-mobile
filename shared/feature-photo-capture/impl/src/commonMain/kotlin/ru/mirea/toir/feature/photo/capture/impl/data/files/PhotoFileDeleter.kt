package ru.mirea.toir.feature.photo.capture.impl.data.files

internal interface PhotoFileDeleter {
    fun delete(uri: String): Boolean
}
