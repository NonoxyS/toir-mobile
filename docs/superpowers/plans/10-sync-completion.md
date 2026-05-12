# Waypoint 10 — Полное закрытие сценария синхронизации

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development` (рекомендуется) или `superpowers:executing-plans`. Шаги фиксируются чекбоксами (`- [ ]`).

**Цель:** Закрыть сценарий синхронизации так, чтобы он соответствовал функциональным требованиям ВКР («Функциональные требования к синхронизации данных», пп. 1–7) и `docs/specs/toir-mobile-spec.md` (раздел 6). Сейчас sync-manager реализует только периодический WorkManager-триггер каждые 2 часа без UI, ручного запуска, retry-политики и транзакционной целостности — это закрывает ≈40% требований ВКР.

**Источник истины по требованиям:** функциональные требования ВКР (раздел «Функциональные требования к синхронизации данных», пп. 1–7) + `docs/specs/toir-mobile-spec.md` §6 «Логика синхронизации».

**Платформы:** Android + iOS.

---

## 0. Контекст: текущее состояние

### Что уже сделано (sync-manager модуль)

- `shared/sync-manager/` — KMP-модуль с тремя операциями:
  - `pushPendingData()` → `POST /api/v1/mobile/sync/push` (inspections, equipmentResults, checklistItemResults, actionLogs)
  - `uploadPendingPhotos()` → multipart `POST /api/v1/mobile/photos/upload`
  - `fetchAndApplyDeltaChanges()` → `GET /api/v1/mobile/config/changes?since=...` + `ConfigChangesApplier`
- `SyncManager.syncNow()` — фасад с `Mutex` и логированием в `ActionLogger`.
- `SyncScheduler` на Android: `PeriodicWorkRequest`, интервал 2 часа, `NetworkType.CONNECTED`, `ExistingPeriodicWorkPolicy.KEEP`.
- `SyncMetaStorage` хранит `KEY_LAST_SYNC_TIME` (используется в delta-window и Bootstrap).

### Чего нет (gap-анализ против ВКР)

| Требование ВКР | Текущее состояние | Действие |
|---|---|---|
| 1. Сохранность пользовательских данных | Частично: FAILED-записи зависают навсегда | Phase 1 |
| 2.1 Загрузка маршрутов/чек-листов/нормативов | Реализовано через `fetchConfigChanges` | OK |
| 2.2 Обновление без дубликатов | upsert + `deletedIds` | OK |
| 3.1 Передача результатов (даты, измерения, отметки отклонений, комментарии, фото) | Реализовано | OK (см. §11) |
| 3.2 Подтверждение / описание ошибки | `RemoteSyncRejected.reason` есть, в UI не показывается | Phase 3 |
| 4. Локальное сохранение, авто после восстановления связи | Локально работает; авто-после-связи **нет** | Phase 2, Phase 4 |
| 4. Ручной запуск пользователем | **Нет** | Phase 3 |
| 5. Фиксация неуспешной передачи | Только Napier-лог | Phase 1 (поля), Phase 3 (UI) |
| 5. Уведомлять о несинхронизированных данных | **Нет** | Phase 3 |
| 5. Повторная попытка | Не работает (FAILED застревает) | Phase 1, Phase 4 |
| 5. Без дублирования (идемпотентность) | Клиент-сторона UUID, бэкенд идемпотентен | OK |
| 6. Разрешение конфликтов | Last-write-wins не реализован; applier перетирает безусловно | Phase 5 |
| 7. Невозможность частичной потери данных маршрута | `ConfigChangesApplier.apply` не в транзакции | Phase 1 |
| 7. Связность данных | FK в SQLDelight | OK |
| 7. Без дубликатов | upsert | OK |
| iOS — фоновая синхра | **Нет** (только Android) | Phase 6 |
| Интеграционные тесты | **Нет** | Phase 7 |

### Известные дефекты

1. `SyncRepositoryImpl.markRejectedAsFailed` ставит `LocalSyncStatus.FAILED` без поля счётчика попыток и времени следующей попытки — записи теряются для последующих push.
2. `SyncRepositoryImpl.pushPendingData` выбирает только `selectPending` — FAILED не подбираются.
3. `SyncRepositoryImpl.fetchAndApplyDeltaChanges` применяет ответ вне транзакции — при крэше посередине БД остаётся в неконсистентном состоянии.
4. `SyncManager.syncBlocking()` не проверяет результаты трёх шагов (свидетельствует о незакрытости контракта).
5. `SyncWorker.doWork` вызывает `syncManager.syncNow()` который возвращает `Job` и сразу возвращает `Result.success()` — Worker завершается до окончания фактической синхронизации. Это баг: WorkManager-механизм отчётности не работает.
6. Нет UI для отображения состояния синхронизации (планируется bottom sheet внутри `feature-routes-list/ui`, не отдельный feature-модуль — см. Phase 3).
7. `SyncScheduler` использует `ExistingPeriodicWorkPolicy.KEEP` — при изменении интервала на установленном устройстве конфиг не обновится.

---

## 1. Архитектурные решения

### 1.1 Триггеры синхронизации (полный список)

| Триггер | Когда | Реализация |
|---|---|---|
| Периодический Android | каждые N часов (по умолчанию 2) | `SyncScheduler` (есть) |
| Периодический iOS | при wake-up BGTask | `IosSyncScheduler` (новый, Phase 6) |
| Ручной | пользователь жмёт «Синхронизировать» / pull-to-refresh | `SyncManager.requestSync(trigger=Manual)` (Phase 3) |
| Авто после обхода | `finishInspection` успешно сохранил inspection | вызов `syncManager.requestSync(trigger=AfterInspection)` (Phase 2) |
| Восстановление сети | `NetworkMonitor` зафиксировал переход offline→online | `syncManager.requestSync(trigger=Connectivity)` (Phase 4) |

### 1.2 Состояние синхронизации (UI)

`SyncManager.runOnce` возвращает обычный `kotlin.Result<Unit>` — никакой кастомной обёртки результата. Отдельно публикуется состояние во времени через `StateFlow<SyncStatus>`:

```kotlin
sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Running : SyncStatus
    data class Success(val finishedAt: Instant, val pushed: Int, val uploaded: Int) : SyncStatus
    data class Failed(val finishedAt: Instant, val reason: SyncFailureReason) : SyncStatus
}

enum class SyncFailureReason { NETWORK, AUTH, SERVER, UNKNOWN }
```

`SyncManager` экспонирует:
- `suspend fun runOnce(trigger: SyncTrigger): Result<Unit>` — синхронный, для Worker и явных вызовов.
- `fun syncNow(trigger: SyncTrigger): Job` — fire-and-forget обёртка над `runOnce`.
- `val status: StateFlow<SyncStatus>` — для UI.
- `val pendingCount: Flow<Int>` — combine over `inspectionStorage`, `photoStorage`, `actionLogStorage` (SQLDelight Flow).

`SyncFailureReason` нужен только для UI snackbar (живёт внутри `SyncStatus.Failed`), не для решения «retry vs fail» в WorkManager.

UI подписывается из `routes-list` (бейдж в Top App Bar).

### 1.3 Retry-политика (Phase 1, §3)

ВКР п.4–5 требует «автоматическую передачу после восстановления соединения» и «обеспечивать повторную попытку передачи». Решение:

- Удалить семантику терминального `LocalSyncStatus.FAILED` для пользовательских данных. Использовать только `PENDING` / `SYNCED`.
- Завести новые поля в `Inspection`, `InspectionEquipmentResult`, `ChecklistItemResult`, `ActionLog`, `Photo`:
  - `sync_attempt_count INTEGER NOT NULL DEFAULT 0`
  - `sync_next_attempt_at TEXT NULL` (ISO instant)
  - `sync_last_error TEXT NULL`
- При reject backend → `attempt_count += 1`, `next_attempt_at = now + backoff(attempt_count)`, `last_error = reason`, статус остаётся `PENDING`.
- При выборке pending — фильтровать `sync_next_attempt_at IS NULL OR sync_next_attempt_at <= now()`.
- Backoff: `min(2^attempt_count * 30s, 1h)` — capped.

`LocalSyncStatus.FAILED` оставляем как enum-значение (схема БД), но мобильный клиент его не пишет: помечаем `@Deprecated`. Чистка значения — после прохождения миграции и удаления всех значений `FAILED` из боевых БД (отдельный PR, не в scope этого waypoint).

### 1.4 Транзакции (Phase 1, §1)

Все методы applier-ов и push-update-status оборачиваются в `database.transaction { }`. Гарантирует ВКР п.7: «невозможность частичной потери».

```kotlin
// shared/core-database/.../ConfigChangesApplier.kt
db.transaction {
    response.locations.forEach { ... }
    response.equipment.forEach { ... }
    ...
}
```

`pushPendingData`: после получения `RemoteSyncPushResponse` все `updateInspectionSyncStatus`, `updateEquipmentResultSyncStatus`, `updateChecklistItemResultSyncStatus`, `updateSyncStatus` (action logs), `markRejectedAsFailed` — внутри одной транзакции.

### 1.5 Конфликты (Phase 5)

Регламент ВКР п.6 не задан явно. Берём правило, явно прописанное в spec и реализуемое:

- **Конфиг (routes, equipment, locations, checklists, assignments):** server-wins. Клиент никогда не пишет в эти таблицы напрямую (только через applier). Уже работает.
- **Inspection / EquipmentResult / ChecklistItemResult:** client-wins до момента передачи. После `SYNCED` backend становится source of truth. Backend в этом waypoint не возвращает эти сущности в `fetchConfigChanges` (см. `RemoteConfigChangesResponse`) → фактического конфликта нет на стороне клиента.
- **Assignment.status:** server-wins (бэк пересчитывает из inspections). Это уже корректно.

В applier добавляем явный optimistic check по `updated_at`: если в БД локальный `updated_at >= remote.updatedAt`, запись пропускается. Защита от поздних/повторных delta-ответов.

### 1.6 Корректный SyncWorker (Phase 1, §5)

```kotlin
class SyncWorker(...) : CoroutineWorker(context, params), KoinComponent {
    private val syncManager: SyncManager by inject()
    override suspend fun doWork(): androidx.work.ListenableWorker.Result =
        if (syncManager.runOnce(trigger = SyncTrigger.Periodic).isSuccess) {
            androidx.work.ListenableWorker.Result.success()
        } else {
            androidx.work.ListenableWorker.Result.retry()
        }
}
```

`SyncManager.runOnce` теперь `suspend`-метод, возвращающий `Result<Unit>`. WorkManager уже делает exponential backoff на `retry()` — отдельная классификация retryable/fatal не нужна.

### 1.7 NetworkMonitor (Phase 4)

Новый KMP API в `shared/core-network` (или в `sync-manager` — решить в реализации, см. Task 4.1):

```kotlin
interface NetworkMonitor {
    val isOnline: StateFlow<Boolean>
}
```

- Android: `ConnectivityManager.registerNetworkCallback` с `NetworkCapabilities.NET_CAPABILITY_INTERNET + VALIDATED`.
- iOS: `NWPathMonitor`, ассоциированный с `dispatch_queue`.

`SyncManager` подписывается на `isOnline.distinctUntilChanged().filter { it }` → `runOnce(trigger = Connectivity)`.

### 1.8 iOS периодика (Phase 6)

`BGTaskScheduler`:
- Регистрируем идентификатор `ru.mirea.toir.sync.periodic` в `Info.plist` (Permitted background task scheduler identifiers).
- В `iosApp` при `application:didFinishLaunchingWithOptions:` регистрируем handler и сабмитим `BGAppRefreshTaskRequest` с `earliestBeginDate = Date(timeIntervalSinceNow: 2*3600)`.
- Хендлер вызывает `syncManager.runOnce(trigger = SyncTrigger.Periodic)` и завершает task. После завершения снова сабмитит следующий request.

KMP-обёртка: `IosSyncScheduler` (iosMain), вызывается из Swift bridging-кода. Swift код небольшой — оставляем в `iosApp/iosApp/SyncBootstrap.swift`.

---

## 2. Файловая структура (новое и изменённое)

```
shared/sync-manager/src/
├── commonMain/kotlin/ru/mirea/toir/sync/
│   ├── domain/
│   │   ├── SyncManager.kt                  [edit: runOnce, status, pendingCount]
│   │   ├── SyncTrigger.kt                  [new]
│   │   ├── SyncStatus.kt                   [new — includes SyncFailureReason]
│   │   ├── network/NetworkMonitor.kt       [new — expect]
│   │   ├── retry/BackoffPolicy.kt          [new]
│   │   └── repository/SyncRepository.kt    [edit: new shapes]
│   ├── data/
│   │   ├── repository/SyncRepositoryImpl.kt           [edit: tx, retry, dedup]
│   │   ├── applier/ConfigChangesApplier.kt            [edit: tx, updated_at guard]
│   │   └── network/SyncApiClientImpl.kt               [edit: error classification]
│   └── di/SyncManagerModule.kt                        [edit]
├── androidMain/kotlin/ru/mirea/toir/sync/
│   ├── network/NetworkMonitor.android.kt              [new — actual]
│   └── worker/SyncScheduler.kt                        [edit: UPDATE policy]
│   └── worker/SyncWorker.kt                           [edit: runOnce]
├── iosMain/kotlin/ru/mirea/toir/sync/
│   ├── network/NetworkMonitor.ios.kt                  [new — actual, NWPathMonitor]
│   └── scheduler/IosSyncScheduler.kt                  [new]
└── commonTest/kotlin/ru/mirea/toir/sync/               [new — Phase 7]
    ├── SyncRepositoryImplTest.kt
    ├── ConfigChangesApplierTest.kt
    ├── BackoffPolicyTest.kt
    └── fixtures/InMemoryToirDatabase.kt

shared/core-database/src/commonMain/
├── sqldelight/ru/mirea/toir/core/database/
│   ├── Inspection.sq                  [edit: add sync_attempt_count, sync_next_attempt_at, sync_last_error]
│   ├── InspectionEquipmentResult.sq   [edit: same]
│   ├── ChecklistItemResult.sq         [edit: same]
│   ├── ActionLog.sq                   [edit: same]
│   ├── Photo.sq                       [edit: same]
│   ├── SyncMeta.sq                    [edit: new keys]
│   └── migrations/3.sqm               [new — миграция версии БД]
└── kotlin/.../storage/sync_meta/SyncMetaStorage.kt    [edit: new keys]

android/app/src/main/kotlin/ru/mirea/toir/app/
└── Application.kt                                      [edit: NetworkMonitor init]

iosApp/iosApp/
├── Info.plist                                          [edit: BGTaskSchedulerPermittedIdentifiers]
├── iosApp.swift                                        [edit: register BG task]
└── SyncBootstrap.swift                                 [new]

shared/feature-route-points/impl/src/commonMain/
└── kotlin/.../data/repository/RoutePointsRepositoryImpl.kt  [edit: trigger syncNow after finishInspection]

shared/feature-routes-list/impl/src/commonMain/
└── kotlin/.../data/repository/RoutesListRepositoryImpl.kt   [edit: combine with pending count]

shared/feature-routes-list/api/src/commonMain/
└── kotlin/.../models/RoutesListSyncIndicator.kt             [new]

shared/feature-routes-list/presentation/...                  [edit: indicator state, sheet visibility]
shared/feature-routes-list/ui/components/SyncIndicatorIcon.kt [new]
shared/feature-routes-list/ui/components/SyncStatusBottomSheet.kt [new — bottom sheet, отдельного экрана нет]
shared/feature-routes-list/ui/RouteCard.kt                   [edit: pending-sync stripe]

docs/design-system/pages/routes-list.md                      [edit — добавлен раздел Bottom sheet (вместо нового sync-status.md)]
```

---

## 3. Фазы реализации

Каждая фаза = отдельный PR. Порядок строгий: следующая фаза опирается на предыдущую.

### Phase 1 — Доменные исправления (foundation)

Цель: устранить блокеры (FAILED-семантика, отсутствие транзакций, неработающий SyncWorker, отсутствие структурированного статуса). Без UI-изменений.

#### Task 1.1: Расширение схемы (`sync_attempt_count`, `sync_next_attempt_at`, `sync_last_error`)

> **Без миграции** — правим CREATE TABLE напрямую (приложение в активной разработке, продакшен-БД ещё нет).

- [ ] **Step 1:** В `Inspection.sq`, `InspectionEquipmentResult.sq`, `ChecklistItemResult.sq`, `ActionLog.sq`, `Photo.sq` добавить в CREATE TABLE:
  ```
  sync_attempt_count INTEGER NOT NULL DEFAULT 0,
  sync_next_attempt_at TEXT,
  sync_last_error TEXT
  ```
- [ ] **Step 2:** В каждом `.sq` заменить `selectPending` → `selectPendingReady(now: TEXT)` с условием `sync_status = 'pending' AND (sync_next_attempt_at IS NULL OR sync_next_attempt_at <= :now)`.
- [ ] **Step 3:** Добавить queries:
  - `markRetryScheduled(id, attemptCount, nextAt, reason)` — обновляет три новые колонки, статус остаётся PENDING.
  - `markSynced(id)` — `sync_status='synced'`, обнуляет `attempt_count=0`, `next_attempt_at=NULL`, `last_error=NULL`.
  - `selectPendingCount` (Long) для UI.
- [ ] **Step 4:** Обновить `InspectionStorage`, `PhotoStorage`, `ActionLogStorage` интерфейсы и реализации: новые методы вместо `selectPending` / `updateXSyncStatus`.
- [ ] **Step 5:** Найти всех потребителей старых сигнатур (`SyncRepositoryImpl` — переезжает в Task 1.5; других не должно быть).
- [ ] **Step 6:** Сборка `./gradlew :shared:core-database:assemble` зелёная.

#### Task 1.2: `BackoffPolicy`

- [ ] Создать `sync/domain/retry/BackoffPolicy.kt`:
  ```kotlin
  internal object BackoffPolicy {
      private val MAX = 1.hours
      private val BASE = 30.seconds
      fun nextDelay(attemptCount: Int): Duration =
          minOf(BASE * (1 shl (attemptCount - 1).coerceIn(0, 10)), MAX)
  }
  ```
- [ ] Юнит-тест в `commonTest`: для attempt=1 → 30s; attempt=5 → 8m; attempt=12 → 1h (cap).

#### Task 1.3: `SyncTrigger`, `SyncStatus` (+ `SyncFailureReason`)

- [ ] Создать `SyncTrigger.kt` (enum: `Periodic`, `Manual`, `AfterInspection`, `Connectivity`, `Bootstrap`) и `SyncStatus.kt` (sealed interface + `enum class SyncFailureReason` в том же файле — это его единственный пользователь).
- [ ] `SyncFailureReason` классифицируется из `Throwable`: сетевые → `NETWORK`, `HttpStatusCode.Unauthorized` → `AUTH`, 5xx → `SERVER`, остальное → `UNKNOWN`. Реализовать `fun Throwable.toSyncFailureReason(): SyncFailureReason`.

#### Task 1.4: Транзакции в `ConfigChangesApplier`

- [ ] Инжектировать `ToirDatabase` в applier (или прокси-обёртку `TransactionRunner` чтобы не утекал SQLDelight в domain).
- [ ] Обернуть `apply()` в `db.transaction { }`.
- [ ] Добавить optimistic check `updated_at` для assignments, routes, routePoints, equipment, locations, checklists, checklistItems.

#### Task 1.5: Транзакции и retry в `SyncRepositoryImpl.pushPendingData`

- [ ] Перейти на `selectPendingReady(now)`.
- [ ] Все updates по результату `RemoteSyncPushResponse` — внутри одной транзакции.
- [ ] Удалить `markRejectedAsFailed`. Вместо неё `markRejectedForRetry`:
  ```kotlin
  private fun markRejectedForRetry(rejected: RemoteSyncRejected, db: ToirDatabase) {
      val (attempt, next) = computeNextAttempt(rejected.entityType, rejected.entityId)
      when (rejected.entityType) {
          INSPECTION -> inspectionStorage.markInspectionRetry(rejected.entityId, attempt, next, rejected.reason.name)
          ...
      }
  }
  ```
- [ ] При успешном accepted — `markSynced(id)` (сбрасывает счётчик).

#### Task 1.6: `SyncManager.runOnce` + StateFlow статуса

- [ ] Рефакторить `SyncManager`:
  ```kotlin
  class SyncManager internal constructor(...) {
      private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
      val status: StateFlow<SyncStatus> = _status.asStateFlow()
      val pendingCount: Flow<Int> = combine(
          inspectionStorage.observePendingCount(),
          photoStorage.observePendingCount(),
          actionLogStorage.observePendingCount(),
      ) { a, b, c -> a + b + c }

      suspend fun runOnce(trigger: SyncTrigger): SyncRunResult { ... }
      fun syncNow(trigger: SyncTrigger): Job = scope.launch { runOnce(trigger) }
  }
  ```
- [ ] `runOnce` устанавливает `_status = Running`, в конце — `Success(...)` или `Failed(finishedAt, reason)`. Возвращает `Result.success(Unit)` или `Result.failure(throwable)`. Записывает в `SyncMetaStorage` ключи `KEY_LAST_SYNC_AT_SUCCESS`, `KEY_LAST_SYNC_ERROR_REASON`, `KEY_LAST_SYNC_ERROR_AT`.
- [ ] Внутри `runOnce` — три шага. Первая ошибка → останавливаемся, возвращаем `Result.failure`. Worker сам решит retry через WorkManager backoff.
- [ ] Удалить `syncBlocking()`.

#### Task 1.7: Исправить `SyncWorker`

- [ ] `doWork` теперь `suspend` вызывает `runOnce(Periodic)` и маппит результат.
- [ ] `SyncScheduler` сменить `ExistingPeriodicWorkPolicy.KEEP` → `UPDATE`.

#### Task 1.8: Новые ключи `SyncMetaStorage`

- [ ] В `SyncMetaStorage.companion` добавить:
  - `KEY_LAST_SYNC_AT_SUCCESS = "last_sync_at_success"`
  - `KEY_LAST_SYNC_ERROR_REASON = "last_sync_error_reason"`
  - `KEY_LAST_SYNC_ERROR_AT = "last_sync_error_at"`
- [ ] Добавить `observeByKey(key): Flow<String?>` (SQLDelight Flow) — пригодится UI в Phase 3.

#### Phase 1 — Verification

- [ ] `./gradlew :shared:core-database:assemble :shared:sync-manager:assemble` зелёный.
- [ ] `./gradlew :shared:sync-manager:allTests` — BackoffPolicy + SyncFailureReason classifier тесты проходят.
- [ ] Smoke: на эмуляторе после периодического срабатывания WorkManager (форсировать через ADB: `adb shell cmd jobscheduler run -f ru.mirea.toir.dev <jobid>`) видим в БД, что отвергнутые записи получили `sync_attempt_count=1` и `sync_next_attempt_at` в будущем, статус остался `PENDING`.
- [ ] Коммит: `feat(sync): retry policy, transactions, structured status (Phase 1)`.

---

### Phase 2 — Авто-sync после завершения обхода ✅

**Цель:** ВКР п.3 — статус задания становится «выполнено» после успешной передачи.

**Суть:** инжектировать `SyncManager` в `feature-route-points`, в `RoutePointsRepositoryImpl.finishInspection` после `actionLogger.log(INSPECTION_COMPLETED, …)` вызывать `syncManager.syncNow(SyncTrigger.AfterInspection)` (fire-and-forget).

- [x] `shared/feature-route-points/impl/build.gradle.kts`: добавлен `projects.shared.syncManager` в `implementations`.
- [x] `RoutePointsRepositoryImpl`: добавлен `syncManager: SyncManager` в конструктор; вызов `syncManager.syncNow(SyncTrigger.AfterInspection)` после `actionLogger.log(INSPECTION_COMPLETED, …)`.
- [x] DI без правок: Koin DSL `new(::RoutePointsRepositoryImpl)` сам резолвит `SyncManager` (зарегистрирован как `single` в `syncManagerModule`).
- [x] `./gradlew :shared:feature-route-points:impl:assemble` зелёный.
- [x] `./gradlew :android:app:assembleDebug` зелёный.

**Verification (smoke на устройстве):** в logcat виден старт sync через ≤1с после завершения; offline → статус `Failed(NETWORK)`, данные остались PENDING. **Будет проверено в Phase 3 smoke-тесте, когда появится UI-индикатор.**

---

### Phase 3 — UI: ручной триггер, last-sync, индикатор в App Bar

Цель: ВКР п.4 «ручной запуск», п.5 «уведомлять о несинхронизированных данных», 3.2 «описание ошибки».

**Дизайн-система:** работаем строго по `docs/design-system/MASTER.md` и `docs/design-system/pages/routes-list.md`. DS уже описывает:
- Sync-иконку в правом краю App Bar (`color.success` ✓ / `color.warning` ⇅ / `color.error` ✕ / spin при активной синхронизации). Тап → экран «Статус синхронизации».
- Левая полоска 3dp `color.sync` + иконка ⇅ на карточках в состоянии «Ожидает синхр.».
- Текстовое состояние карточки «Завершён, не синхр.» в порядке сортировки.

Никаких отдельных «банеров» под App Bar — это противоречит DS. Только иконка в App Bar + **bottom sheet «Статус синхронизации»** (отдельного экрана нет — решение зафиксировано 2026-05-12 после ревью: информации мало, пустой экран = over-engineering, ломает контекст списка маршрутов).

#### Task 3.0: Обновлён DS (`routes-list.md`) — bottom sheet вместо экрана ✅

- [x] В `docs/design-system/pages/routes-list.md` зафиксирован раздел «Bottom sheet «Статус синхронизации»» с ASCII-мокапом, поведением, состояниями.
- [x] Pin `feedback_ui_overengineering`: не делать отдельный screen ради 3 строк информации.

> Дизайн-аудит `ui-ux-pro-max:ui-ux-pro-max` НЕ вызывается — DS уже содержит исчерпывающую спеку. Если в процессе появятся развилки (анимации, цвета, размеры) — поднять отдельно.

#### Task 3.1: Доменные потоки и проекция UI-состояния

- [ ] В `feature-routes-list/api/models/` добавить `RoutesListSyncIndicator`:
  ```kotlin
  data class RoutesListSyncIndicator(
      val isRunning: Boolean,
      val pendingCount: Int,
      val lastError: SyncFailureReason?, // null если последняя синхра успешна
  )
  ```
  (Соответствует четырём состояниям иконки из DS: spin / ⇅ warning / ✕ error / ✓ success.)
- [ ] В `RoutesListRepository` добавить `fun observeSyncIndicator(): Flow<RoutesListSyncIndicator>` — combine из `SyncManager.status` + `SyncManager.pendingCount` + `syncMetaStorage.observeByKey(KEY_LAST_SYNC_ERROR_REASON)`.

#### Task 3.2: Sync-иконка в App Bar (RoutesList)

- [ ] В `feature-routes-list/ui/` создать `SyncIndicatorIcon` Composable (поместить в `ui/components/`, не в общий `common-ui` — пока используется в одном экране):
  - `isRunning=true` → `Icon(refresh)` с `infiniteRotation`, цвет `MaterialTheme.colorScheme.onSurface`.
  - `pendingCount > 0, lastError == null` → `Icon(sync_alt)` с `color.warning`.
  - `lastError != null` → `Icon(error_outline)` с `color.error`.
  - иначе → `Icon(check_circle)` с `color.success`.
  - Тап-таргет 44dp (через `Modifier.size(44.dp)` или Material IconButton).
- [ ] Тап → открывает `SyncStatusBottomSheet` (Task 3.4) — установка `state.isSyncSheetVisible = true` через `Intent.OnSyncIndicatorClicked`.
- [ ] **Long-press НЕ реализуем** — скрытый жест, не соответствует accessibility; ручной запуск только через кнопку в bottom sheet.

#### Task 3.3: Левая полоска и состояние карточки «Ожидает синхр.»

- [ ] В `RoutesListRepositoryImpl` уже есть `hasPendingSync` (см. `RoutesListRepositoryImpl.kt:77`). Прокинуть значение в `DomainRoute` → `UiRoute` → карточку.
- [ ] В `RouteCard` Composable добавить:
  - левая полоска `Modifier.drawBehind { ... width=3.dp, color = colorScheme.sync }` при `hasPendingSync=true`;
  - иконка ⇅ в строке метаданных при `hasPendingSync=true`.
- [ ] Добавить `color.sync` в `shared/common-ui/.../theme/Colors.kt` если ещё нет (проверить — должно быть; иначе — отдельный коммит в Theme).

#### Task 3.4: `SyncStatusBottomSheet` Composable

> Реализуется как Composable внутри `feature-routes-list/ui/components/SyncStatusBottomSheet.kt`. Отдельный feature-модуль НЕ создаётся (отказались от `feature-sync-status` — over-engineering для 3 строк информации).

- [ ] `SyncStatusBottomSheet(state: RoutesListSyncIndicator, lastSuccessAt: Instant?, lastFailedAt: Instant?, isVisible: Boolean, onDismiss: () -> Unit, onSyncNow: () -> Unit)`.
- [ ] Material 3 `ModalBottomSheet` с `sheetState = rememberModalBottomSheetState()`. Top corners — `radius.lg` (16dp).
- [ ] Содержимое (по спеке `routes-list.md` → раздел «Bottom sheet»):
  - Заголовок «Статус синхронизации» (`type.displayMedium`).
  - Карточка «Последняя синхронизация»: время + статус-иконка (✓ success / ✕ error) + текст причины при `Failed`.
  - Карточка «Ожидают отправки»: счётчик (`type.displayLarge`) или текст «Все данные отправлены» при `pendingCount=0`.
  - Кнопка `Primary` «Синхронизировать сейчас» (disabled при `isRunning`). При `isRunning` — progress bar height 4dp `color.warning` вместо кнопки.
- [ ] При `onSyncNow` — `syncManager.syncNow(SyncTrigger.Manual)`. Sheet не закрывается автоматически — пользователь видит переход в Running → Success/Failed.
- [ ] Тексты ошибок из Task 3.6.

#### Task 3.5: ~~Snackbar при ручном вызове из App Bar~~ — удалено

Long-press жест отказались делать (см. Task 3.2). Ручной запуск только через bottom sheet, где результат виден напрямую в карточке «Последняя синхронизация». Snackbar избыточен.

#### Task 3.6: Локализация

- [ ] В `shared/common-resources/src/commonMain/moko-resources/base/strings.xml`:
  ```xml
  <string name="sync_status_title">Статус синхронизации</string>
  <string name="sync_status_last_success">Последняя синхронизация: %s</string>
  <string name="sync_status_never">Синхронизации ещё не было</string>
  <string name="sync_status_running">Идёт синхронизация…</string>
  <string name="sync_status_pending_count">Ожидают отправки: %d</string>
  <string name="sync_action_manual">Синхронизировать сейчас</string>
  <string name="sync_error_network">Нет соединения</string>
  <string name="sync_error_auth">Сессия истекла, войдите заново</string>
  <string name="sync_error_server">Сервер недоступен, повторите позже</string>
  <string name="sync_error_unknown">Не удалось синхронизировать</string>
  ```

#### Task 3.7: Презентация и executor routes-list

- [ ] В `RoutesListState` добавить `syncIndicator: RoutesListSyncIndicator`, `isSyncSheetVisible: Boolean`, `syncLastSuccessAt: Instant?`, `syncLastFailedAt: Instant?`.
- [ ] В `RoutesListReducer`: `Msg.SyncIndicatorChanged(...)`, `Msg.SyncSheetVisibilityChanged(Boolean)`.
- [ ] В `RoutesListExecutor`: подписка на `repository.observeSyncIndicator()` + `observeByKey(KEY_LAST_SYNC_AT_SUCCESS / KEY_LAST_SYNC_ERROR_AT)` в `Action.Init`.
- [ ] Intent-ы: `OnSyncIndicatorClicked` → `SyncSheetVisibilityChanged(true)`; `OnSyncSheetDismissed` → `SyncSheetVisibilityChanged(false)`; `OnSyncNowClicked` → `syncManager.syncNow(SyncTrigger.Manual)`.

#### Phase 3 — Verification

- [ ] Скриншоты четырёх состояний sync-иконки в App Bar приложены к PR.
- [ ] Скриншот карточки в состоянии «Ожидает синхр.» с полоской `color.sync` приложен.
- [ ] Скриншот bottom sheet «Статус синхронизации» в четырёх состояниях (Idle/Running/Success/Failed) приложен.
- [ ] Smoke на устройстве:
  - В оффлайне завершить обход → иконка переходит в `color.warning` ⇅, на карточке появляется полоска.
  - Включить сеть → тап на иконку → открывается bottom sheet → кнопка «Синхронизировать сейчас» → spin → success ✓.
  - Намеренный 500 (через wrong baseUrl) → иконка `color.error` ✕, текст ошибки в карточке «Последняя синхронизация».
- [ ] UI-юнит тесты для `RoutesListReducer` на `Msg.SyncIndicatorChanged` и `Msg.SyncSheetVisibilityChanged`.
- [ ] Коммит: `feat(sync): manual trigger, status sheet, app-bar indicator (Phase 3)`.

---

### Phase 4 — Сетевой триггер NetworkMonitor (краткая спека)

**Цель:** ВКР п.4 «автоматическая передача после восстановления соединения».

**Суть:** `expect interface NetworkMonitor { val isOnline: StateFlow<Boolean> }`.
- Android actual: `ConnectivityManager.registerNetworkCallback` с `NET_CAPABILITY_INTERNET + VALIDATED`.
- iOS actual: `NWPathMonitor` на dispatch_queue.
- В `SyncManager.init`: подписка `isOnline.drop(1).filter { it }.debounce(1.seconds).onEach { syncNow(Connectivity) }`.

**Verification:** offline → завершение обхода → online → ≤2с старт sync на обеих платформах.

**Детальную спеку зафиксировать перед началом фазы.**

---

### Phase 5 — Конфликты и идемпотентность (краткая спека)

**Цель:** ВКР п.6, защита от поздних/дублирующих delta-ответов.

**Суть:** в `ConfigChangesApplier.apply` перед каждым upsert сравнивать `local.updatedAt >= remote.updatedAt` → skip. Применить ко всем сущностям с `updatedAt` (assignments, routes, routePoints, equipment, locations, checklists, checklistItems). Проверить, что markSynced идемпотентен; добавить early-return по `photoStorage.storageKey` для повторных photo accept.

**Verification:** unit-тест applier-а пропускает stale; двойной apply одного и того же delta не создаёт изменений.

**Детальную спеку зафиксировать перед началом фазы.**

---

### Phase 6 — iOS BGTaskScheduler (краткая спека)

**Цель:** паритет с Android-периодикой.

**Суть:**
- В `iosApp/iosApp/Info.plist`: `BGTaskSchedulerPermittedIdentifiers = ["ru.mirea.toir.sync.periodic"]`, `UIBackgroundModes = [fetch, processing]`.
- Swift `SyncBootstrap.swift`: регистрация BGTask handler + `BGAppRefreshTaskRequest` с `earliestBeginDate = now + 2h`. Вызов из `iOSApp.init`.
- KMP `IosSyncRunner.kt` (iosMain): мост к `syncManager.runOnce(Periodic)`, callback с `Boolean`.

**Verification:** Xcode → Debug → Simulate Background Fetch → sync запускается, виден в Console.app.

**Детальную спеку зафиксировать перед началом фазы.**

---

### Phase 7 — Интеграционные тесты (краткая спека)

**Цель:** надёжность sync-сценария; закрывает `project_test_debt` из памяти.

**Суть:**
- In-memory SQLDelight инфра в `shared/core-database/src/commonTest/` (JVM `JdbcSqliteDriver(IN_MEMORY)` + Schema.create + seed-хелперы).
- Ktor `MockEngine` хелпер `fakeSyncApiClient(...)` с пред-настроенными ответами.
- Тесты в `shared/sync-manager/src/commonTest/`: `SyncRepositoryImplTest` (empty/accepted/rejected/network/auth/photo/delta/stale/idempotent), `BackoffPolicyTest`, `SyncManagerTest` (mutex/status emissions).

**Verification:** `:shared:sync-manager:allTests` зелёный, покрытие новых методов > 80%.

**Детальную спеку зафиксировать перед началом фазы.**

---

## 4. Приёмочные критерии (mapping к ВКР)

| Требование ВКР | Критерий приёмки | Способ проверки |
|---|---|---|
| 1. Сохранность данных | После любого сбоя при push неотправленные записи остаются `PENDING` с `attempt_count++` и доступны при следующей синхре | unit-test + smoke offline → online |
| 2.1 Загрузка маршрутов | После Bootstrap пользователь видит назначенные маршруты в `routes-list` без сети | существующее поведение, не регрессировано |
| 2.2 Обновление без дубликатов | Повторный delta-fetch не создаёт дублей, не обнуляет более свежие локальные данные | unit applier-test + smoke |
| 3.1 Передача результатов | Бэкенд получает inspection/equipment/checklist/actionLogs/photos с client-UUID | в существующих тестах + ручная сверка с API логом |
| 3.2 Подтверждение / описание ошибки | Bottom sheet «Статус синхронизации» отображает текст причины при `Failed` | UI smoke в Phase 3 |
| 4. Offline работа | Полный сценарий «логин → старт → завершение обхода» в режиме полёта работает | smoke E2E |
| 4. Авто после восстановления | При появлении сети sync стартует в течение ≤ 2 с | smoke в Phase 4 |
| 4. Ручной запуск | Кнопка «Синхронизировать сейчас» в bottom sheet «Статус синхронизации» запускает sync | smoke в Phase 3 |
| 5. Фиксация неуспешной | `KEY_LAST_SYNC_ERROR_*` заполнены, видны в bottom sheet «Статус синхронизации» | smoke в Phase 3 |
| 5. Уведомлять о PENDING | Иконка App Bar в `color.warning` + полоска `color.sync` на карточке + счётчик в bottom sheet | smoke в Phase 3 |
| 5. Повторная попытка | Через backoff sync дёргается повторно, данные уходят | smoke + unit |
| 5. Без дублирования | Бэкенд idempotent + `markSynced` no-op на SYNCED | unit Phase 5 + ручная проверка API логов |
| 6. Конфликты | `updated_at` guard в applier; client-wins до push | unit Phase 5 |
| 7. Невозможность частичной потери | `db.transaction { }` обёртки | unit + ручной kill-test (kill app в момент apply) |
| 7. Связность данных | FK + транзакции | существующие SQLDelight FK |

---

## 5. Риски и открытые вопросы

| Риск | Митигация |
|---|---|
| Миграция БД 2→3 на устройствах с боевыми данными | Миграция аддитивная (только ADD COLUMN с DEFAULT). Тест на in-memory + ручной апгрейд из существующей БД. |
| `NWPathMonitor` на iOS не всегда сразу определяет VALIDATED-соединение | Дополнительная проверка через первый `ktorClient` запрос с коротким timeout. Опционально, не в scope. |
| BGTask на iOS не запускается на симуляторе по расписанию | Smoke через `e -l objc -- (void)[[BGTaskScheduler sharedScheduler] _simulateLaunchForTaskWithIdentifier:@"ru.mirea.toir.sync.periodic"]`. |
| `WhileSubscribed` для `pendingCount` — может стрелять лишним запросом БД | Уже-замеморизированные SQLDelight Flow + `combine` дешёвые. |
| Конфликт «manual sync» во время «periodic» | `Mutex.tryLock` — второй вызов skip; в UI кнопка отключается на `Running`. |
| Регламент конфликтов из ВКР п.6 расплывчат | Зафиксировано: server-wins для конфига, client-wins до push для пользовательских данных. Если регламент ВКР другой — пересматриваем Phase 5. |

---

## 6. Что **не** в scope этого waypoint

- Удаление `LocalSyncStatus.FAILED` из enum схемы (отдельный cleanup PR после прогона миграции в проде).
- Шифрование локальной БД (Sqlcipher) — отдельная задача (Phase out-of-scope).
- Экран «Журнал» из ВКР (упоминается как необязательный MVP).
- Backend-side изменения (API считаем стабильным по `docs/specs/toir-backend-*.yaml`).
- Полнотекстовая поддержка push-уведомления о завершении синхры (FCM/APNS).

---

## 7. Порядок исполнения и ветки

1. Phase 1 — ветка `feature/sync-foundation`, PR в `main`.
2. Phase 2 — ветка `feature/sync-auto-after-inspection`, PR.
3. Phase 3 — ветка `feature/sync-ui`, PR.
4. Phase 4 — ветка `feature/sync-network-monitor`, PR.
5. Phase 5 — ветка `feature/sync-conflict`, PR.
6. Phase 6 — ветка `feature/sync-ios-bg`, PR.
7. Phase 7 — ветка `feature/sync-tests`, PR.

После Phase 3 продукт уже соответствует основной массе требований ВКР; Phase 4–7 поднимают качество и iOS-паритет.
