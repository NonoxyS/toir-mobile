package ru.mirea.toir.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
val unconfinedDispatcher: CoroutineDispatcher = Dispatchers.Unconfined
