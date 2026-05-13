package ru.mirea.toir.sync.domain

enum class SyncTrigger {
    Periodic,
    Manual,
    AfterInspection,
    Connectivity,
    Bootstrap,
}
