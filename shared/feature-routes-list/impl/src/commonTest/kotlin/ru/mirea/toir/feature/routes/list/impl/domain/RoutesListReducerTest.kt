package ru.mirea.toir.feature.routes.list.impl.domain

import kotlinx.collections.immutable.persistentListOf
import ru.mirea.toir.feature.routes.list.api.store.RoutesListStore
import ru.mirea.toir.feature.routes.list.impl.domain.RoutesListStoreFactory.Message
import kotlin.test.Test
import kotlin.test.assertFalse
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
            initial.copy(isLoading = true).reduce(Message.SetAssignments(persistentListOf()))
        }
        assertFalse(result.isLoading)
        assertTrue(result.assignments.isEmpty())
    }
}
