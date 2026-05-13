package ru.mirea.toir.sync.domain.retry

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * Exponential backoff capped at 1 hour: min(2^(attemptCount - 1) * 30s, 1h).
 * attemptCount must be >= 1 (the just-incremented count). attemptCount=1 → 30s, 2 → 1m, 3 → 2m, etc.
 */
internal object BackoffPolicy {
    private val BASE: Duration = 30.seconds
    private val MAX: Duration = 1.hours
    private const val MAX_SHIFT = 10

    fun nextDelay(attemptCount: Long): Duration {
        val shift = (attemptCount - 1).coerceIn(0L, MAX_SHIFT.toLong()).toInt()
        val candidate = BASE * (1L shl shift).toInt()
        return if (candidate > MAX) MAX else candidate
    }
}
