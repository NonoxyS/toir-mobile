package ru.mirea.toir.sync.fixtures

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNull

class TestDatabaseSmokeTest {

    private val pair = TestDatabase.create()
    private val db = pair.first
    private val driver = pair.second

    @AfterTest
    fun tearDown() = driver.close()

    @Test
    fun `empty database — selectById returns null`() {
        val result = db.inspectionQueries.selectById("nope").executeAsOneOrNull()
        assertNull(result)
    }
}
