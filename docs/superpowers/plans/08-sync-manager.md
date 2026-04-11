# Waypoint 08 — Sync Manager (Синхронизация)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Реализовать модуль синхронизации: push результатов обхода (`POST /api/v1/mobile/sync/push`), загрузка фото (`POST /api/v1/mobile/photos/upload`), дельта-синхронизация справочников (`GET /api/v1/mobile/config/changes`). Android: периодический запуск через WorkManager.

**Architecture:** Отдельный модуль `shared/sync-manager` (не 4-модульная фича — нет UI экрана). Содержит: `SyncApiClient`, `SyncRepository`, `SyncManager`. Вызов синхронизации: вручную из любого экрана или из WorkManager (Android). После успеха → `sync_status = SYNCED`.

**Tech Stack:** Ktor (multipart для фото), SQLDelight, WorkManager (Android), Koin, Kotlin Serialization.

---

## Файловая структура

```
shared/sync-manager/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/ru/mirea/toir/sync/
    │   ├── di/SyncManagerModule.kt
    │   ├── api/SyncApiClient.kt
    │   ├── api/SyncApiClientImpl.kt
    │   ├── api/models/RemoteSyncPushRequest.kt
    │   ├── api/models/RemoteSyncPushResponse.kt
    │   ├── SyncRepository.kt
    │   ├── SyncRepositoryImpl.kt
    │   └── SyncManager.kt
    └── androidMain/kotlin/ru/mirea/toir/sync/
        └── worker/SyncWorker.kt
```

---

### Task 1: Создать модуль sync-manager

- [ ] **Step 1: Зарегистрировать в settings.gradle.kts**

```kotlin
include(":shared:sync-manager")
```

- [ ] **Step 2: Добавить WorkManager в libs.versions.toml**

В `[versions]`:
```toml
androidx-workmanager = "2.10.1"
```

В `[libraries]`:
```toml
androidx-workmanager = { module = "androidx.work:work-runtime-ktx", version.ref = "androidx-workmanager" }
koin-android-workmanager = { group = "io.insert-koin", name = "koin-androidx-workmanager", version.ref = "koin" }
```

- [ ] **Step 3: Создать build.gradle.kts**

`shared/sync-manager/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
import extensions.androidMainDependencies
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.sync"
}

commonMainDependencies {
    implementations(
        projects.shared.coreNetwork,
        projects.shared.coreDatabase,
    )
}

androidMainDependencies {
    implementations(
        libs.androidx.workmanager,
        libs.koin.android.workmanager,
    )
}
```

- [ ] **Step 4: Создать директории**

```bash
mkdir -p shared/sync-manager/src/commonMain/kotlin/ru/mirea/toir/sync/di
mkdir -p shared/sync-manager/src/commonMain/kotlin/ru/mirea/toir/sync/api/models
mkdir -p shared/sync-manager/src/androidMain/kotlin/ru/mirea/toir/sync/worker
```

---

### Task 2: Remote DTO для sync push

> ⚠️ Поля взяты из `POST /api/v1/mobile/sync/push` в openapi/documentation.yaml. Сверь с актуальным YAML перед реализацией.

**Files:**
- Create: `.../sync/api/models/RemoteSyncPushRequest.kt`
- Create: `.../sync/api/models/RemoteSyncPushResponse.kt`

- [ ] **Step 1: RemoteSyncPushRequest**

```kotlin
package ru.mirea.toir.sync.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteSyncPushRequest(
    @SerialName("clientBatchId") val clientBatchId: String,
    @SerialName("deviceId") val deviceId: String,
    @SerialName("sentAt") val sentAt: String,
    @SerialName("inspections") val inspections: List<RemoteSyncInspection>?,
    @SerialName("inspectionEquipmentResults") val inspectionEquipmentResults: List<RemoteSyncEquipmentResult>?,
    @SerialName("checklistItemResults") val checklistItemResults: List<RemoteSyncChecklistItemResult>?,
    @SerialName("actionLogs") val actionLogs: List<RemoteSyncActionLog>?,
)

@Serializable
internal data class RemoteSyncInspection(
    @SerialName("id") val id: String,
    @SerialName("assignmentId") val assignmentId: String,
    @SerialName("routeId") val routeId: String,
    @SerialName("status") val status: String,
    @SerialName("startedAt") val startedAt: String,
    @SerialName("completedAt") val completedAt: String?,
)

@Serializable
internal data class RemoteSyncEquipmentResult(
    @SerialName("id") val id: String,
    @SerialName("inspectionId") val inspectionId: String,
    @SerialName("routePointId") val routePointId: String,
    @SerialName("equipmentId") val equipmentId: String,
    @SerialName("status") val status: String,
    @SerialName("startedAt") val startedAt: String?,
    @SerialName("completedAt") val completedAt: String?,
)

@Serializable
internal data class RemoteSyncChecklistItemResult(
    @SerialName("id") val id: String,
    @SerialName("inspectionEquipmentResultId") val inspectionEquipmentResultId: String,
    @SerialName("checklistItemId") val checklistItemId: String,
    @SerialName("valueBoolean") val valueBoolean: Boolean?,
    @SerialName("valueNumber") val valueNumber: Double?,
    @SerialName("valueText") val valueText: String?,
    @SerialName("valueSelect") val valueSelect: String?,
    @SerialName("isConfirmed") val isConfirmed: Boolean,
    @SerialName("answeredAt") val answeredAt: String?,
)

@Serializable
internal data class RemoteSyncActionLog(
    @SerialName("id") val id: String,
    @SerialName("inspectionId") val inspectionId: String,
    @SerialName("actionType") val actionType: String,
    @SerialName("metadata") val metadata: String?,
    @SerialName("createdAt") val createdAt: String,
)
```

- [ ] **Step 2: RemoteSyncPushResponse**

```kotlin
package ru.mirea.toir.sync.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteSyncPushResponse(
    @SerialName("clientBatchId") val clientBatchId: String?,
    @SerialName("result") val result: String?,
    @SerialName("accepted") val accepted: RemoteSyncAccepted?,
    @SerialName("rejected") val rejected: List<RemoteSyncRejected>?,
    @SerialName("serverTime") val serverTime: String?,
)

@Serializable
internal data class RemoteSyncAccepted(
    @SerialName("inspections") val inspections: List<String>?,
    @SerialName("inspectionEquipmentResults") val inspectionEquipmentResults: List<String>?,
    @SerialName("checklistItemResults") val checklistItemResults: List<String>?,
    @SerialName("actionLogs") val actionLogs: List<String>?,
)

@Serializable
internal data class RemoteSyncRejected(
    @SerialName("entityType") val entityType: String?,
    @SerialName("entityId") val entityId: String?,
    @SerialName("reason") val reason: String?,
)
```

---

### Task 3: SyncApiClient

**Files:**
- Create: `.../sync/api/SyncApiClient.kt`
- Create: `.../sync/api/SyncApiClientImpl.kt`

- [ ] **Step 1: SyncApiClient**

```kotlin
package ru.mirea.toir.sync.api

import ru.mirea.toir.sync.api.models.RemoteSyncPushRequest
import ru.mirea.toir.sync.api.models.RemoteSyncPushResponse

internal interface SyncApiClient {
    suspend fun pushSync(request: RemoteSyncPushRequest): Result<RemoteSyncPushResponse>
    suspend fun uploadPhoto(photoId: String, checklistItemResultId: String, fileUri: String): Result<Unit>
    suspend fun fetchConfigChanges(since: String): Result<Unit>
}
```

- [ ] **Step 2: SyncApiClientImpl**

```kotlin
package ru.mirea.toir.sync.api

import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.network.ktor.KtorClient
import ru.mirea.toir.sync.api.models.RemoteSyncPushRequest
import ru.mirea.toir.sync.api.models.RemoteSyncPushResponse

internal class SyncApiClientImpl(
    private val ktorClient: KtorClient,
) : SyncApiClient {

    override suspend fun pushSync(request: RemoteSyncPushRequest): Result<RemoteSyncPushResponse> =
        ktorClient.executeQuery(
            query = {
                ktorClient.post("/api/v1/mobile/sync/push") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            },
            deserializer = RemoteSyncPushResponse.serializer(),
            success = { it.wrapResultSuccess() },
            loggingErrorMessage = "pushSync failed",
        )

    override suspend fun uploadPhoto(photoId: String, checklistItemResultId: String, fileUri: String): Result<Unit> =
        ktorClient.executeQuery(
            query = {
                val bytes = readFileBytes(fileUri)
                ktorClient.post("/api/v1/mobile/photos/upload") {
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append("photoId", photoId)
                                append("checklistItemResultId", checklistItemResultId)
                                append(
                                    key = "file",
                                    value = bytes,
                                    headers = Headers.build {
                                        append(HttpHeaders.ContentType, "image/jpeg")
                                        append(HttpHeaders.ContentDisposition, "filename=\"photo.jpg\"")
                                    },
                                )
                            }
                        )
                    )
                }
            },
            deserializer = null,
            success = { Unit.wrapResultSuccess() },
            loggingErrorMessage = "uploadPhoto failed",
        )

    override suspend fun fetchConfigChanges(since: String): Result<Unit> =
        ktorClient.executeQuery(
            query = { ktorClient.get("/api/v1/mobile/config/changes?since=$since") },
            deserializer = null,
            success = { Unit.wrapResultSuccess() },
            loggingErrorMessage = "fetchConfigChanges failed",
        )
}

internal expect fun readFileBytes(fileUri: String): ByteArray
```

- [ ] **Step 3: Создать expect/actual для readFileBytes**

`androidMain/.../sync/api/FileReader.android.kt`:
```kotlin
package ru.mirea.toir.sync.api

import android.net.Uri
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.content.Context

internal actual fun readFileBytes(fileUri: String): ByteArray {
    // Android: read from content URI or file URI
    // This requires Context — consider refactoring to pass bytes explicitly
    // For now: simple file read for file:// URIs
    return java.io.File(android.net.Uri.parse(fileUri).path ?: error("Invalid URI")).readBytes()
}
```

`iosMain/.../sync/api/FileReader.ios.kt`:
```kotlin
package ru.mirea.toir.sync.api

import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL

internal actual fun readFileBytes(fileUri: String): ByteArray {
    val url = NSURL(string = fileUri)
    val data = NSData.dataWithContentsOfURL(url) ?: error("Cannot read file: $fileUri")
    return ByteArray(data.length.toInt()).also { bytes ->
        data.bytes?.let { ptr ->
            bytes.indices.forEach { i -> bytes[i] = (ptr as kotlinx.cinterop.ByteVar).value }
        }
    }
}
```

> ⚠️ iOS `readFileBytes` — упрощённая реализация. В production используй `NSData.bytes` через `kotlinx.cinterop` или передавай `ByteArray` напрямую вместо URI.

---

### Task 4: SyncRepository

**Files:**
- Create: `.../sync/SyncRepository.kt`
- Create: `.../sync/SyncRepositoryImpl.kt`

- [ ] **Step 1: SyncRepository**

```kotlin
package ru.mirea.toir.sync

internal interface SyncRepository {
    suspend fun pushPendingData(deviceId: String): Result<SyncResult>
    suspend fun uploadPendingPhotos(): Result<Int>
    suspend fun fetchDeltaChanges(): Result<Unit>
}

data class SyncResult(
    val acceptedCount: Int,
    val rejectedCount: Int,
)
```

- [ ] **Step 2: SyncRepositoryImpl**

```kotlin
package ru.mirea.toir.sync

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.dao.ActionLogDao
import ru.mirea.toir.core.database.dao.InspectionDao
import ru.mirea.toir.core.database.dao.PhotoDao
import ru.mirea.toir.core.database.dao.SyncMetaDao
import ru.mirea.toir.sync.api.SyncApiClient
import ru.mirea.toir.sync.api.models.RemoteSyncActionLog
import ru.mirea.toir.sync.api.models.RemoteSyncChecklistItemResult
import ru.mirea.toir.sync.api.models.RemoteSyncEquipmentResult
import ru.mirea.toir.sync.api.models.RemoteSyncInspection
import ru.mirea.toir.sync.api.models.RemoteSyncPushRequest
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class SyncRepositoryImpl(
    private val syncApiClient: SyncApiClient,
    private val inspectionDao: InspectionDao,
    private val photoDao: PhotoDao,
    private val actionLogDao: ActionLogDao,
    private val syncMetaDao: SyncMetaDao,
    private val coroutineDispatchers: CoroutineDispatchers,
) : SyncRepository {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun pushPendingData(deviceId: String): Result<SyncResult> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val pendingInspections = inspectionDao.selectPendingInspections()
                    val pendingEquipmentResults = inspectionDao.selectPendingEquipmentResults()
                    val pendingChecklistResults = inspectionDao.selectPendingChecklistItemResults()
                    val pendingLogs = actionLogDao.selectPending()

                    if (pendingInspections.isEmpty() && pendingEquipmentResults.isEmpty()
                        && pendingChecklistResults.isEmpty() && pendingLogs.isEmpty()
                    ) {
                        return@coRunCatching SyncResult(0, 0).wrapResultSuccess()
                    }

                    val request = RemoteSyncPushRequest(
                        clientBatchId = Uuid.random().toString(),
                        deviceId = deviceId,
                        sentAt = Clock.System.now().toString(),
                        inspections = pendingInspections.map { insp ->
                            RemoteSyncInspection(
                                id = insp.id,
                                assignmentId = insp.assignment_id,
                                routeId = insp.route_id,
                                status = insp.status,
                                startedAt = insp.started_at,
                                completedAt = insp.completed_at,
                            )
                        },
                        inspectionEquipmentResults = pendingEquipmentResults.map { r ->
                            RemoteSyncEquipmentResult(
                                id = r.id,
                                inspectionId = r.inspection_id,
                                routePointId = r.route_point_id,
                                equipmentId = r.equipment_id,
                                status = r.status,
                                startedAt = r.started_at,
                                completedAt = r.completed_at,
                            )
                        },
                        checklistItemResults = pendingChecklistResults.map { r ->
                            RemoteSyncChecklistItemResult(
                                id = r.id,
                                inspectionEquipmentResultId = r.inspection_equipment_result_id,
                                checklistItemId = r.checklist_item_id,
                                valueBoolean = r.value_boolean?.let { it == 1L },
                                valueNumber = r.value_number,
                                valueText = r.value_text,
                                valueSelect = r.value_select,
                                isConfirmed = r.is_confirmed == 1L,
                                answeredAt = r.answered_at,
                            )
                        },
                        actionLogs = pendingLogs.map { log ->
                            RemoteSyncActionLog(
                                id = log.id,
                                inspectionId = log.inspection_id,
                                actionType = log.action_type,
                                metadata = log.metadata,
                                createdAt = log.created_at,
                            )
                        },
                    )

                    val response = syncApiClient.pushSync(request).getOrThrow()

                    val acceptedInspections = response.accepted?.inspections.orEmpty()
                    val acceptedEquipment = response.accepted?.inspectionEquipmentResults.orEmpty()
                    val acceptedChecklist = response.accepted?.checklistItemResults.orEmpty()
                    val acceptedLogs = response.accepted?.actionLogs.orEmpty()

                    acceptedInspections.forEach { inspectionDao.updateInspectionSyncStatus(it, "SYNCED") }
                    acceptedEquipment.forEach { inspectionDao.updateEquipmentResultSyncStatus(it, "SYNCED") }
                    acceptedChecklist.forEach { inspectionDao.updateChecklistItemResultSyncStatus(it, "SYNCED") }
                    acceptedLogs.forEach { actionLogDao.updateSyncStatus(it, "SYNCED") }

                    val totalAccepted = acceptedInspections.size + acceptedEquipment.size +
                            acceptedChecklist.size + acceptedLogs.size
                    val totalRejected = response.rejected.orEmpty().size

                    SyncResult(acceptedCount = totalAccepted, rejectedCount = totalRejected).wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "pushPendingData failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override suspend fun uploadPendingPhotos(): Result<Int> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val pending = photoDao.selectPending()
                    var uploaded = 0
                    pending.forEach { photo ->
                        syncApiClient.uploadPhoto(
                            photoId = photo.id,
                            checklistItemResultId = photo.checklist_item_result_id,
                            fileUri = photo.file_uri,
                        ).onSuccess {
                            photoDao.updateSyncStatus(photo.id, "SYNCED", null)
                            uploaded++
                        }
                    }
                    uploaded.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "uploadPendingPhotos failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override suspend fun fetchDeltaChanges(): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val lastSync = syncMetaDao.selectByKey(SyncMetaDao.KEY_LAST_SYNC_TIME)
                        ?: "2000-01-01T00:00:00Z"
                    syncApiClient.fetchConfigChanges(lastSync).getOrThrow()
                    syncMetaDao.upsert(SyncMetaDao.KEY_LAST_SYNC_TIME, Clock.System.now().toString())
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "fetchDeltaChanges failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
```

---

### Task 5: SyncManager — фасад для запуска синхронизации

**Files:**
- Create: `.../sync/SyncManager.kt`

- [ ] **Step 1: SyncManager**

```kotlin
package ru.mirea.toir.sync

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.mirea.toir.common.coroutines.CoroutineDispatchers

class SyncManager(
    private val syncRepository: SyncRepository,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val deviceId: String,
) {
    private val scope = CoroutineScope(coroutineDispatchers.io + SupervisorJob())

    fun syncNow() {
        scope.launch {
            Napier.d("SyncManager: starting sync")
            syncRepository.pushPendingData(deviceId).onSuccess { result ->
                Napier.d("SyncManager: push success — accepted=${result.acceptedCount}, rejected=${result.rejectedCount}")
            }
            syncRepository.uploadPendingPhotos().onSuccess { count ->
                Napier.d("SyncManager: uploaded $count photos")
            }
            syncRepository.fetchDeltaChanges().onSuccess {
                Napier.d("SyncManager: delta sync done")
            }
        }
    }
}
```

---

### Task 6: Android WorkManager

**Files:**
- Create: `.../sync/worker/SyncWorker.kt` (androidMain)

- [ ] **Step 1: Создать SyncWorker**

```kotlin
package ru.mirea.toir.sync.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mirea.toir.sync.SyncManager

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val syncManager: SyncManager by inject()

    override suspend fun doWork(): Result {
        return try {
            syncManager.syncNow()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

- [ ] **Step 2: Создать SyncScheduler (Android)**

Создать `androidMain/.../sync/worker/SyncScheduler.kt`:
```kotlin
package ru.mirea.toir.sync.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private const val SYNC_WORK_NAME = "toir_periodic_sync"

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 2,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        ).setConstraints(constraints).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
```

---

### Task 7: DI-модуль

**Files:**
- Create: `.../sync/di/SyncManagerModule.kt`

- [ ] **Step 1: Создать SyncManagerModule**

```kotlin
package ru.mirea.toir.sync.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.sync.SyncManager
import ru.mirea.toir.sync.SyncRepository
import ru.mirea.toir.sync.SyncRepositoryImpl
import ru.mirea.toir.sync.api.SyncApiClient
import ru.mirea.toir.sync.api.SyncApiClientImpl

val syncManagerModule = module {
    factory<SyncApiClient> { new(::SyncApiClientImpl) }
    factory<SyncRepository> { new(::SyncRepositoryImpl) }
    single {
        SyncManager(
            syncRepository = get(),
            coroutineDispatchers = get(),
            deviceId = "device-android-001", // TODO: get from TokenStorage
        )
    }
}
```

> ⚠️ `deviceId` должен приходить из `TokenStorage` (создаётся в Waypoint 02). Обнови этот шаг: инжектируй `TokenStorage` из `feature-auth:impl` или вынеси интерфейс `DeviceIdProvider` в `core-domain`. Вариант 1 (быстрее): добавь `DeviceIdProvider` в `core-domain`:
> ```kotlin
> interface DeviceIdProvider { suspend fun getDeviceId(): String }
> ```
> Реализуй в `feature-auth:impl` и зарегистрируй в Koin.

---

### Task 8: Интеграция в App + Koin + запуск WorkManager

- [ ] **Step 1: Добавить syncManagerModule в Koin.kt**

```kotlin
import ru.mirea.toir.sync.di.syncManagerModule
// ...
syncManagerModule,
```

- [ ] **Step 2: Зарегистрировать WorkManager в Koin (Android)**

В `android/app/src/main/kotlin/.../MainApplication.kt` (или где происходит `initKoin`):
```kotlin
import org.koin.androidx.workmanager.koin.workManagerFactory
import ru.mirea.toir.sync.worker.SyncScheduler

// В initKoin добавить:
install(KoinAndroidContext)
workManagerFactory()

// После initKoin:
SyncScheduler.schedule(this)
```

- [ ] **Step 3: Добавить кнопку «Синхронизировать» в RoutesListScreen (опционально)**

В `RoutesListStore.Intent` добавить `data object SyncNow : Intent`.  
В `RoutesListExecutor` обработать: `get<SyncManager>().syncNow()`.  
В `RoutesListScreen` добавить кнопку в TopAppBar.

- [ ] **Step 4: Сборка**

```bash
./gradlew :android:app:assembleDebug
```
Ожидаемый результат: `BUILD SUCCESSFUL`

---

### Task 9: Финальная проверка end-to-end

- [ ] **Step 1: Проверить полный flow**
  1. Запустить приложение
  2. Авторизоваться → bootstrap
  3. Начать обход → открыть точку → карточка оборудования → чек-лист
  4. Заполнить все пункты (Boolean, Number, Text, Select, Confirm)
  5. Добавить фото к пункту с `requiresPhoto = true`
  6. Завершить проверку → вернуться к точкам
  7. Завершить обход
  8. Нажать «Синхронизировать» (если добавили кнопку) → наблюдать логи Napier

- [ ] **Step 2: Убедиться, что данные дошли до бэкенда**

```bash
# Проверить логи бэкенда или БД через psql
```

- [ ] **Step 3: Запустить Detekt**

```bash
./gradlew detekt
```
Ожидаемый результат: нет критических ошибок.

- [ ] **Step 4: Финальный commit**

```bash
git add shared/sync-manager/ settings.gradle.kts shared/main/ gradle/libs.versions.toml android/
git commit -m "feat: implement sync manager with push sync, photo upload, and WorkManager periodic sync"
```

---

## Итог

После Waypoint 08 приложение полностью реализует все сценарии из спецификации:

| Сценарий | Waypoint |
|----------|---------|
| Авторизация + JWT | 02 |
| Offline-first bootstrap | 03 |
| Список маршрутов | 04 |
| Точки маршрута + статусы | 05 |
| Карточка оборудования | 05 |
| Чек-лист (все типы) | 06 |
| Фотофиксация | 07 |
| Синхронизация с сервером | 08 |
| ActionLog | 01 (DB) + 08 (sync) |
