package ru.mirea.toir.feature.bootstrap.impl.domain

import com.arkivanov.mvikotlin.core.rx.observer
import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.Label
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapResult
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BootstrapExecutorTest {

    @BeforeTest
    fun disableMainThreadAssertions() {
        isAssertOnMainThreadEnabled = false
    }

    @Test
    fun `when bootstrap returns Unauthorized — executor calls logout and publishes NavigateToLogin`() =
        runTest {
            val repository = FakeBootstrapRepository(nextResult = BootstrapResult.Unauthorized)
            val authRepository = FakeAuthRepository()
            val labels = mutableListOf<Label>()
            var syncTriggered = 0

            val store = BootstrapStoreFactory(
                storeFactory = TestStoreFactory(),
                bootstrapRepository = repository,
                authRepository = authRepository,
                triggerBackgroundSync = { syncTriggered++ },
                mainDispatcher = Dispatchers.Unconfined,
            ).create()

            try {
                store.labels(observer(onNext = { label: Label -> labels += label }))
                store.init()

                assertEquals(listOf<Label>(Label.NavigateToLogin), labels)
                assertEquals(1, authRepository.logoutCallCount)
                assertEquals(0, syncTriggered)
            } finally {
                store.dispose()
            }
        }

    @Test
    fun `when bootstrap returns Success — publishes NavigateToRoutesList and triggers sync`() = runTest {
        val repository = FakeBootstrapRepository(nextResult = BootstrapResult.Success)
        val authRepository = FakeAuthRepository()
        val labels = mutableListOf<Label>()
        var syncTriggered = 0

        val store = BootstrapStoreFactory(
            storeFactory = TestStoreFactory(),
            bootstrapRepository = repository,
            authRepository = authRepository,
            triggerBackgroundSync = { syncTriggered++ },
            mainDispatcher = Dispatchers.Unconfined,
        ).create()

        try {
            store.labels(observer(onNext = { label: Label -> labels += label }))
            store.init()

            assertEquals(listOf<Label>(Label.NavigateToRoutesList), labels)
            assertEquals(1, syncTriggered)
        } finally {
            store.dispose()
        }
    }

    @Test
    fun `when bootstrap returns Failure — executor sets error state and does not navigate`() = runTest {
        val repository = FakeBootstrapRepository(
            nextResult = BootstrapResult.Failure(RuntimeException("boom")),
        )
        val authRepository = FakeAuthRepository()
        val labels = mutableListOf<Label>()
        var syncTriggered = 0

        val store = BootstrapStoreFactory(
            storeFactory = TestStoreFactory(),
            bootstrapRepository = repository,
            authRepository = authRepository,
            triggerBackgroundSync = { syncTriggered++ },
            mainDispatcher = Dispatchers.Unconfined,
        ).create()

        try {
            store.labels(observer(onNext = { label: Label -> labels += label }))
            store.init()

            assertTrue(store.state.isError)
            assertEquals(false, store.state.isLoading)
            assertEquals(emptyList<Label>(), labels)
            assertEquals(0, syncTriggered)
        } finally {
            store.dispose()
        }
    }

    @Test
    fun `when bootstrap returns Unauthorized but logout fails — executor still publishes NavigateToLogin`() =
        runTest {
            val repository = FakeBootstrapRepository(nextResult = BootstrapResult.Unauthorized)
            val authRepository = FakeAuthRepository(logoutShouldThrow = true)
            val labels = mutableListOf<Label>()
            var syncTriggered = 0

            val store = BootstrapStoreFactory(
                storeFactory = TestStoreFactory(),
                bootstrapRepository = repository,
                authRepository = authRepository,
                triggerBackgroundSync = { syncTriggered++ },
                mainDispatcher = Dispatchers.Unconfined,
            ).create()

            try {
                store.labels(observer(onNext = { label: Label -> labels += label }))
                store.init()

                assertEquals(listOf<Label>(Label.NavigateToLogin), labels)
                assertEquals(1, authRepository.logoutCallCount)
                assertEquals(false, store.state.isLoading)
                assertEquals(0, syncTriggered)
            } finally {
                store.dispose()
            }
        }
}
