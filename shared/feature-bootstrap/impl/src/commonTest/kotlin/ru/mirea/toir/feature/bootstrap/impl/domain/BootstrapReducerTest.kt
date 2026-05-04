package ru.mirea.toir.feature.bootstrap.impl.domain

import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BootstrapReducerTest {
    private val reducer = BootstrapReducer()
    private val initial = BootstrapStore.State()

    @Test
    fun `SetLoading sets isLoading true`() {
        val result = with(reducer) { initial.reduce(BootstrapStoreFactory.Message.SetLoading) }
        assertTrue(result.isLoading)
        assertFalse(result.isError)
    }

    @Test
    fun `SetError sets isError true and clears loading`() {
        val loading = initial.copy(isLoading = true)
        val result = with(reducer) { loading.reduce(BootstrapStoreFactory.Message.SetError) }
        assertFalse(result.isLoading)
        assertTrue(result.isError)
    }
}
