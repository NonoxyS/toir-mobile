package ru.mirea.toir.common.extensions

fun <T> T.wrapResultSuccess(): Result<T> = Result.success(this)

fun <T> Throwable.wrapResultFailure(): Result<T> = Result.failure(this)
