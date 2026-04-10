package ru.mirea.toir.feature.auth.impl.domain

import ru.mirea.toir.feature.auth.api.store.AuthStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthReducerTest {

    private val reducer = AuthReducer()
    private val initialState = AuthStore.State()

    @Test
    fun `SetLogin updates login field`() {
        val result = with(reducer) { initialState.reduce(AuthStoreFactory.Message.SetLogin("user123")) }
        assertEquals("user123", result.login)
    }

    @Test
    fun `SetPassword updates password field`() {
        val result = with(reducer) { initialState.reduce(AuthStoreFactory.Message.SetPassword("pass")) }
        assertEquals("pass", result.password)
    }

    @Test
    fun `SetLoading sets isLoading true and clears error`() {
        val stateWithError = initialState.copy(errorMessage = "some error")
        val result = with(reducer) { stateWithError.reduce(AuthStoreFactory.Message.SetLoading) }
        assertTrue(result.isLoading)
        assertNull(result.errorMessage)
    }

    @Test
    fun `SetError sets error message and clears loading`() {
        val loading = initialState.copy(isLoading = true)
        val result = with(reducer) { loading.reduce(AuthStoreFactory.Message.SetError("Неверный логин или пароль")) }
        assertFalse(result.isLoading)
        assertEquals("Неверный логин или пароль", result.errorMessage)
    }
}
