# Waypoint 02 — Feature Auth (Авторизация)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Реализовать экран авторизации, хранение JWT-токенов, Ktor Bearer-перехватчик и auto-refresh токена.

**Architecture:** 4-модульная feature (`api/impl/presentation/ui`). Токены хранятся в DataStore (уже есть `core-storage`). `deviceCode` — UUID, генерируется один раз и хранится в DataStore. Ktor-плагин `Auth` добавляется в `core-network` для автоматической подстановки Bearer.

**Tech Stack:** MVIKotlin, Koin, Ktor Auth plugin, DataStore, Compose Multiplatform.

---

## Файловая структура

```
shared/core-network/src/commonMain/.../ktor/
├── KtorClientImpl.kt                         # MODIFY — добавить bearer auth plugin
└── NetworkEnvironment.kt                     # MODIFY — обновить URL тоир-бэкенда

shared/feature-auth/
├── api/
│   ├── build.gradle.kts
│   └── src/commonMain/.../feature/auth/api/
│       ├── store/AuthStore.kt
│       └── models/DomainAuthUser.kt
├── impl/
│   ├── build.gradle.kts
│   └── src/commonMain/.../feature/auth/impl/
│       ├── di/FeatureAuthImplModule.kt
│       ├── data/network/AuthApiClient.kt
│       ├── data/network/AuthApiClientImpl.kt
│       ├── data/network/models/RemoteLoginRequest.kt
│       ├── data/network/models/RemoteLoginResponse.kt
│       ├── data/storage/TokenStorage.kt
│       ├── data/storage/TokenStorageImpl.kt
│       ├── data/repository/AuthRepositoryImpl.kt
│       ├── data/mappers/AuthUserMapper.kt
│       ├── domain/repository/AuthRepository.kt    ← интерфейс в domain
│       └── domain/AuthStoreFactory.kt   (+ AuthExecutor, AuthReducer)
├── presentation/
│   ├── build.gradle.kts
│   └── src/commonMain/.../feature/auth/presentation/
│       ├── di/FeatureAuthPresentationModule.kt
│       ├── models/UiAuthState.kt
│       ├── models/UiAuthLabel.kt
│       ├── mappers/UiAuthStateMapper.kt
│       ├── mappers/UiAuthLabelMapper.kt
│       └── AuthViewModel.kt
└── ui/
    ├── build.gradle.kts
    └── src/commonMain/.../feature/auth/ui/
        ├── api/FeatureAuthScreenApi.kt
        └── LoginScreen.kt
```

---

### Task 1: Обновить NetworkEnvironment и создать структуру модуля

**Files:**
- Modify: `shared/core-network/src/commonMain/kotlin/ru/mirea/toir/core/network/ktor/NetworkEnvironment.kt`
- Modify: `settings.gradle.kts`

- [ ] **Step 1: Обновить NetworkEnvironment**

Заменить `NetworkEnvironment.kt`:
```kotlin
package ru.mirea.toir.core.network.ktor

enum class NetworkEnvironment(
    val apiHost: String,
) {
    Dev(apiHost = "10.0.2.2:8080"),
    Prod(apiHost = "toir-backend.example.com"),
}
```

> ⚠️ `10.0.2.2` — адрес хоста из Android-эмулятора. При использовании реального устройства/бэкенда в той же сети — поменяй на IP машины.

- [ ] **Step 2: Зарегистрировать модули в settings.gradle.kts**

Добавить после `// Shared feature modules`:
```kotlin
include(":shared:feature-auth:api")
include(":shared:feature-auth:impl")
include(":shared:feature-auth:presentation")
include(":shared:feature-auth:ui")
```

- [ ] **Step 3: Создать директории модулей**

```bash
mkdir -p shared/feature-auth/api/src/commonMain/kotlin/ru/mirea/toir/feature/auth/api/store
mkdir -p shared/feature-auth/api/src/commonMain/kotlin/ru/mirea/toir/feature/auth/api/models
mkdir -p shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/di
mkdir -p shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/data/network/models
mkdir -p shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/data/storage
mkdir -p shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/data/repository
mkdir -p shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/data/mappers
mkdir -p shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/domain
mkdir -p shared/feature-auth/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/auth/presentation/di
mkdir -p shared/feature-auth/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/auth/presentation/models
mkdir -p shared/feature-auth/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/auth/presentation/mappers
mkdir -p shared/feature-auth/ui/src/commonMain/kotlin/ru/mirea/toir/feature/auth/ui/api
```

---

### Task 2: Создать build.gradle.kts для каждого субмодуля

**Files:**
- Create: `shared/feature-auth/api/build.gradle.kts`
- Create: `shared/feature-auth/impl/build.gradle.kts`
- Create: `shared/feature-auth/presentation/build.gradle.kts`
- Create: `shared/feature-auth/ui/build.gradle.kts`

- [ ] **Step 1: api/build.gradle.kts**

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.auth.api"
}
```

- [ ] **Step 2: impl/build.gradle.kts**

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.auth.impl"
}

commonMainDependencies {
    implementations(
        projects.shared.coreNetwork,
        projects.shared.coreStorage,
        projects.shared.coreDatabase,
    )
}
```

- [ ] **Step 3: presentation/build.gradle.kts**

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.auth.presentation"
}
```

- [ ] **Step 4: ui/build.gradle.kts**

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.auth.ui"
}
```

---

### Task 3: Store (api-модуль)

**Files:**
- Create: `shared/feature-auth/api/src/commonMain/kotlin/ru/mirea/toir/feature/auth/api/models/DomainAuthUser.kt`
- Create: `shared/feature-auth/api/src/commonMain/kotlin/ru/mirea/toir/feature/auth/api/store/AuthStore.kt`

- [ ] **Step 1: Создать DomainAuthUser**

```kotlin
package ru.mirea.toir.feature.auth.api.models

data class DomainAuthUser(
    val id: String,
    val displayName: String,
    val role: String,
)
```

- [ ] **Step 2: Создать AuthStore**

```kotlin
package ru.mirea.toir.feature.auth.api.store

import com.arkivanov.mvikotlin.core.store.Store

interface AuthStore : Store<AuthStore.Intent, AuthStore.State, AuthStore.Label> {

    data class State(
        val login: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data class OnLoginChange(val value: String) : Intent
        data class OnPasswordChange(val value: String) : Intent
        data object OnLoginClick : Intent
    }

    sealed interface Label {
        data object NavigateToMain : Label
    }
}
```

---

### Task 4: DTO и API (impl-модуль)

**Files:**
- Create: `shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/data/network/models/RemoteLoginRequest.kt`
- Create: `shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/data/network/models/RemoteLoginResponse.kt`
- Create: `shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/data/network/AuthApiClient.kt`
- Create: `shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/data/network/AuthApiClientImpl.kt`

> ⚠️ Поля DTO взяты из openapi/documentation.yaml (`LoginRequest`, `LoginResponse`). Перед реализацией сверь поля с актуальным YAML.

- [ ] **Step 1: Создать RemoteLoginRequest**

```kotlin
package ru.mirea.toir.feature.auth.impl.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteLoginRequest(
    @SerialName("login") val login: String,
    @SerialName("password") val password: String,
    @SerialName("deviceCode") val deviceCode: String,
    @SerialName("platform") val platform: String?,
    @SerialName("appVersion") val appVersion: String?,
)
```

- [ ] **Step 2: Создать RemoteLoginResponse**

```kotlin
package ru.mirea.toir.feature.auth.impl.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteLoginResponse(
    @SerialName("accessToken") val accessToken: String?,
    @SerialName("refreshToken") val refreshToken: String?,
    @SerialName("user") val user: RemoteUser?,
    @SerialName("device") val device: RemoteDevice?,
)

@Serializable
internal data class RemoteUser(
    @SerialName("id") val id: String?,
    @SerialName("displayName") val displayName: String?,
    @SerialName("role") val role: String?,
)

@Serializable
internal data class RemoteDevice(
    @SerialName("id") val id: String?,
    @SerialName("deviceCode") val deviceCode: String?,
)

@Serializable
internal data class RemoteRefreshRequest(
    @SerialName("refreshToken") val refreshToken: String,
)

@Serializable
internal data class RemoteRefreshResponse(
    @SerialName("accessToken") val accessToken: String?,
    @SerialName("refreshToken") val refreshToken: String?,
)
```

- [ ] **Step 3: Создать AuthApiClient**

```kotlin
package ru.mirea.toir.feature.auth.impl.data.network

import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteLoginRequest
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteLoginResponse
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteRefreshResponse

internal interface AuthApiClient {
    suspend fun login(request: RemoteLoginRequest): Result<RemoteLoginResponse>
    suspend fun refresh(refreshToken: String): Result<RemoteRefreshResponse>
}
```

- [ ] **Step 4: Создать AuthApiClientImpl**

```kotlin
package ru.mirea.toir.feature.auth.impl.data.network

import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ru.mirea.toir.core.network.ktor.KtorClient
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteLoginRequest
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteLoginResponse
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteRefreshRequest
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteRefreshResponse

internal class AuthApiClientImpl(
    private val ktorClient: KtorClient,
) : AuthApiClient {

    override suspend fun login(request: RemoteLoginRequest): Result<RemoteLoginResponse> =
        ktorClient.executeQuery(
            query = {
                ktorClient.post("/api/v1/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            },
            deserializer = RemoteLoginResponse.serializer(),
            success = { it.wrapResultSuccess() },
            loggingErrorMessage = "auth login failed",
        )

    override suspend fun refresh(refreshToken: String): Result<RemoteRefreshResponse> =
        ktorClient.executeQuery(
            query = {
                ktorClient.post("/api/v1/auth/refresh") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoteRefreshRequest(refreshToken = refreshToken))
                }
            },
            deserializer = RemoteRefreshResponse.serializer(),
            success = { it.wrapResultSuccess() },
            loggingErrorMessage = "auth refresh failed",
        )
}
```

---

### Task 5: TokenStorage (impl-модуль)

**Files:**
- Create: `shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/data/storage/TokenStorage.kt`
- Create: `shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/data/storage/TokenStorageImpl.kt`

- [ ] **Step 1: Создать TokenStorage**

```kotlin
package ru.mirea.toir.feature.auth.impl.data.storage

internal interface TokenStorage {
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
    suspend fun getOrCreateDeviceCode(): String
}
```

- [ ] **Step 2: Создать TokenStorageImpl**

```kotlin
package ru.mirea.toir.feature.auth.impl.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class TokenStorageImpl(
    private val dataStore: DataStore<Preferences>,
) : TokenStorage {

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    override suspend fun getAccessToken(): String? =
        dataStore.data.map { it[KEY_ACCESS_TOKEN] }.firstOrNull()

    override suspend fun getRefreshToken(): String? =
        dataStore.data.map { it[KEY_REFRESH_TOKEN] }.firstOrNull()

    override suspend fun clearTokens() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getOrCreateDeviceCode(): String {
        val existing = dataStore.data.map { it[KEY_DEVICE_CODE] }.firstOrNull()
        if (existing != null) return existing
        val newCode = Uuid.random().toString()
        dataStore.edit { it[KEY_DEVICE_CODE] = newCode }
        return newCode
    }

    private companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("auth_access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("auth_refresh_token")
        val KEY_DEVICE_CODE = stringPreferencesKey("auth_device_code")
    }
}
```

---

### Task 6: AuthRepository (impl-модуль)

**Files:**
- Create: `shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/data/mappers/AuthUserMapper.kt`
- Create: `shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/domain/repository/AuthRepository.kt`
- Create: `shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/data/repository/AuthRepositoryImpl.kt`

- [ ] **Step 1: Создать AuthUserMapper**

```kotlin
package ru.mirea.toir.feature.auth.impl.data.mappers

import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.auth.api.models.DomainAuthUser
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteUser

internal interface AuthUserMapper : Mapper<RemoteUser, DomainAuthUser>

internal class AuthUserMapperImpl : AuthUserMapper {
    override fun map(item: RemoteUser): DomainAuthUser = DomainAuthUser(
        id = item.id.orEmpty(),
        displayName = item.displayName.orEmpty(),
        role = item.role.orEmpty(),
    )
}
```

- [ ] **Step 2: Создать AuthRepository (в domain/repository)**

```kotlin
package ru.mirea.toir.feature.auth.impl.domain.repository

import ru.mirea.toir.feature.auth.api.models.DomainAuthUser

internal interface AuthRepository {
    suspend fun login(login: String, password: String): Result<DomainAuthUser>
    suspend fun getStoredAccessToken(): String?
    suspend fun refreshAccessToken(): Result<Unit>
    suspend fun logout()
}
```

- [ ] **Step 3: Создать AuthRepositoryImpl (в data/repository)**

```kotlin
package ru.mirea.toir.feature.auth.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.feature.auth.api.models.DomainAuthUser
import ru.mirea.toir.feature.auth.impl.data.mappers.AuthUserMapper
import ru.mirea.toir.feature.auth.impl.data.network.AuthApiClient
import ru.mirea.toir.feature.auth.impl.data.network.models.RemoteLoginRequest
import ru.mirea.toir.feature.auth.impl.data.storage.TokenStorage
import ru.mirea.toir.feature.auth.impl.domain.repository.AuthRepository

internal class AuthRepositoryImpl(
    private val apiClient: AuthApiClient,
    private val tokenStorage: TokenStorage,
    private val userMapper: AuthUserMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
) : AuthRepository {

    override suspend fun login(login: String, password: String): Result<DomainAuthUser> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val deviceCode = tokenStorage.getOrCreateDeviceCode()
                    val response = apiClient.login(
                        RemoteLoginRequest(
                            login = login,
                            password = password,
                            deviceCode = deviceCode,
                            platform = "android",
                            appVersion = null,
                        )
                    ).getOrThrow()

                    tokenStorage.saveTokens(
                        accessToken = response.accessToken.orEmpty(),
                        refreshToken = response.refreshToken.orEmpty(),
                    )

                    userMapper.map(response.user ?: error("No user in login response")).wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "login failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override suspend fun getStoredAccessToken(): String? =
        withContext(coroutineDispatchers.io) {
            tokenStorage.getAccessToken()
        }

    override suspend fun refreshAccessToken(): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val refreshToken = tokenStorage.getRefreshToken()
                        ?: error("No refresh token stored")
                    val response = apiClient.refresh(refreshToken).getOrThrow()
                    tokenStorage.saveTokens(
                        accessToken = response.accessToken.orEmpty(),
                        refreshToken = response.refreshToken.orEmpty(),
                    )
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "refreshAccessToken failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override suspend fun logout() {
        withContext(coroutineDispatchers.io) { tokenStorage.clearTokens() }
    }
}
```

---

### Task 7: Executor, Reducer, StoreFactory (impl-модуль)

**Files:**
- Create: `shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/domain/AuthExecutor.kt`
- Create: `shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/domain/AuthReducer.kt`
- Create: `shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/domain/AuthStoreFactory.kt`

- [ ] **Step 1: Создать AuthReducer**

```kotlin
package ru.mirea.toir.feature.auth.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.auth.api.store.AuthStore

internal class AuthReducer : Reducer<AuthStore.State, AuthStoreFactory.Message> {

    override fun AuthStore.State.reduce(msg: AuthStoreFactory.Message): AuthStore.State = when (msg) {
        is AuthStoreFactory.Message.SetLogin -> copy(login = msg.value)
        is AuthStoreFactory.Message.SetPassword -> copy(password = msg.value)
        AuthStoreFactory.Message.SetLoading -> copy(isLoading = true, errorMessage = null)
        is AuthStoreFactory.Message.SetError -> copy(isLoading = false, errorMessage = msg.message)
        AuthStoreFactory.Message.ClearLoading -> copy(isLoading = false)
    }
}
```

- [ ] **Step 2: Написать тест для AuthReducer**

Создать `shared/feature-auth/impl/src/commonTest/kotlin/ru/mirea/toir/feature/auth/impl/domain/AuthReducerTest.kt`:
```kotlin
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
```

- [ ] **Step 3: Запустить тест — убедиться, что он падает (AuthReducer ещё не полностью написан)**

```bash
./gradlew :shared:feature-auth:impl:testDebugUnitTest 2>&1 | tail -20
```

- [ ] **Step 4: Создать AuthExecutor**

```kotlin
package ru.mirea.toir.feature.auth.impl.domain

import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.auth.api.store.AuthStore
import ru.mirea.toir.feature.auth.impl.domain.repository.AuthRepository

internal class AuthExecutor(
    private val authRepository: AuthRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<AuthStore.Intent, Nothing, AuthStore.State, AuthStoreFactory.Message, AuthStore.Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteIntent(intent: AuthStore.Intent) {
        when (intent) {
            is AuthStore.Intent.OnLoginChange -> dispatch(AuthStoreFactory.Message.SetLogin(intent.value))
            is AuthStore.Intent.OnPasswordChange -> dispatch(AuthStoreFactory.Message.SetPassword(intent.value))
            AuthStore.Intent.OnLoginClick -> handleLogin()
        }
    }

    private suspend fun handleLogin() {
        val currentState = state()
        if (currentState.isLoading) return
        dispatch(AuthStoreFactory.Message.SetLoading)

        authRepository.login(
            login = currentState.login,
            password = currentState.password,
        ).fold(
            onSuccess = {
                dispatch(AuthStoreFactory.Message.ClearLoading)
                publish(AuthStore.Label.NavigateToMain)
            },
            onFailure = { throwable ->
                dispatch(AuthStoreFactory.Message.SetError(throwable.message ?: "Ошибка авторизации"))
            },
        )
    }
}
```

- [ ] **Step 5: Создать AuthStoreFactory**

```kotlin
package ru.mirea.toir.feature.auth.impl.domain

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.auth.api.store.AuthStore
import ru.mirea.toir.feature.auth.impl.domain.repository.AuthRepository

internal class AuthStoreFactory(
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository,
    private val mainDispatcher: CoroutineDispatcher,
) {

    fun create(): AuthStore =
        object :
            AuthStore,
            Store<AuthStore.Intent, AuthStore.State, AuthStore.Label> by storeFactory.create(
                name = AuthStore::class.simpleName,
                initialState = AuthStore.State(),
                bootstrapper = null,
                executorFactory = { AuthExecutor(authRepository, mainDispatcher) },
                reducer = AuthReducer(),
            ) {}

    internal sealed interface Message {
        data class SetLogin(val value: String) : Message
        data class SetPassword(val value: String) : Message
        data object SetLoading : Message
        data class SetError(val message: String) : Message
        data object ClearLoading : Message
    }
}
```

- [ ] **Step 6: Запустить тест — убедиться, что он проходит**

```bash
./gradlew :shared:feature-auth:impl:testDebugUnitTest
```
Ожидаемый результат: `BUILD SUCCESSFUL`, все тесты зелёные.

---

### Task 8: DI (impl-модуль)

**Files:**
- Create: `shared/feature-auth/impl/src/commonMain/kotlin/ru/mirea/toir/feature/auth/impl/di/FeatureAuthImplModule.kt`

- [ ] **Step 1: Создать FeatureAuthImplModule**

```kotlin
package ru.mirea.toir.feature.auth.impl.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.feature.auth.impl.data.mappers.AuthUserMapper
import ru.mirea.toir.feature.auth.impl.data.mappers.AuthUserMapperImpl
import ru.mirea.toir.feature.auth.impl.data.network.AuthApiClient
import ru.mirea.toir.feature.auth.impl.data.network.AuthApiClientImpl
import ru.mirea.toir.feature.auth.impl.domain.repository.AuthRepository
import ru.mirea.toir.feature.auth.impl.data.repository.AuthRepositoryImpl
import ru.mirea.toir.feature.auth.impl.data.storage.TokenStorage
import ru.mirea.toir.feature.auth.impl.data.storage.TokenStorageImpl
import ru.mirea.toir.feature.auth.impl.domain.AuthStoreFactory

val featureAuthImplModule = module {
    factory<AuthApiClient> { new(::AuthApiClientImpl) }
    factory<TokenStorage> { new(::TokenStorageImpl) }
    factory<AuthUserMapper> { new(::AuthUserMapperImpl) }
    factory<AuthRepository> { new(::AuthRepositoryImpl) }
    factory { new(::AuthStoreFactory) }
}
```

---

### Task 9: Presentation-модуль (ViewModel + маппинг)

**Files:**
- Create: `shared/feature-auth/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/auth/presentation/models/UiAuthState.kt`
- Create: `shared/feature-auth/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/auth/presentation/models/UiAuthLabel.kt`
- Create: `shared/feature-auth/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/auth/presentation/mappers/UiAuthStateMapper.kt`
- Create: `shared/feature-auth/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/auth/presentation/mappers/UiAuthLabelMapper.kt`
- Create: `shared/feature-auth/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/auth/presentation/AuthViewModel.kt`
- Create: `shared/feature-auth/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/auth/presentation/di/FeatureAuthPresentationModule.kt`

- [ ] **Step 1: Создать UiAuthState**

```kotlin
package ru.mirea.toir.feature.auth.presentation.models

import androidx.compose.runtime.Immutable

@Immutable
data class UiAuthState(
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
```

- [ ] **Step 2: Создать UiAuthLabel**

```kotlin
package ru.mirea.toir.feature.auth.presentation.models

sealed interface UiAuthLabel {
    data object NavigateToMain : UiAuthLabel
}
```

- [ ] **Step 3: Создать UiAuthStateMapper**

```kotlin
package ru.mirea.toir.feature.auth.presentation.mappers

import ru.mirea.toir.feature.auth.api.store.AuthStore
import ru.mirea.toir.feature.auth.presentation.models.UiAuthState

interface UiAuthStateMapper {
    fun map(state: AuthStore.State): UiAuthState
}

internal class UiAuthStateMapperImpl : UiAuthStateMapper {
    override fun map(state: AuthStore.State): UiAuthState = with(state) {
        UiAuthState(
            login = login,
            password = password,
            isLoading = isLoading,
            errorMessage = errorMessage,
        )
    }
}
```

- [ ] **Step 4: Создать UiAuthLabelMapper**

```kotlin
package ru.mirea.toir.feature.auth.presentation.mappers

import ru.mirea.toir.common.mappers.Mapper
import ru.mirea.toir.feature.auth.api.store.AuthStore
import ru.mirea.toir.feature.auth.presentation.models.UiAuthLabel

interface UiAuthLabelMapper : Mapper<AuthStore.Label, UiAuthLabel>

internal class UiAuthLabelMapperImpl : UiAuthLabelMapper {
    override fun map(item: AuthStore.Label): UiAuthLabel = when (item) {
        AuthStore.Label.NavigateToMain -> UiAuthLabel.NavigateToMain
    }
}
```

- [ ] **Step 5: Создать AuthViewModel**

```kotlin
package ru.mirea.toir.feature.auth.presentation

import ru.mirea.toir.core.presentation.BaseViewModel
import ru.mirea.toir.feature.auth.api.store.AuthStore
import ru.mirea.toir.feature.auth.presentation.mappers.UiAuthLabelMapper
import ru.mirea.toir.feature.auth.presentation.mappers.UiAuthStateMapper
import ru.mirea.toir.feature.auth.presentation.models.UiAuthLabel
import ru.mirea.toir.feature.auth.presentation.models.UiAuthState

class AuthViewModel internal constructor(
    private val store: AuthStore,
    private val stateMapper: UiAuthStateMapper,
    private val labelMapper: UiAuthLabelMapper,
) : BaseViewModel<UiAuthState, UiAuthLabel>(initialState = UiAuthState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onLoginChange(value: String) = store.accept(AuthStore.Intent.OnLoginChange(value))
    fun onPasswordChange(value: String) = store.accept(AuthStore.Intent.OnPasswordChange(value))
    fun onLoginClick() = store.accept(AuthStore.Intent.OnLoginClick)

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
```

- [ ] **Step 6: Создать FeatureAuthPresentationModule**

```kotlin
package ru.mirea.toir.feature.auth.presentation.di

import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.mirea.toir.feature.auth.presentation.AuthViewModel
import ru.mirea.toir.feature.auth.presentation.mappers.UiAuthLabelMapper
import ru.mirea.toir.feature.auth.presentation.mappers.UiAuthLabelMapperImpl
import ru.mirea.toir.feature.auth.presentation.mappers.UiAuthStateMapper
import ru.mirea.toir.feature.auth.presentation.mappers.UiAuthStateMapperImpl

val featureAuthPresentationModule = module {
    factory<UiAuthStateMapper> { new(::UiAuthStateMapperImpl) }
    factory<UiAuthLabelMapper> { new(::UiAuthLabelMapperImpl) }
    viewModelOf(::AuthViewModel)
}
```

---

### Task 10: UI-модуль (LoginScreen)

**Files:**
- Create: `shared/feature-auth/ui/src/commonMain/kotlin/ru/mirea/toir/feature/auth/ui/api/FeatureAuthScreenApi.kt`
- Create: `shared/feature-auth/ui/src/commonMain/kotlin/ru/mirea/toir/feature/auth/ui/LoginScreen.kt`

Добавить строки в `shared/common-resources/src/commonMain/moko-resources/base/strings.xml`:
```xml
<!-- auth -->
<string name="auth_title">Авторизация</string>
<string name="auth_login_hint">Логин</string>
<string name="auth_password_hint">Пароль</string>
<string name="auth_button_login">Войти</string>
<string name="auth_error_invalid_credentials">Неверный логин или пароль</string>
```

- [ ] **Step 1: Создать маршрут в core-navigation**

В `shared/core-navigation/src/commonMain/kotlin/ru/mirea/toir/core/navigation/Screen.kt` добавить:
```kotlin
@Serializable
data object AuthRoute : Screen
```

- [ ] **Step 2: Создать FeatureAuthScreenApi**

```kotlin
package ru.mirea.toir.feature.auth.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.mirea.toir.core.navigation.AuthRoute
import ru.mirea.toir.feature.auth.ui.LoginScreen

fun NavGraphBuilder.composableAuthScreen(
    onNavigateToMain: () -> Unit,
) {
    composable<AuthRoute> {
        LoginScreen(onNavigateToMain = onNavigateToMain)
    }
}
```

- [ ] **Step 3: Создать LoginScreen**

```kotlin
package ru.mirea.toir.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.ext.CollectFlow
import ru.mirea.toir.feature.auth.presentation.AuthViewModel
import ru.mirea.toir.feature.auth.presentation.models.UiAuthLabel
import ru.mirea.toir.res.MR

@Composable
internal fun LoginScreen(
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiAuthLabel.NavigateToMain -> onNavigateToMain()
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(MR.strings.auth_title),
                    style = MaterialTheme.typography.headlineMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.login,
                    onValueChange = viewModel::onLoginChange,
                    label = { Text(text = stringResource(MR.strings.auth_login_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                )

                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text(text = stringResource(MR.strings.auth_password_hint)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { viewModel.onLoginClick() }),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                )

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = viewModel::onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading && state.login.isNotBlank() && state.password.isNotBlank(),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.height(20.dp))
                    } else {
                        Text(text = stringResource(MR.strings.auth_button_login))
                    }
                }
            }
        }
    }
}
```

---

### Task 11: Добавить Bearer-перехватчик в Ktor

**Files:**
- Modify: `shared/core-network/src/commonMain/kotlin/ru/mirea/toir/core/network/ktor/KtorClientImpl.kt`

> Ktor `Auth` plugin автоматически добавляет `Authorization: Bearer <token>` и делает token refresh при 401.
> Это требует зависимости `ktor-auth`. Проверь, есть ли она уже в `libs.versions.toml`.

- [ ] **Step 1: Добавить ktor-auth в libs.versions.toml (если отсутствует)**

В секцию `[libraries]`:
```toml
ktor-auth = { module = "io.ktor:ktor-client-auth", version.ref = "ktor" }
```

- [ ] **Step 2: Добавить ktor-auth в core-network зависимости**

В `shared/core-network/build.gradle.kts` в `commonMainDependencies` добавить:
```kotlin
implementations(libs.ktor.auth)
```

- [ ] **Step 3: Настроить Bearer-перехватчик в HttpClient**

В `KtorClientImpl.kt` или в `CoreNetworkKtorModule.kt` (где создаётся `HttpClient`) добавить плагин `Auth`.
Поскольку `TokenStorage` из `feature-auth` не должна попадать в `core-network` (circular dependency), создай интерфейс-мост:

Создать `shared/core-network/src/commonMain/kotlin/ru/mirea/toir/core/network/auth/TokenProvider.kt`:
```kotlin
package ru.mirea.toir.core.network.auth

interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun refreshAndGetAccessToken(): String?
}
```

Затем в `CoreNetworkKtorModule.kt` при создании `HttpClient` добавить:
```kotlin
install(Auth) {
    bearer {
        loadTokens {
            val token = get<TokenProvider>().getAccessToken()
            BearerTokens(accessToken = token.orEmpty(), refreshToken = "")
        }
        refreshTokens {
            val newToken = get<TokenProvider>().refreshAndGetAccessToken()
            BearerTokens(accessToken = newToken.orEmpty(), refreshToken = "")
        }
    }
}
```

- [ ] **Step 4: Реализовать TokenProvider в feature-auth impl**

В `FeatureAuthImplModule.kt` добавить:
```kotlin
factory<TokenProvider> {
    object : TokenProvider {
        private val storage = get<TokenStorage>()
        private val repository = get<AuthRepository>()
        override suspend fun getAccessToken(): String? = storage.getAccessToken()
        override suspend fun refreshAndGetAccessToken(): String? {
            repository.refreshAccessToken()
            return storage.getAccessToken()
        }
    }
}
```

---

### Task 12: Зарегистрировать модули в Koin и финальная проверка

**Files:**
- Modify: `shared/main/src/commonMain/kotlin/ru/mirea/toir/di/Koin.kt`

- [ ] **Step 1: Добавить auth-модули в initKoin**

```kotlin
import ru.mirea.toir.feature.auth.impl.di.featureAuthImplModule
import ru.mirea.toir.feature.auth.presentation.di.featureAuthPresentationModule

// В modules():
featureAuthImplModule,
featureAuthPresentationModule,
```

- [ ] **Step 2: Собрать и проверить**

```bash
./gradlew :android:app:assembleDebug
```
Ожидаемый результат: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add shared/feature-auth/ shared/core-network/ shared/common-resources/ shared/core-navigation/ settings.gradle.kts shared/main/ gradle/libs.versions.toml
git commit -m "feat: implement auth feature with login screen, JWT storage and Ktor bearer interceptor"
```
