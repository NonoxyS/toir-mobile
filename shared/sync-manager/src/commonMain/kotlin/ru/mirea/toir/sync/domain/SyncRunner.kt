package ru.mirea.toir.sync.domain

import kotlinx.coroutines.Job

interface SyncRunner {
    fun syncNow(trigger: SyncTrigger): Job
}
