package ru.mirea.toir.core.database

interface TransactionRunner {
    fun <T> transactional(block: () -> T): T
}

internal class TransactionRunnerImpl(
    private val db: ToirDatabase,
) : TransactionRunner {
    override fun <T> transactional(block: () -> T): T {
        var captured: Any? = SENTINEL
        db.transaction {
            captured = block()
        }
        @Suppress("UNCHECKED_CAST")
        return captured as T
    }

    private companion object {
        private val SENTINEL = Any()
    }
}
