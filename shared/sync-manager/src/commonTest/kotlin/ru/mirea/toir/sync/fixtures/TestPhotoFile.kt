package ru.mirea.toir.sync.fixtures

internal expect fun writeFakeFile(bytes: ByteArray, fileName: String): String
internal expect fun deleteFakeFile(path: String)
