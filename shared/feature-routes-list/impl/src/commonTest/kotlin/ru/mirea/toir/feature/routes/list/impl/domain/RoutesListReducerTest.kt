package ru.mirea.toir.feature.routes.list.impl.domain

import ru.mirea.toir.feature.routes.list.api.models.RoutesListSyncFailure
import ru.mirea.toir.feature.routes.list.api.models.RoutesListSyncIndicator
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import ru.mirea.toir.feature.routes.list.impl.domain.RoutesListStoreFactory.Message
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoutesListReducerTest {
    private val reducer = RoutesListReducer()
    private val initial = RoutesListStore.State()

    @Test
    fun `SetLoading sets isLoading`() {
        val result = with(reducer) { initial.reduce(Message.SetLoading) }
        assertTrue(result.isLoading)
    }

    @Test
    fun `SetError sets isError and clears loading`() {
        val result = with(reducer) {
            initial.copy(isLoading = true).reduce(Message.SetError)
        }
        assertFalse(result.isLoading)
        assertTrue(result.isError)
    }

    @Test
    fun `SetAssignments replaces list and clears loading`() {
        val result = with(reducer) {
            initial.copy(isLoading = true).reduce(Message.SetAssignments(emptyList()))
        }
        assertFalse(result.isLoading)
        assertTrue(result.assignments.isEmpty())
    }

    @Test
    fun `SetSyncIndicator updates only indicator`() {
        val indicator = RoutesListSyncIndicator(
            isRunning = true,
            pendingCount = 3,
            lastError = null,
        )
        val result = with(reducer) {
            initial.copy(isLoading = false).reduce(Message.SetSyncIndicator(indicator))
        }
        assertEquals(indicator, result.syncIndicator)
        assertFalse(result.isLoading)
    }

    @Test
    fun `SetSyncIndicator carries error reason`() {
        val indicator = RoutesListSyncIndicator(
            isRunning = false,
            pendingCount = 1,
            lastError = RoutesListSyncFailure.NETWORK,
        )
        val result = with(reducer) { initial.reduce(Message.SetSyncIndicator(indicator)) }
        assertEquals(RoutesListSyncFailure.NETWORK, result.syncIndicator.lastError)
        assertEquals(1, result.syncIndicator.pendingCount)
    }

    @Test
    fun `SetSyncLastSuccessAt stores value`() {
        val result = with(reducer) {
            initial.reduce(Message.SetSyncLastSuccessAt("2026-05-12T14:32:00Z"))
        }
        assertEquals("2026-05-12T14:32:00Z", result.syncLastSuccessAt)
    }

    @Test
    fun `SetSyncLastFailedAt accepts null`() {
        val result = with(reducer) {
            initial.copy(syncLastFailedAt = "x").reduce(Message.SetSyncLastFailedAt(null))
        }
        assertNull(result.syncLastFailedAt)
    }

    @Test
    fun `SetSyncSheetVisible flips visibility`() {
        val shown = with(reducer) { initial.reduce(Message.SetSyncSheetVisible(true)) }
        assertTrue(shown.isSyncSheetVisible)
        val hidden = with(reducer) { shown.reduce(Message.SetSyncSheetVisible(false)) }
        assertFalse(hidden.isSyncSheetVisible)
    }
}
