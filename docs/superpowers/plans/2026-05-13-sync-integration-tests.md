# Sync Integration Tests Implementation Plan (revised — path A: commonTest + iOS native runtime)

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Закрыть регресс sync-сценария интеграционными тестами sync-manager. Тесты живут в `commonTest`, гоняются на iOS-симуляторе как `./gradlew :shared:sync-manager:iosSimulatorArm64Test`. Это заменяет ручной UI-регресс и не требует никаких изменений в `build-logic`.

**Architecture:** Этот KMP-проект использует AGP 9 `com.android.kotlin.multiplatform.library` plugin — у него нет Android unit test source set, и JVM target в проекте сознательно отсутствует. Единственный runtime для unit-тестов сейчас — iOS native (через `:iosSimulatorArm64Test` / `:iosX64Test`). Тесты пишем в `src/commonTest/kotlin/...`. Платформо-зависимый кусок — создание SqlDriver — выносим в `expect` функцию в commonTest и actual только под iOS. SQLDelight native-driver поддерживает in-memory через `DatabaseConfiguration.inMemory = true`. Ktor `MockEngine` — multiplatform, заводится в commonTest без actual'ов. Боевой `SyncApiClientImpl` + `HttpClient(MockEngine)` проверяет реальную сериализацию JSON, а не fake интерфейс. Без mockk/turbine/kotest — только `kotlin-test` + `kotlinx-coroutines-test` + простые fakes.

**Tech Stack:**
- `kotlin-test` + `kotlinx-coroutines-test` (есть в каталоге)
- SQLDelight 2.3.2 native-driver (`libs.sqldelight.native`) — уже в `iosMain` core-database, нам нужен только в commonTest sync-manager
- Ktor 3.4.2 + `ktor-client-mock` (`libs.ktor.clientMock`) — multiplatform, уже добавлен в каталог
- `ToirDatabase.Schema` создаётся автоматически в `NativeSqliteDriver` при первом запросе

**Test runner command:** `./gradlew :shared:sync-manager:iosSimulatorArm64Test --tests "..."`

---

## Scope

**Покрываем (regression):**
- `ConfigChangesApplier.apply` — happy, empty, deleted ids
- `SyncRepositoryImpl.pushPendingData` — happy / rejected / empty / HTTP 500
- `SyncRepositoryImpl.fetchAndApplyDeltaChanges` — happy + 500
- `SyncRepositoryImpl.uploadPendingPhotos` — happy + fail
- `SyncRepositoryImpl.recordSuccessfulRun` / `recordFailedRun`
- `SyncRepositoryImpl.observeHasPending` — Flow эмитит после insert
- **(опционально)** `SyncManager.runOnce` orchestration

**НЕ покрываем:**
- Тесты feature-репозиториев — отдельный план
- `SyncWorker`/`SyncScheduler` (WorkManager — нужен Robolectric)
- `BackoffPolicy` — уже покрыт
- Мапперы — уже покрыты
- iOS-native UI / iOS-specific behavior — не задача этого слоя

---

## Test Architecture Decision

**Почему `commonTest` + iOS native, а не JVM:**
- Проект использует AGP 9 KMP plugin. У него нет `androidUnitTest` source set и Gradle-таска `compileDebugUnitTestKotlinAndroid` / `testDebugUnitTest`. Существующие 4 теста в `commonTest` (мапперы, `BackoffPolicyTest`) гоняются ТОЛЬКО как `:iosSimulatorArm64Test` / `:iosX64Test` — иначе их и не запустить.
- Добавление `jvm()` target — серьёзная инвазивная правка: для каждого `expect class` в `commonMain` (DatabaseDriverFactory, FileReader, NetworkMonitor и т.д.) пришлось бы писать JVM `actual`. Это «JVM-actual мусор ради тестов».
- iOS native runtime на симуляторе медленнее JVM (cold-boot до минуты, потом 5-15с на прогон) — но это не критично: тесты гоняются перед коммитом руками, не в TDD-цикле каждые 30 секунд.
- Все нужные либы (sqldelight-native, ktor-client-mock) уже multiplatform.

**Почему `NativeSqliteDriver(inMemory = true)`:**
- Не пишет на диск симулятора → нет грязного состояния между тестами.
- Schema создаётся автоматически при первом запросе.
- API SQLDelight 2.3.2 (точная сигнатура подтверждается чтением `app.cash.sqldelight.driver.native.NativeSqliteDriver` — параметр `onConfiguration: (DatabaseConfiguration) -> DatabaseConfiguration` принимает копию с `inMemory = true`).

**Почему боевой `SyncApiClientImpl` + `MockEngine`, а не fake `SyncApiClient`:**
- Покрываем сериализацию/десериализацию JSON.
- Маппинг HTTP-ошибок в `Result.failure` — реальный код.

**Почему `expect` функция для драйвера в commonTest, а не в commonMain:**
- Не загрязняем production API тестовыми вещами.
- Production уже имеет свой `DatabaseDriverFactory` (`commonMain/expect class`) — у него отдельная задача и иной контракт. Тестовая фабрика — отдельная сущность.

---

## File Structure

**Изменения и новые файлы:**

```
gradle/libs.versions.toml                       [edit] +ktor-clientMock (уже добавлено)

shared/sync-manager/
├── build.gradle.kts                            [edit] +commonTest deps (ktor-clientMock, content-negotiation, serialization-json, coroutines-test, core-database, core-network)
├── src/commonTest/kotlin/ru/mirea/toir/sync/
│   ├── fixtures/
│   │   ├── TestDatabaseDriverFactory.kt        [new] expect fun createInMemoryDriver(): SqlDriver
│   │   ├── TestDatabase.kt                     [new] фабрика ToirDatabase с column adapters
│   │   ├── TestSyncApi.kt                      [new] MockEngine builder + SyncApiClientImpl wiring
│   │   ├── TestData.kt                         [new] data builders
│   │   ├── TestDispatchers.kt                  [new] CoroutineDispatchers через UnconfinedTestDispatcher
│   │   └── TestFileReader.kt                   [new] FakeFileReader
│   ├── data/applier/
│   │   ├── ConfigChangesApplierHappyPathTest.kt [new]
│   │   └── ConfigChangesApplierPartialTest.kt   [new]
│   └── data/repository/
│       ├── SyncRepositoryPushTest.kt           [new]
│       ├── SyncRepositoryFetchTest.kt          [new]
│       ├── SyncRepositoryPhotoTest.kt          [new]
│       ├── SyncRepositoryMetaTest.kt           [new]
│       └── SyncRepositoryObserveTest.kt        [new]
└── src/iosTest/kotlin/ru/mirea/toir/sync/
    └── fixtures/
        └── TestDatabaseDriverFactory.ios.kt    [new] actual NativeSqliteDriver(inMemory=true)
```

**Опционально (Task 13):**
```
shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/domain/
└── SyncManagerOrchestrationTest.kt             [new]
```

---

### Task 1: Add ktor-client-mock to sync-manager commonTest deps

**Files:**
- Modify: `shared/sync-manager/build.gradle.kts`

`gradle/libs.versions.toml` уже содержит `ktor-clientMock` (добавлено в более ранней попытке). `sqldelight-native` уже в каталоге (`libs.sqldelight.native`). `kotlin-test`, `kotlin-testJunit`, `kotlin-coroutines-test`, `ktor-contentNegotiation`, `ktor-serializationJson` уже в каталоге.

DSL-helper `commonTestDependencies` уже существует в `build-logic/src/main/kotlin/extensions/KmpDependenciesExtensions.kt` — переиспользуем.

- [ ] **Step 1: Add commonTestDependencies block to sync-manager build.gradle.kts**

Append AFTER existing `androidMainDependencies { ... }` block (ends around L29):

```kotlin
commonTestDependencies {
    implementations(
        libs.kotlin.test,
        libs.kotlin.coroutines.test,
        libs.ktor.clientMock,
        libs.ktor.contentNegotiation,
        libs.ktor.serializationJson,
        libs.sqldelight.native,
        projects.shared.coreDatabase,
        projects.shared.coreNetwork,
    )
}
```

Add the import at top of the file (next to existing `extensions.*` imports):

```kotlin
import extensions.commonTestDependencies
```

If `commonTestDependencies` import is already covered by a wildcard, skip — but the existing file uses explicit imports per function. Add it explicitly.

**Important:** project `sync-manager` already depends on `core-database` and `core-network` via `commonMain` (line 17-21 of `build.gradle.kts`). The explicit `commonTest` re-declaration is for transitive visibility in tests — but if test compilation works without it, remove the two `projects.shared.*` lines and keep deps minimal. Confirm by trying compilation both ways; prefer minimal.

- [ ] **Step 2: Verify compilation**

Run from the working dir (`/Users/a.dobrov/StudioProjects/toir-mobile`):

```bash
./gradlew :shared:sync-manager:compileKotlinIosSimulatorArm64 :shared:sync-manager:compileTestKotlinIosSimulatorArm64
```

Expected: `BUILD SUCCESSFUL`. No test code yet — this confirms wiring resolves.

If `compileTestKotlinIosSimulatorArm64` doesn't exist, run `./gradlew :shared:sync-manager:tasks --all | grep -i "Test"` to discover the correct task name (KMP target naming sometimes uses `iosSimulatorArm64Test` for the run task and `compileTestKotlinIosSimulatorArm64` for compile-only; the run task implicitly compiles).

- [ ] **Step 3: Commit**

```bash
git add gradle/libs.versions.toml shared/sync-manager/build.gradle.kts
git commit -m "test(sync): add commonTest deps for integration tests (ktor mock + sqldelight native)"
```

---

### Task 2: TestDatabaseDriverFactory (expect/actual) + TestDatabase factory

**Files:**
- Create: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/TestDatabaseDriverFactory.kt`
- Create: `shared/sync-manager/src/iosTest/kotlin/ru/mirea/toir/sync/fixtures/TestDatabaseDriverFactory.ios.kt`
- Create: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/TestDatabase.kt`

**Goal:** `TestDatabase.create()` returns a fresh `ToirDatabase` backed by in-memory SQLite. iOS-only actual; production code untouched.

- [ ] **Step 1: Inspect production ToirDatabase wiring**

Read: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/di/CoreDatabaseModule.kt`

Copy the exact `ToirDatabase(driver, …Adapter = …)` invocation, including all column adapter parameter names. These typically use `EnumColumnAdapter` for `LocalInspectionStatus`, `LocalSyncStatus`, `LocalBatchStatus`, `LocalRouteAssignmentStatus`, `LocalRejectionReason`, `LocalEquipmentResultStatus`. Identify the exact ctor parameter names from `shared/core-database/build/generated/sqldelight/code/ToirDatabase/.../ToirDatabaseImpl.kt` if needed.

- [ ] **Step 2: Create expect function in commonTest**

```kotlin
// shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/TestDatabaseDriverFactory.kt
package ru.mirea.toir.sync.fixtures

import app.cash.sqldelight.db.SqlDriver

internal expect fun createInMemoryDriver(): SqlDriver
```

- [ ] **Step 3: Create iOS actual**

```kotlin
// shared/sync-manager/src/iosTest/kotlin/ru/mirea/toir/sync/fixtures/TestDatabaseDriverFactory.ios.kt
package ru.mirea.toir.sync.fixtures

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import ru.mirea.toir.core.database.ToirDatabase

internal actual fun createInMemoryDriver(): SqlDriver = NativeSqliteDriver(
    schema = ToirDatabase.Schema,
    name = "test.db",
    onConfiguration = { config -> config.copy(inMemory = true) },
)
```

**If `ToirDatabase.Schema` is `internal`** (commonMain may mark it so) — add `@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")` at file top. Tests living in `sync-manager` module can't see `internal` declarations of `core-database` module without it.

**If `config.copy(inMemory = true)` doesn't compile** — `DatabaseConfiguration` may not have an `inMemory` field directly; in some SQLDelight versions it's nested under `extendedConfig`. Try:

```kotlin
onConfiguration = { config ->
    config.copy(extendedConfig = config.extendedConfig.copy(inMemory = true))
}
```

If neither works, fall back to disk DB with unique random `name = Uuid.random().toString()` and explicit deletion in `@AfterTest`. Read the version of `co.touchlab.sqliter.DatabaseConfiguration` available in SQLDelight 2.3.2 native-driver if both forms fail.

- [ ] **Step 4: Create TestDatabase factory in commonTest**

```kotlin
// shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/TestDatabase.kt
package ru.mirea.toir.sync.fixtures

import app.cash.sqldelight.db.SqlDriver
import ru.mirea.toir.core.database.ToirDatabase
// + adapter imports from CoreDatabaseModule

object TestDatabase {

    fun create(): Pair<ToirDatabase, SqlDriver> {
        val driver = createInMemoryDriver()
        // ToirDatabase.Schema is applied by NativeSqliteDriver automatically on first query.
        // If running on JdbcSqliteDriver in the future, you'd call Schema.create(driver) here.
        val db = ToirDatabase(
            driver = driver,
            // <copy adapter parameter list verbatim from CoreDatabaseModule>
        )
        return db to driver
    }
}
```

Same `@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")` if `ToirDatabase` constructor or adapter types are `internal`.

- [ ] **Step 5: Write smoke test**

```kotlin
// shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/TestDatabaseSmokeTest.kt
package ru.mirea.toir.sync.fixtures

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNull

class TestDatabaseSmokeTest {

    private val pair = TestDatabase.create()
    private val db = pair.first
    private val driver = pair.second

    @AfterTest fun tearDown() = driver.close()

    @Test
    fun `empty database — selectInspectionById returns null`() {
        val result = db.inspectionQueries.selectInspectionById("nope").executeAsOneOrNull()
        assertNull(result)
    }
}
```

- [ ] **Step 6: Run**

```bash
./gradlew :shared:sync-manager:iosSimulatorArm64Test --tests "ru.mirea.toir.sync.fixtures.TestDatabaseSmokeTest"
```

Expected: PASS. First boot of simulator takes 30-60 seconds.

If FAIL on missing `inspectionQueries` — that's `ToirDatabase.inspectionQueries` exposed by SQLDelight. Confirm package and queryWrapper name from `shared/core-database/build/generated/sqldelight/.../ToirDatabaseImpl.kt`.

- [ ] **Step 7: Commit**

```bash
git add shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/ shared/sync-manager/src/iosTest/kotlin/ru/mirea/toir/sync/fixtures/
git commit -m "test(sync): add in-memory ToirDatabase fixture (commonTest + iosTest actual)"
```

---

### Task 3: TestSyncApi MockEngine helper

**Files:**
- Create: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/TestSyncApi.kt`

**Goal:** Real `SyncApiClientImpl` wired to `HttpClient(MockEngine)` with per-endpoint stub handlers and captured request log.

- [ ] **Step 1: Inspect SyncApiClientImpl wiring**

Read: `shared/sync-manager/src/commonMain/kotlin/ru/mirea/toir/sync/data/network/SyncApiClientImpl.kt`

Identify:
- ctor params (likely HttpClient + a base URL provider or Auth provider)
- endpoint paths (`/sync/push`, `/sync/changes`, `/sync/photo` — confirm by reading)

- [ ] **Step 2: Create TestSyncApi.kt**

```kotlin
package ru.mirea.toir.sync.fixtures

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.mirea.toir.sync.data.network.SyncApiClient
import ru.mirea.toir.sync.data.network.SyncApiClientImpl

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
class TestSyncApi {

    private var pushHandler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        respondJson("""{"accepted":[],"rejected":[]}""")
    }
    private var fetchHandler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        respondJson("""{
            "locations":[],"equipment":[],"routes":[],"routePoints":[],"checklists":[],"assignments":[],
            "deleted":{"locations":[],"equipment":[],"routes":[],"routePoints":[],"checklists":[],"assignments":[]},
            "updatedAt":"2026-05-13T00:00:00Z"
        }""")
    }
    private var photoHandler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        respondJson("""{"storageKey":"k-1"}""")
    }

    val capturedRequests = mutableListOf<HttpRequestData>()

    fun stubPush(h: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) { pushHandler = h }
    fun stubFetch(h: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) { fetchHandler = h }
    fun stubPhoto(h: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) { photoHandler = h }

    fun build(): SyncApiClient {
        val mockEngine = MockEngine { request ->
            capturedRequests += request
            when {
                request.url.encodedPath.contains("/sync/push") -> pushHandler(request)
                request.url.encodedPath.contains("/sync/changes") -> fetchHandler(request)
                request.url.encodedPath.contains("/sync/photo") -> photoHandler(request)
                else -> respond("Unknown endpoint: ${request.url}", HttpStatusCode.NotFound)
            }
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
            }
        }
        // Match real ctor — adjust if SyncApiClientImpl needs other deps (BaseUrlProvider, AuthProvider).
        return SyncApiClientImpl(httpClient = httpClient, baseUrl = "http://test/")
    }

    companion object {
        fun MockRequestHandleScope.respondJson(body: String, status: HttpStatusCode = HttpStatusCode.OK): HttpResponseData =
            respond(content = body, status = status, headers = headersOf(HttpHeaders.ContentType, "application/json"))
    }
}
```

Adjust `SyncApiClientImpl` constructor call to match real signature.

- [ ] **Step 3: Smoke test**

```kotlin
// shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/TestSyncApiSmokeTest.kt
package ru.mirea.toir.sync.fixtures

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class TestSyncApiSmokeTest {

    @Test
    fun `fetchConfigChanges returns parsed empty response`() = runTest {
        val api = TestSyncApi().build()
        val result = api.fetchConfigChanges(since = "2026-01-01T00:00:00Z")
        assertTrue(result.isSuccess, "Expected success but was $result")
        assertTrue(result.getOrThrow().locations.isEmpty())
    }
}
```

- [ ] **Step 4: Run + commit**

```bash
./gradlew :shared:sync-manager:iosSimulatorArm64Test --tests "ru.mirea.toir.sync.fixtures.TestSyncApiSmokeTest"
```
Expected: PASS.

```bash
git add shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/TestSyncApi.kt shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/TestSyncApiSmokeTest.kt
git commit -m "test(sync): add Ktor MockEngine helper wired to real SyncApiClientImpl"
```

---

### Task 4: TestData builders + TestDispatchers + TestFileReader

**Files:**
- Create: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/TestData.kt`
- Create: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/TestDispatchers.kt`
- Create: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/TestFileReader.kt`

- [ ] **Step 1: Inspect SyncRepositoryImpl ctor + storages**

Read `shared/sync-manager/src/commonMain/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryImpl.kt` lines 52-65. List every storage and each insert method signature.

Read `shared/common/src/commonMain/kotlin/ru/mirea/toir/common/coroutines/CoroutineDispatchers.kt` to confirm the data class shape.

Read `shared/sync-manager/src/commonMain/kotlin/ru/mirea/toir/sync/data/FileReader.kt` for the FileReader interface signature.

- [ ] **Step 2: Create TestDispatchers**

```kotlin
package ru.mirea.toir.sync.fixtures

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import ru.mirea.toir.common.coroutines.CoroutineDispatchers

@OptIn(ExperimentalCoroutinesApi::class)
fun testDispatchers(): CoroutineDispatchers {
    val d = UnconfinedTestDispatcher()
    return CoroutineDispatchers(main = d, io = d, default = d)
}
```

Adjust ctor parameters to match the real `CoroutineDispatchers` class.

- [ ] **Step 3: Create TestFileReader**

```kotlin
package ru.mirea.toir.sync.fixtures

import ru.mirea.toir.sync.data.FileReader

class TestFileReader(
    private val payloads: MutableMap<String, ByteArray> = mutableMapOf(),
) : FileReader {
    fun put(path: String, bytes: ByteArray) { payloads[path] = bytes }
    override fun readBytes(path: String): ByteArray =
        payloads[path] ?: error("No fake bytes registered for path '$path'")
}
```

Match `FileReader` interface signature exactly.

- [ ] **Step 4: Create TestData with seed functions**

```kotlin
package ru.mirea.toir.sync.fixtures

import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.models.LocalInspectionStatus
import ru.mirea.toir.core.database.models.LocalSyncStatus

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
object TestData {

    const val ASSIGNMENT_ID = "asg-1"
    const val ROUTE_ID = "route-1"
    const val INSPECTION_ID = "ins-1"
    const val EQUIPMENT_ID = "eq-1"
    const val EQUIPMENT_RESULT_ID = "eqr-1"
    const val CHECKLIST_ITEM_RESULT_ID = "cir-1"
    const val PHOTO_ID = "photo-1"
    const val LOCATION_ID = "loc-1"
    const val ROUTE_POINT_ID = "rp-1"
    const val CHECKLIST_ID = "chk-1"
    const val NOW = "2026-05-13T12:00:00Z"

    fun ToirDatabase.seedLocation(id: String = LOCATION_ID) {
        // open shared/core-database/src/commonMain/sqldelight/.../Location.sq and copy insert params
        locationQueries.insert(id = id, /* fill */)
    }

    fun ToirDatabase.seedEquipment(id: String = EQUIPMENT_ID, locationId: String = LOCATION_ID) {
        equipmentQueries.insert(id = id, locationId = locationId, /* fill */)
    }

    fun ToirDatabase.seedRoute(id: String = ROUTE_ID) {
        routeQueries.insert(id = id, /* fill */)
    }

    fun ToirDatabase.seedRoutePoint(
        id: String = ROUTE_POINT_ID,
        routeId: String = ROUTE_ID,
        equipmentId: String = EQUIPMENT_ID,
    ) {
        routePointQueries.insert(id = id, routeId = routeId, equipmentId = equipmentId, /* fill */)
    }

    fun ToirDatabase.seedAssignment(
        id: String = ASSIGNMENT_ID,
        routeId: String = ROUTE_ID,
    ) {
        routeAssignmentQueries.insert(id = id, routeId = routeId, /* fill from RouteAssignment.sq */)
    }

    fun ToirDatabase.seedPendingInspection(
        id: String = INSPECTION_ID,
        assignmentId: String = ASSIGNMENT_ID,
        attemptCount: Long = 0L,
    ) {
        inspectionQueries.insertInspection(
            id = id,
            assignmentId = assignmentId,
            status = LocalInspectionStatus.COMPLETED,
            syncStatus = LocalSyncStatus.PENDING,
            completedAt = NOW,
            syncAttemptCount = attemptCount,
            // fill from Inspection.sq
        )
    }

    fun ToirDatabase.seedPendingEquipmentResult(
        id: String = EQUIPMENT_RESULT_ID,
        inspectionId: String = INSPECTION_ID,
        equipmentId: String = EQUIPMENT_ID,
        routePointId: String = ROUTE_POINT_ID,
    ) { /* insert from InspectionEquipmentResult.sq with syncStatus = PENDING */ }

    fun ToirDatabase.seedPendingChecklistItemResult(
        id: String = CHECKLIST_ITEM_RESULT_ID,
        equipmentResultId: String = EQUIPMENT_RESULT_ID,
    ) { /* insert from ChecklistItemResult.sq with syncStatus = PENDING */ }

    fun ToirDatabase.seedPendingPhoto(
        id: String = PHOTO_ID,
        checklistItemResultId: String = CHECKLIST_ITEM_RESULT_ID,
        localPath: String = "/tmp/photo.jpg",
    ) { /* insert from Photo.sq with syncStatus = PENDING */ }

    /** Full FK chain for a single-inspection scenario. */
    fun ToirDatabase.seedFullPendingScenario() {
        seedLocation()
        seedEquipment()
        seedRoute()
        seedRoutePoint()
        seedAssignment()
        seedPendingInspection()
        seedPendingEquipmentResult()
        seedPendingChecklistItemResult()
    }
}
```

**Critical:** open each `.sq` file in `shared/core-database/src/commonMain/sqldelight/` and copy `insert*` parameter names/types verbatim. Use the actual generated query class names (likely `locationQueries`, `equipmentQueries`, etc. — confirm from `ToirDatabaseImpl.kt`).

- [ ] **Step 5: Smoke test**

```kotlin
// TestDataSmokeTest.kt
package ru.mirea.toir.sync.fixtures

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import ru.mirea.toir.sync.fixtures.TestData.seedFullPendingScenario

class TestDataSmokeTest {

    private val pair = TestDatabase.create()
    private val db = pair.first
    private val driver = pair.second

    @AfterTest fun tearDown() = driver.close()

    @Test
    fun `seedFullPendingScenario persists all FK rows`() {
        db.seedFullPendingScenario()
        val ins = db.inspectionQueries.selectInspectionById(TestData.INSPECTION_ID).executeAsOne()
        assertEquals(TestData.INSPECTION_ID, ins.id)
    }
}
```

- [ ] **Step 6: Run + commit**

```bash
./gradlew :shared:sync-manager:iosSimulatorArm64Test --tests "ru.mirea.toir.sync.fixtures.*"
```
Expected: 3 PASS (Database, SyncApi, TestData smoke tests).

```bash
git add shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/fixtures/
git commit -m "test(sync): add data builders, test dispatchers, fake file reader"
```

---

### Task 5: ConfigChangesApplier — happy path

**Files:**
- Create: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/applier/ConfigChangesApplierHappyPathTest.kt`

- [ ] **Step 1: Wire applier with real storages**

```kotlin
package ru.mirea.toir.sync.data.applier

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import ru.mirea.toir.core.database.TransactionRunnerImpl
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorageImpl
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorageImpl
import ru.mirea.toir.core.database.storage.location.LocationStorageImpl
import ru.mirea.toir.core.database.storage.route.RouteStorageImpl
import ru.mirea.toir.sync.data.network.models.RemoteConfigChangesResponse
// ... Remote* DTO imports
import ru.mirea.toir.sync.fixtures.TestDatabase

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
class ConfigChangesApplierHappyPathTest {

    private val pair = TestDatabase.create()
    private val db = pair.first
    private val driver = pair.second

    private val applier = ConfigChangesApplier(
        routeStorage = RouteStorageImpl(db /*, testDispatchers() if needed */),
        equipmentStorage = EquipmentStorageImpl(db),
        locationStorage = LocationStorageImpl(db),
        checklistStorage = ChecklistStorageImpl(db),
        transactionRunner = TransactionRunnerImpl(db),
    )

    @AfterTest fun tearDown() = driver.close()
```

- [ ] **Step 2: Write happy test**

```kotlin
    @Test
    fun `apply persists locations, equipment, routes, route points, checklists, assignments`() {
        val response = RemoteConfigChangesResponse(
            locations = listOf(RemoteConfigLocation(id = "loc-1", name = "L1", updatedAt = "2026-05-13T12:00:00Z" /* fill */)),
            equipment = listOf(/* one item with locationId="loc-1" */),
            routes = listOf(/* one route */),
            routePoints = listOf(/* one rp tied to route + equipment */),
            checklists = listOf(/* one checklist tied to equipment */),
            assignments = listOf(/* one assignment tied to route */),
            deleted = /* empty RemoteDeletedIds */,
            updatedAt = "2026-05-13T12:00:00Z",
        )

        applier.apply(response)

        assertEquals(1, db.locationQueries.selectAll().executeAsList().size)
        assertEquals(1, db.equipmentQueries.selectAll().executeAsList().size)
        assertEquals(1, db.routeQueries.selectAll().executeAsList().size)
        assertEquals(1, db.routePointQueries.selectAll().executeAsList().size)
        assertEquals(1, db.checklistQueries.selectAll().executeAsList().size)
        assertEquals(1, db.routeAssignmentQueries.selectAll().executeAsList().size)
    }
}
```

Use the actual `selectAll` query name from each `.sq` file (may be `selectAllRoutes`, `selectAll`, etc.).

- [ ] **Step 3: Run + commit**

```bash
./gradlew :shared:sync-manager:iosSimulatorArm64Test --tests "*ConfigChangesApplierHappyPathTest*"
```
Expected: PASS.

```bash
git add shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/applier/ConfigChangesApplierHappyPathTest.kt
git commit -m "test(sync): characterize ConfigChangesApplier happy path"
```

---

### Task 6: ConfigChangesApplier — partial + deleted ids

**Files:**
- Create: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/applier/ConfigChangesApplierPartialTest.kt`

- [ ] **Step 1: Empty-response test**

```kotlin
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
class ConfigChangesApplierPartialTest {

    private val pair = TestDatabase.create()
    private val db = pair.first
    private val driver = pair.second
    private val applier = /* same wiring as Task 5 */

    @AfterTest fun tearDown() = driver.close()

    @Test
    fun `apply with all empty lists — no-op`() {
        val empty = RemoteConfigChangesResponse(
            locations = emptyList(), equipment = emptyList(), routes = emptyList(),
            routePoints = emptyList(), checklists = emptyList(), assignments = emptyList(),
            deleted = RemoteDeletedIds(/* all empty */),
            updatedAt = "2026-05-13T12:00:00Z",
        )
        applier.apply(empty)
        assertEquals(0, db.locationQueries.selectAll().executeAsList().size)
    }
```

- [ ] **Step 2: Deleted-ids test**

```kotlin
    @Test
    fun `apply with deleted ids removes rows`() {
        db.locationQueries.insert(id = "loc-del", /* fill */)

        val response = RemoteConfigChangesResponse(
            locations = emptyList(), equipment = emptyList(), routes = emptyList(),
            routePoints = emptyList(), checklists = emptyList(), assignments = emptyList(),
            deleted = RemoteDeletedIds(locations = listOf("loc-del"), /* rest empty */),
            updatedAt = "2026-05-13T12:00:00Z",
        )

        applier.apply(response)

        assertEquals(0, db.locationQueries.selectAll().executeAsList().size)
    }
}
```

If applier doesn't currently handle deletes (it might be a known gap per the active plan), the deleted-ids test will FAIL. Don't paper over: mark it as a discovered bug in commit message, file a follow-up. Leave the test asserting *correct* behavior — failing tests on first run after writing them is a TDD-flavored signal, not a defect of the plan.

- [ ] **Step 3: Run + commit**

```bash
./gradlew :shared:sync-manager:iosSimulatorArm64Test --tests "*ConfigChangesApplierPartialTest*"
```

```bash
git add shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/applier/ConfigChangesApplierPartialTest.kt
git commit -m "test(sync): cover ConfigChangesApplier empty + deleted-ids"
```

---

### Task 7: SyncRepositoryImpl — pushPendingData happy

**Files:**
- Create: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryPushTest.kt`

- [ ] **Step 1: Wire SyncRepositoryImpl**

```kotlin
package ru.mirea.toir.sync.data.repository

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import ru.mirea.toir.core.database.TransactionRunnerImpl
import ru.mirea.toir.core.database.storage.action_log.ActionLogStorageImpl
import ru.mirea.toir.core.database.storage.checklist.ChecklistStorageImpl
import ru.mirea.toir.core.database.storage.equipment.EquipmentStorageImpl
import ru.mirea.toir.core.database.storage.inspection.InspectionStorageImpl
import ru.mirea.toir.core.database.storage.location.LocationStorageImpl
import ru.mirea.toir.core.database.storage.photo.PhotoStorageImpl
import ru.mirea.toir.core.database.storage.route.RouteStorageImpl
import ru.mirea.toir.core.database.storage.sync_meta.SyncMetaStorageImpl
import ru.mirea.toir.core.database.models.LocalSyncStatus
import ru.mirea.toir.sync.data.applier.ConfigChangesApplier
import ru.mirea.toir.sync.fixtures.TestData
import ru.mirea.toir.sync.fixtures.TestData.seedFullPendingScenario
import ru.mirea.toir.sync.fixtures.TestDatabase
import ru.mirea.toir.sync.fixtures.TestFileReader
import ru.mirea.toir.sync.fixtures.TestSyncApi
import ru.mirea.toir.sync.fixtures.TestSyncApi.Companion.respondJson
import ru.mirea.toir.sync.fixtures.testDispatchers

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
class SyncRepositoryPushTest {

    private val pair = TestDatabase.create()
    private val db = pair.first
    private val driver = pair.second
    private val syncApi = TestSyncApi()

    private val repo: SyncRepository = SyncRepositoryImpl(
        // COPY CTOR ARG LIST VERBATIM from SyncRepositoryImpl.kt L52-65:
        inspectionStorage = InspectionStorageImpl(db, testDispatchers()),
        photoStorage = PhotoStorageImpl(db, testDispatchers()),
        actionLogStorage = ActionLogStorageImpl(db, testDispatchers()),
        syncMetaStorage = SyncMetaStorageImpl(db),
        configChangesApplier = ConfigChangesApplier(
            routeStorage = RouteStorageImpl(db),
            equipmentStorage = EquipmentStorageImpl(db),
            locationStorage = LocationStorageImpl(db),
            checklistStorage = ChecklistStorageImpl(db),
            transactionRunner = TransactionRunnerImpl(db),
        ),
        syncApiClient = syncApi.build(),
        coroutineDispatchers = testDispatchers(),
        fileReader = TestFileReader(),
        transactionRunner = TransactionRunnerImpl(db),
        // adjust ctor args to actual SyncRepositoryImpl signature
    )

    @AfterTest fun tearDown() = driver.close()
```

**Critical:** copy ctor args verbatim from `SyncRepositoryImpl` (lines 52-65). Don't guess. If a ctor needs a `CurrentUserProvider` or other service — add a minimal fake in `fixtures/` and dispatch a follow-up subagent if needed.

- [ ] **Step 2: Happy push test**

```kotlin
    @Test
    fun `pushPendingData with one pending inspection — accepted, marked SYNCED`() = runTest {
        db.seedFullPendingScenario()

        syncApi.stubPush {
            respondJson("""
                {"accepted":[{"entityType":"INSPECTION","clientId":"${TestData.INSPECTION_ID}","serverId":"srv-1"}],"rejected":[]}
            """.trimIndent())
        }

        val result = repo.pushPendingData()

        assertTrue(result.isSuccess, "Push failed: $result")
        val inspection = db.inspectionQueries.selectInspectionById(TestData.INSPECTION_ID).executeAsOne()
        assertEquals(LocalSyncStatus.SYNCED, inspection.syncStatus)
    }
}
```

Match `RemoteSyncAccepted` field names from `RemoteSyncPushResponse.kt`.

- [ ] **Step 3: Run + commit**

```bash
./gradlew :shared:sync-manager:iosSimulatorArm64Test --tests "*SyncRepositoryPushTest.pushPendingData with one pending inspection*"
```

```bash
git add shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryPushTest.kt
git commit -m "test(sync): pushPendingData happy path — accepted rows marked SYNCED"
```

---

### Task 8: SyncRepositoryImpl — pushPendingData rejected + empty

**Files:**
- Modify: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryPushTest.kt`

- [ ] **Step 1: Rejected test**

```kotlin
    @Test
    fun `pushPendingData with rejected — attempt count incremented, status PENDING`() = runTest {
        db.seedFullPendingScenario()

        syncApi.stubPush {
            respondJson("""
                {"accepted":[],"rejected":[{"entityType":"INSPECTION","clientId":"${TestData.INSPECTION_ID}","reason":"VALIDATION_FAILED","message":"bad"}]}
            """.trimIndent())
        }

        val result = repo.pushPendingData()

        assertTrue(result.isSuccess)
        val inspection = db.inspectionQueries.selectInspectionById(TestData.INSPECTION_ID).executeAsOne()
        assertEquals(LocalSyncStatus.PENDING, inspection.syncStatus)
        assertEquals(1L, inspection.syncAttemptCount)
        assertTrue(inspection.syncNextAttemptAt != null)
    }
```

- [ ] **Step 2: Empty pending test**

```kotlin
    @Test
    fun `pushPendingData with no pending — succeeds, no HTTP call`() = runTest {
        val result = repo.pushPendingData()
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrThrow().pushed)
        assertEquals(0, syncApi.capturedRequests.count { it.url.encodedPath.contains("/sync/push") })
    }
```

- [ ] **Step 3: Run + commit**

```bash
./gradlew :shared:sync-manager:iosSimulatorArm64Test --tests "*SyncRepositoryPushTest*"
```

```bash
git add shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryPushTest.kt
git commit -m "test(sync): pushPendingData rejected + empty cases"
```

---

### Task 9: SyncRepositoryImpl — pushPendingData HTTP 500

**Files:**
- Modify: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryPushTest.kt`

- [ ] **Step 1: 500 test**

```kotlin
    @Test
    fun `pushPendingData HTTP 500 — Result_failure, rows stay PENDING`() = runTest {
        db.seedFullPendingScenario()

        syncApi.stubPush { respond("server down", HttpStatusCode.InternalServerError) }

        val result = repo.pushPendingData()

        assertTrue(result.isFailure, "Expected failure but was $result")
        val inspection = db.inspectionQueries.selectInspectionById(TestData.INSPECTION_ID).executeAsOne()
        assertEquals(LocalSyncStatus.PENDING, inspection.syncStatus)
    }
```

- [ ] **Step 2: Run + commit**

```bash
./gradlew :shared:sync-manager:iosSimulatorArm64Test --tests "*SyncRepositoryPushTest*"
```
Expected: 4 PASS.

```bash
git add shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryPushTest.kt
git commit -m "test(sync): pushPendingData HTTP 500 leaves rows pending"
```

---

### Task 10: SyncRepositoryImpl — fetchAndApplyDeltaChanges

**Files:**
- Create: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryFetchTest.kt`

```kotlin
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
class SyncRepositoryFetchTest {

    private val pair = TestDatabase.create()
    private val db = pair.first
    private val driver = pair.second
    private val syncApi = TestSyncApi()
    private val repo = /* same wiring as Task 7 */

    @AfterTest fun tearDown() = driver.close()

    @Test
    fun `fetchAndApplyDeltaChanges — applies and updates last_sync_time`() = runTest {
        syncApi.stubFetch {
            respondJson("""{
                "locations":[{"id":"loc-1","name":"L1","updatedAt":"2026-05-13T12:00:00Z"}],
                "equipment":[],"routes":[],"routePoints":[],"checklists":[],"assignments":[],
                "deleted":{"locations":[],"equipment":[],"routes":[],"routePoints":[],"checklists":[],"assignments":[]},
                "updatedAt":"2026-05-13T12:00:00Z"
            }""")
        }

        val result = repo.fetchAndApplyDeltaChanges()

        assertTrue(result.isSuccess)
        assertEquals(1, db.locationQueries.selectAll().executeAsList().size)
        // Confirm key name from SyncRepositoryImpl.kt or SyncMetaStorageImpl.kt
        val lastSync = db.syncMetaQueries.selectByKey("last_sync_time").executeAsOneOrNull()
        assertEquals("2026-05-13T12:00:00Z", lastSync?.value_)
    }

    @Test
    fun `fetchAndApplyDeltaChanges HTTP 500 — failure, last_sync_time unchanged`() = runTest {
        val before = db.syncMetaQueries.selectByKey("last_sync_time").executeAsOneOrNull()?.value_
        syncApi.stubFetch { respond("nope", HttpStatusCode.InternalServerError) }

        val result = repo.fetchAndApplyDeltaChanges()
        val after = db.syncMetaQueries.selectByKey("last_sync_time").executeAsOneOrNull()?.value_

        assertTrue(result.isFailure)
        assertEquals(before, after)
    }
}
```

```bash
./gradlew :shared:sync-manager:iosSimulatorArm64Test --tests "*SyncRepositoryFetchTest*"
git add shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryFetchTest.kt
git commit -m "test(sync): fetchAndApplyDeltaChanges happy + HTTP error"
```

---

### Task 11: SyncRepositoryImpl — uploadPendingPhotos

**Files:**
- Create: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryPhotoTest.kt`

```kotlin
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
class SyncRepositoryPhotoTest {

    private val pair = TestDatabase.create()
    private val db = pair.first
    private val driver = pair.second
    private val syncApi = TestSyncApi()
    private val fileReader = TestFileReader().apply { put("/tmp/photo.jpg", ByteArray(100) { it.toByte() }) }
    private val repo = /* same wiring as Task 7, but with this fileReader */

    @AfterTest fun tearDown() = driver.close()

    @Test
    fun `uploadPendingPhotos — happy — SYNCED with storageKey`() = runTest {
        db.seedFullPendingScenario()
        db.seedPendingPhoto()

        syncApi.stubPhoto { respondJson("""{"storageKey":"s3://k/photo-1.jpg"}""") }

        val result = repo.uploadPendingPhotos()

        assertTrue(result.isSuccess)
        val photo = db.photoQueries.selectById(TestData.PHOTO_ID).executeAsOne()
        assertEquals(LocalSyncStatus.SYNCED, photo.syncStatus)
        assertEquals("s3://k/photo-1.jpg", photo.storageKey)
    }

    @Test
    fun `uploadPendingPhotos HTTP 500 — attempt count incremented`() = runTest {
        db.seedFullPendingScenario()
        db.seedPendingPhoto()

        syncApi.stubPhoto { respond("down", HttpStatusCode.InternalServerError) }

        repo.uploadPendingPhotos()

        val photo = db.photoQueries.selectById(TestData.PHOTO_ID).executeAsOne()
        assertEquals(LocalSyncStatus.PENDING, photo.syncStatus)
        assertEquals(1L, photo.syncAttemptCount)
    }
}
```

```bash
./gradlew :shared:sync-manager:iosSimulatorArm64Test --tests "*SyncRepositoryPhotoTest*"
git add shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryPhotoTest.kt
git commit -m "test(sync): uploadPendingPhotos happy + failure"
```

---

### Task 12: SyncRepositoryImpl — meta + observe

**Files:**
- Create: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryMetaTest.kt`
- Create: `shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryObserveTest.kt`

**SyncRepositoryMetaTest:**

```kotlin
@OptIn(ExperimentalTime::class)
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
class SyncRepositoryMetaTest {

    private val pair = TestDatabase.create()
    private val db = pair.first
    private val driver = pair.second
    private val repo = /* same wiring */

    @AfterTest fun tearDown() = driver.close()

    @Test
    fun `recordSuccessfulRun writes sync_last_success_at`() = runTest {
        val now = Instant.parse("2026-05-13T12:00:00Z")
        repo.recordSuccessfulRun(now)
        val v = db.syncMetaQueries.selectByKey("sync_last_success_at").executeAsOneOrNull()?.value_
        assertEquals(now.toString(), v)
    }

    @Test
    fun `recordFailedRun writes failure_at + failure_reason`() = runTest {
        val now = Instant.parse("2026-05-13T12:05:00Z")
        repo.recordFailedRun(now, SyncFailureReason.NETWORK)
        val ts = db.syncMetaQueries.selectByKey("sync_last_failure_at").executeAsOneOrNull()?.value_
        val reason = db.syncMetaQueries.selectByKey("sync_last_failure_reason").executeAsOneOrNull()?.value_
        assertEquals(now.toString(), ts)
        assertEquals("NETWORK", reason)
    }
}
```

Confirm key names from `SyncRepositoryImpl.kt` lines 227-249.

**SyncRepositoryObserveTest:**

```kotlin
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
class SyncRepositoryObserveTest {

    private val pair = TestDatabase.create()
    private val db = pair.first
    private val driver = pair.second
    private val repo = /* same wiring */

    @AfterTest fun tearDown() = driver.close()

    @Test
    fun `observeHasPending emits true after pending insert`() = runTest {
        db.seedLocation(); db.seedEquipment(); db.seedRoute(); db.seedRoutePoint(); db.seedAssignment()

        val emissions = mutableListOf<Boolean>()
        val job = launch { repo.observeHasPending().take(2).toList(emissions) }
        db.seedPendingInspection()
        job.join()

        assertEquals(listOf(false, true), emissions)
    }
}
```

```bash
./gradlew :shared:sync-manager:iosSimulatorArm64Test --tests "*SyncRepositoryMetaTest*" --tests "*SyncRepositoryObserveTest*"
git add shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryMetaTest.kt shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/data/repository/SyncRepositoryObserveTest.kt
git commit -m "test(sync): meta records + observeHasPending flow emissions"
```

---

### Task 13 (optional): SyncManager.runOnce orchestration

Skip if time-constrained. Coverage on repository (Tasks 7-12) covers ~80% of the surface. This task only verifies sequencing + status transitions.

```kotlin
// shared/sync-manager/src/commonTest/kotlin/ru/mirea/toir/sync/domain/SyncManagerOrchestrationTest.kt
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
class SyncManagerOrchestrationTest {
    // wire SyncManager with real repo + fake NetworkMonitor + testDispatchers
    @Test fun `runOnce success — Idle → Running → Success`() = runTest { /* ... */ }
    @Test fun `runOnce empty data — no push HTTP, fetch HTTP made`() = runTest { /* ... */ }
}
```

---

## Verification

```bash
./gradlew :shared:sync-manager:iosSimulatorArm64Test
```

Expected: 16-20 tests PASS, run time ~30-60s (after first simulator boot).

```bash
./gradlew :shared:sync-manager:check
```

Expected: BUILD SUCCESSFUL. Detekt may complain about long JSON literals in tests — apply `@Suppress("MaxLineLength")` on offending classes.

---

## Self-Review

**Spec coverage** (against `docs/superpowers/plans/10-sync-completion.md` Phase 7):
- ✅ SyncRepository push happy/rejected/error (Tasks 7-9)
- ✅ Retry scheduling on reject (Task 8)
- ✅ fetchAndApplyDeltaChanges + last_sync_time (Task 10)
- ✅ ConfigChangesApplier happy + deletes (Tasks 5-6)
- ✅ Photos upload + meta + observe (Tasks 11-12)
- ❌ `updated_at` guard (Phase 5 dep) — out of scope
- ❌ NetworkMonitor (Phase 4) — out of scope

**Placeholder scan:** every step has explicit instructions ("copy ctor verbatim from line X-Y"). Adapter parameter lists, `.sq` insert params, and ctor arg lists are flagged "fill from source" — not placeholders, but explicit anchors to existing code. No TBD/TODO.

**Risk: ctor args mismatch.** Tasks 7-12 share wiring. If Task 7 ctor wiring fails, all subsequent tasks fail. Mitigation: Task 7 Step 1 explicitly mandates reading `SyncRepositoryImpl.kt` and copying verbatim. After Task 7 passes review, Tasks 8-12 reuse the same block.

**Risk: NativeSqliteDriver in-memory API differs from `config.copy(inMemory = true)`.** Mitigation: Task 2 Step 3 includes a fallback (`extendedConfig.inMemory`) and a final fallback (disk DB with unique random name). If both fail, escalate.

**Risk: `internal` visibility in core-database/sync-manager blocks tests.** Tests live in `sync-manager` module but use `internal` types from `core-database`. The `@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")` annotation works at file scope. Used preventively at top of every test file.

---

## Execution Handoff

Subagent-driven (already chosen). Continue task-by-task. First task is **Task 1** above (commonTestDependencies in `sync-manager/build.gradle.kts`).
