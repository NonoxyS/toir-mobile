package ru.mirea.toir.sync.domain.retry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class BackoffPolicyTest {

    @Test
    fun firstAttempt_returns30s() {
        assertEquals(30.seconds, BackoffPolicy.nextDelay(1))
    }

    @Test
    fun secondAttempt_returns1m() {
        assertEquals(1.minutes, BackoffPolicy.nextDelay(2))
    }

    @Test
    fun fifthAttempt_returns8m() {
        assertEquals(8.minutes, BackoffPolicy.nextDelay(5))
    }

    @Test
    fun eleventhAttempt_capsAt1h() {
        assertEquals(1.hours, BackoffPolicy.nextDelay(11))
    }

    @Test
    fun hugeAttempt_stillCapsAt1h() {
        assertEquals(1.hours, BackoffPolicy.nextDelay(100))
    }

    @Test
    fun zeroAttempt_treatedAsFirst() {
        assertEquals(30.seconds, BackoffPolicy.nextDelay(0))
    }
}
