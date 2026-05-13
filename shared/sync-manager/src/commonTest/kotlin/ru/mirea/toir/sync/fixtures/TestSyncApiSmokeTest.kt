@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package ru.mirea.toir.sync.fixtures

import kotlinx.coroutines.test.runTest
import ru.mirea.toir.sync.data.network.models.RemoteSyncPushRequest
import kotlin.test.Test
import kotlin.test.assertTrue

class TestSyncApiSmokeTest {

    @Test
    fun `fetchConfigChanges with default stub returns parsed empty response`() = runTest {
        val api = TestSyncApi().build()
        val result = api.fetchConfigChanges(since = "2026-01-01T00:00:00Z")
        assertTrue(result.isSuccess, "Expected success but was $result")
        assertTrue(result.getOrThrow().locations.isEmpty())
    }

    @Test
    fun `pushSync default stub returns parsed empty response`() = runTest {
        val api = TestSyncApi().build()
        val request = RemoteSyncPushRequest(
            clientBatchId = "test-batch",
            deviceId = "test-device",
            sentAt = "2026-05-13T00:00:00Z",
        )
        val result = api.pushSync(request)
        assertTrue(result.isSuccess, "Expected success but was $result")
        val response = result.getOrThrow()
        assertTrue(response.accepted.inspections.isEmpty())
        assertTrue(response.rejected.isEmpty())
    }
}
