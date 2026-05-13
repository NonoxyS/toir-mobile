package ru.mirea.toir.core.database

interface TransactionRunner {
    fun transactional(block: () -> Unit)
}

internal class TransactionRunnerImpl(
    private val db: ToirDatabase,
) : TransactionRunner {
    override fun transactional(block: () -> Unit) {
        db.transaction { block() }
    }
}
