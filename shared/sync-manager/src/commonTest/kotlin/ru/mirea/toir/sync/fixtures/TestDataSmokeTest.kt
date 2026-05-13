package ru.mirea.toir.sync.fixtures

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import ru.mirea.toir.sync.fixtures.TestData.seedFullPendingScenario

class TestDataSmokeTest {

    private val handle = TestDatabase.create()
    private val db = handle.db
    private val driver = handle.driver

    @AfterTest fun tearDown() = driver.close()

    @Test
    fun `seedFullPendingScenario persists inspection`() {
        db.seedFullPendingScenario()
        val ins = db.inspectionQueries.selectById(TestData.INSPECTION_ID).executeAsOne()
        assertEquals(TestData.INSPECTION_ID, ins.id)
    }
}
