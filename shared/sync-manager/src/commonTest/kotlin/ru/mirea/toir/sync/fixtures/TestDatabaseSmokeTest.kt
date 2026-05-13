package ru.mirea.toir.sync.fixtures

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNull

class TestDatabaseSmokeTest {

    private val handle = TestDatabase.create()
    private val db = handle.db
    private val driver = handle.driver

    @AfterTest
    fun tearDown() = driver.close()

    @Test
    fun `empty database — selectById returns null`() {
        val result = db.inspectionQueries.selectById("nope").executeAsOneOrNull()
        assertNull(result)
    }
}
