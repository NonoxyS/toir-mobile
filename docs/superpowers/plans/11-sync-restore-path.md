# Waypoint 11 — Обратный путь синхронизации (восстановление данных)

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`. Шаги фиксируются чекбоксами (`- [ ]`).
> **Full-stack waypoint:** затрагивает два репозитория — `toir-mobile` и `~/IdeaProjects/toir-backend`.

**Цель:** Замкнуть offline-first в обе стороны. Сейчас незавершённый обход доезжает на сервер (push работает), но при потере локальной БД (переустановка, очистка данных, новое устройство) в приложение **не возвращается** — `bootstrap` и `delta` несут только конфигурацию. Waypoint достраивает обратный путь: сервер отдаёт незавершённые обходы пользователя с результатами и фото, клиент применяет их локально с корректным мёржем поверх возможных локальных правок.

**Связь с ВКР:** закрывает требование **п.1 «Сохранность пользовательских данных»** в сценарии потери локальной БД и **п.6 «Разрешение конфликтов»** для пользовательских сущностей — Waypoint 10 это требование явно отложил («backend не возвращает inspections → конфликта нет», `10-sync-completion.md` §1.5). Создавая обратный путь, мы обязаны реализовать мёрж — он и есть реализация п.6.

**Источник истины по требованиям:** функциональные требования ВКР (раздел «Функциональные требования к синхронизации данных», пп. 1, 6, 7) + хэндофф `docs/superpowers/notes/2026-05-15-sync-restore-path-handoff.md`.

**Платформы:** Android + iOS.

---

## 0. Контекст: текущее состояние

### Что работает

- **Push (запись на сервер):** `SyncRepositoryImpl.pushPendingData` выгребает все записи со `sync_status = 'pending'` (включая незавершённые обходы) и отправляет на `POST /api/v1/mobile/sync/push`. Сервер сохраняет их в `inspections` / `inspection_equipment_results` / `checklist_item_results` как есть.
- **Серверная БД хранит промежуточные результаты.** Схема `toir-backend/.../V1__schema.sql`: `inspections.status` без CHECK-констрейнта, `completed_at` nullable, `checklist_item_results` — отдельная строка на каждое значение. Данные лежат в полной грануляции.
- **Фото:** `POST /photos/upload` (multipart) загружает файл, `StorageService.store` кладёт в локальный сторадж сервера, `photos` хранит метаданные. Метода скачивания **нет**.

### Где дыра

- `BootstrapResponse` (backend) и `RemoteBootstrapResponse` (client) содержат только конфиг — inspections/results/photos там НЕТ.
- `ConfigChangesResponse` / `RemoteConfigChangesResponse` — то же.
- Следствие: `SYNC_REQUIRED` + кнопка «Обновить» (Waypoint 10) не могут восстановить данные — delta их не несёт.
- При потере локальной БД незавершённая часть обхода физически на сервере, но мобильному приложению недоступна.

### Ключевой факт для дизайна мёржа

`startDestination = BootstrapRoute` (`shared/main/.../ui/App.kt:31`) → `BootstrapRepositoryImpl.loadAndSaveBootstrap` выполняется **при каждом холодном старте**, не только при первом логине. Значит мёрж серверной копии поверх непустой локальной БД — основной путь, а не пограничный случай.

### Что уже есть и переиспользуется

**Backend:**
- DTO `InspectionSyncDto`, `InspectionEquipmentResultSyncDto`, `ChecklistItemResultSyncDto` (`api/dto/mobile/SyncPushRequest.kt`) — несут все нужные поля, переиспользуем как форму ответа.
- `InspectionEquipmentResultRepository.findByInspectionId()`, `ChecklistItemResultRepository.findByInspectionEquipmentResultId()`, `PhotoRepository.findByChecklistItemResultId()` — выборки вглубь дерева уже есть.
- `StorageService.load(key)` — чтение файла из стораджа уже есть.
- IDOR-safe паттерн в `MobileInspectionRoutes.kt`: `inspection.userId != userId → 404`.

**Client:**
- `InspectionStorage` умеет `insertInspection` / `insertEquipmentResult` / `insertOrReplaceChecklistItemResult` + `markXSynced` + `selectXById`.
- `PhotoStorage` / `Photo.sq`: есть `storage_key`, `sync_status`, `markSynced`.
- `SyncManager.executeSyncCycle` — точка, куда встраивается шаг докачки фото (рядом с `uploadPendingPhotos`).
- `SyncTrigger.Bootstrap` — enum-значение уже есть, не используется.

---

## 1. Архитектурные решения

### 1.1 Что сервер добавляет в ответы

В `BootstrapResponse` (и `ConfigChangesResponse`) добавляются поля:

```
inspections: List<InspectionSyncDto>                       // переиспользуем push-DTO
inspectionEquipmentResults: List<InspectionEquipmentResultSyncDto>
checklistItemResults: List<ChecklistItemResultSyncDto>
photos: List<PhotoSyncDto>                                 // новый DTO — метаданные фото
```

`PhotoSyncDto`: `id`, `checklistItemResultId`, `fileName`, `mimeType`, `sizeBytes`, `checksum`, `createdAt`, `uploadedAt`. **Без байтов файла** — бинарь скачивается отдельным запросом.

Отбор на сервере: только незавершённые обходы пользователя (`status NOT IN ('completed','cancelled')`), их IER, CIR и фото. Чужие обходы не отдаются (фильтр по `userId`). Для delta — дополнительно `updated_at >= since`.

### 1.2 Скачивание фото

Метаданные фото восстанавливаются в bootstrap-транзакции синхронно (они маленькие). Сам файл — отдельным фоновым проходом:

- Новый backend-эндпоинт `GET /api/v1/mobile/photos/{photoId}` — отдаёт байты файла. IDOR-защита: фото → CIR → IER → inspection → проверка `inspection.userId == userId`, иначе 404.
- Колонка `photos.file_uri` становится **nullable**: `NULL` = «метаданные есть, файл ещё не скачан».
- Новый шаг в `SyncManager.executeSyncCycle` — `downloadMissingPhotos()`: выбирает `photos WHERE file_uri IS NULL AND storage_key IS NOT NULL`, скачивает, пишет байты в локальный сторадж устройства, проставляет `file_uri`. Зеркало существующего `uploadPendingPhotos()`.
- После успешного bootstrap клиент делает `syncManager.syncNow(SyncTrigger.Bootstrap)` → цикл подхватывает недостающие фото.
- Потребители `file_uri` (галерея/просмотрщик в `feature-photo-capture`) при `file_uri == null` показывают плейсхолдер «загружается».

### 1.3 Правило мёржа (центральное решение, реализует ВКР п.6)

Применяется **поштучно, по `id`, независимо к каждой таблице** (`inspections`, `inspection_equipment_results`, `checklist_item_results`, `photos`). Арбитр конфликта — локальный `sync_status`:

| Локальная запись | Действие | Обоснование |
|---|---|---|
| Отсутствует | INSERT серверной копии, `sync_status = 'synced'` | Чистое восстановление после потери БД |
| Есть, `sync_status = 'synced'` | UPDATE серверной копией, остаётся `synced` | Они идентичны (последний push подтверждён) → фактически no-op; бонусом доезжают серверные изменения статуса (напр. обход отклонён) |
| Есть, `sync_status IN ('pending','retry_scheduled','rejected')` | **SKIP, оставить локальную** | В локальной записи есть правки, которых сервер ещё не видел; затереть = потерять данные пользователя |

Принципы:
- **Серверная копия никогда не выигрывает у локальной несинхронизированной записи.** Направление «сервер всегда прав» сознательно не реализуем.
- **Не удаляем локальные записи, которых нет в ответе сервера.** Мёрж итерируется только по присланному. Локально созданный, ещё не отправленный обход в ответе отсутствует → остаётся нетронутым.
- **`sync_status` — арбитр, а не `updated_at`.** `pending` буквально означает «нет на сервере» — это надёжнее сравнения часов устройства.
- **Уровень — целая строка**, не отдельные поля. `checklist_item_result` — одна логическая единица.
- **Идемпотентность:** сервер возвращает реальные UUID; повторный bootstrap видит `synced` → no-op.

### 1.4 Транзакционность и гонка

Весь мёрж bootstrap-ответа (конфиг + восстановление) оборачивается в **одну транзакцию SQLDelight**. Причина: параллельно может крутиться push из `SyncManager` (триггеры Connectivity/Periodic), переключающий `pending → synced`. Транзакция гарантирует, что «прочитал статус → записал» не разъедется. Логика мёржа остаётся в Kotlin (читаемо, тестируемо), консистентность даёт транзакция.

`BootstrapRepositoryImpl` сейчас НЕ оборачивает запись в транзакцию — нужно инжектировать `ToirDatabase` (или прокси `TransactionRunner`, как обсуждалось в Waypoint 10 Task 1.4) и обернуть весь блок `loadAndSaveBootstrap`.

Порядок записи внутри транзакции — сверху вниз по FK: конфиг → inspections → IER → CIR → photos. Гарантирует, что родитель существует до вставки потомка независимо от того, взят он с сервера или оставлен локальный.

### 1.5 Конфликт «обе стороны изменены» — out of scope

В модели ВКР «один пользователь — одно устройство» строка `pending` локально и одновременно новее на сервере существовать не может (нужно второе устройство). Это **явно фиксируется в тексте ВКР как осознанное ограничение**, разрешение конфликтов уровня vector-clock/CRDT не реализуется. Правило 1.3 для одного устройства всегда корректно: локальная незапушенная правка выигрывает у сервера.

### 1.6 Delta-путь (кнопка «Обновить»)

Кнопка «Обновить» при `SYNC_REQUIRED` (Waypoint 10) станет реально восстанавливать данные: `buildConfigChanges` начнёт отдавать незавершённые обходы с `updated_at >= since`, `ConfigChangesApplier` применит их тем же правилом мёржа 1.3. Без этой фазы восстановление работает только через перезапуск приложения (bootstrap).

---

## 2. Файловая структура (новое и изменённое)

### Backend (`~/IdeaProjects/toir-backend`)

```
src/main/kotlin/ru/mirea/toir/
├── api/dto/mobile/
│   ├── BootstrapResponse.kt              [edit: +inspections,+ier,+cir,+photos]
│   ├── ConfigChangesResponse.kt          [edit: те же 4 поля]
│   └── PhotoSyncDto.kt                   [new]
├── api/routes/
│   └── MobilePhotoRoutes.kt              [edit: +GET /photos/{photoId}]
├── application/services/
│   ├── ReferenceDataService.kt           [edit: buildBootstrap + buildConfigChanges собирают дерево обходов]
│   └── PhotoService.kt                   [edit: +download(photoId, userId): bytes — с IDOR-проверкой]
├── domain/repositories/
│   └── InspectionRepository.kt           [edit: +findIncompleteByUserId, +findIncompleteByUserIdUpdatedSince]
└── infrastructure/persistence/repositories/
    └── InspectionRepositoryImpl.kt       [edit: реализация новых выборок]
src/test/kotlin/ru/mirea/toir/
├── sync/  или  bootstrap/                [new: тесты на restore в bootstrap/delta]
└── photo/PhotoServiceTest.kt             [edit: +download + IDOR]
```

### Client (`toir-mobile`)

```
shared/core-database/src/commonMain/
├── sqldelight/ru/mirea/toir/core/database/
│   ├── Photo.sq                          [edit: file_uri → nullable; +selectMissingFiles; +insertRestoredPhoto]
│   ├── Inspection.sq                     [edit: +upsertFromServer (INSERT ... ON CONFLICT с учётом sync_status)]
│   ├── InspectionEquipmentResult.sq      [edit: +upsertFromServer]
│   └── ChecklistItemResult.sq            [edit: +upsertFromServer]
└── kotlin/.../storage/
    ├── inspection/InspectionStorage(+Impl).kt   [edit: +applyServerInspection/Ier/Cir с правилом мёржа]
    └── photo/PhotoStorage(+Impl).kt             [edit: file_uri nullable; +selectMissingFiles; +insertRestoredPhoto; +setFileUri]

shared/feature-bootstrap/impl/src/commonMain/
├── data/network/models/RemoteBootstrapResponse.kt  [edit: +inspections,+ier,+cir,+photos + DTO-классы]
└── data/repository/BootstrapRepositoryImpl.kt      [edit: +inspectionStorage,+photoStorage,+ToirDatabase; транзакция; мёрж; syncNow(Bootstrap)]

shared/sync-manager/src/
├── commonMain/kotlin/ru/mirea/toir/sync/
│   ├── domain/repository/SyncRepository.kt          [edit: +downloadMissingPhotos(): Result<Long>]
│   ├── domain/SyncManager.kt                        [edit: шаг downloadMissingPhotos в executeSyncCycle]
│   ├── data/repository/SyncRepositoryImpl.kt        [edit: реализация downloadMissingPhotos]
│   ├── data/network/SyncApiClient(+Impl).kt         [edit: +downloadPhoto(photoId): ByteArray]
│   ├── data/network/models/RemoteConfigChangesResponse.kt  [edit: +inspections,+ier,+cir,+photos]
│   └── data/applier/ConfigChangesApplier.kt         [edit: применяет восстановление с правилом мёржа]
└── commonTest/kotlin/ru/mirea/toir/sync/            [new: тесты мёржа — 5 сценариев + докачка фото]

shared/feature-bootstrap/impl/src/commonTest/        [new/edit: тесты BootstrapRepositoryImpl мёржа]
```

---

## 3. Фазы реализации

Каждая фаза = отдельный PR. Порядок строгий. Backend-фаза (1) первой — она задаёт контракт API, на который опираются клиентские фазы.

### Phase 1 — Backend: восстановление в bootstrap

**Цель:** `/bootstrap` отдаёт незавершённые обходы пользователя с результатами и метаданными фото.

- [ ] **Task 1.1** `PhotoSyncDto` в `api/dto/mobile/` (id, checklistItemResultId, fileName, mimeType, sizeBytes, checksum, createdAt, uploadedAt).
- [ ] **Task 1.2** `BootstrapResponse` — добавить 4 поля (`inspections`, `inspectionEquipmentResults`, `checklistItemResults`, `photos`), переиспользовать существующие `*SyncDto` из `SyncPushRequest.kt`.
- [ ] **Task 1.3** `InspectionRepository.findIncompleteByUserId(userId): List<Inspection>` + реализация в `InspectionRepositoryImpl` (SQL: `WHERE user_id = ? AND status NOT IN ('completed','cancelled')`).
- [ ] **Task 1.4** `ReferenceDataService.buildBootstrap` — после конфига собрать дерево: для каждого незавершённого обхода → `ierRepo.findByInspectionId` → `cirRepo.findByInspectionEquipmentResultId` → `photoRepo.findByChecklistItemResultId`. Замапить в DTO. Инжектировать недостающие репозитории в `ReferenceDataService` (правка `Application.kt:42-45`).
- [ ] **Task 1.5** Тест в `src/test`: пользователь с незавершённым обходом → `/bootstrap` содержит его inspection + IER + CIR + photo-метаданные; чужой обход в ответ не попадает; пользователь без обходов → пустые списки.
- [ ] **Verification:** `./gradlew test` (backend) зелёный; ручной `curl /bootstrap` с токеном показывает незавершённый обход.
- [ ] Коммит: `feat(sync): bootstrap returns user's incomplete inspections (Phase 1)`.

### Phase 2 — Backend: скачивание фото + delta

**Цель:** эндпоинт скачивания файла фото; delta тоже отдаёт восстановление.

- [ ] **Task 2.1** `PhotoService.download(photoId, userId): ByteArray?` — найти photo → CIR → IER → inspection, проверить `inspection.userId == userId`, вернуть `storageService.load(photo.storageKey)`. Для цепочки нужны `ierRepo` + `inspectionRepo` в `PhotoService` (правка конструктора + `Application.kt:56`).
- [ ] **Task 2.2** `MobilePhotoRoutes` — `GET /photos/{photoId}`: 400 при кривом UUID, 404 если фото нет/чужое, иначе `call.respondBytes(bytes, ContentType)`.
- [ ] **Task 2.3** `ConfigChangesResponse` — те же 4 поля; `ReferenceDataService.buildConfigChanges` собирает незавершённые обходы с `updated_at >= since` (новый `InspectionRepository.findIncompleteByUserIdUpdatedSince`).
- [ ] **Task 2.4** Тесты: download отдаёт байты владельцу, 404 чужому/несуществующему; delta содержит обновлённый незавершённый обход.
- [ ] **Verification:** `./gradlew test` зелёный.
- [ ] Коммит: `feat(sync): photo download endpoint + delta restore (Phase 2)`.

### Phase 3 — Client: схема БД и слой хранилища

**Цель:** подготовить локальную БД и storage-слой к восстановлению. Без изменения поведения.

> **Без миграции** — правим CREATE TABLE напрямую (продакшен-БД ещё нет; так же делалось в Waypoint 10 Task 1.1).

- [ ] **Task 3.1** `Photo.sq` — `file_uri` → nullable. Добавить `selectMissingFiles` (`WHERE file_uri IS NULL AND storage_key IS NOT NULL`), `insertRestoredPhoto` (file_uri = NULL, sync_status = 'synced'), `setFileUri(id, uri)`.
- [ ] **Task 3.2** `Inspection.sq` / `InspectionEquipmentResult.sq` / `ChecklistItemResult.sq` — добавить `upsertFromServer`-запросы, реализующие правило 1.3 средствами SQL: `INSERT ... ON CONFLICT(id) DO UPDATE SET ... WHERE sync_status = 'synced'` (при конфликте обновляем только если локальная `synced`; `pending`/`retry`/`rejected` — UPDATE не срабатывает, строка остаётся). Вставка новой — со `sync_status = 'synced'`.
- [ ] **Task 3.3** `InspectionStorage` (+Impl) — методы `applyServerInspection(...)`, `applyServerEquipmentResult(...)`, `applyServerChecklistItemResult(...)` поверх новых запросов.
- [ ] **Task 3.4** `PhotoStorage` (+Impl) — `file_uri` nullable во всех сигнатурах; `selectMissingFiles()`, `insertRestoredPhoto(...)`, `setFileUri(id, uri)`. Найти всех потребителей `file_uri` (`feature-photo-capture`, `IntentStarter`) — обработать `null`.
- [ ] **Verification:** `./gradlew :shared:core-database:assemble` зелёный; существующие тесты `core-database` зелёные.
- [ ] Коммит: `feat(sync): db schema + storage for inspection restore (Phase 3)`.

### Phase 4 — Client: восстановление в bootstrap с мёржем

**Цель:** `BootstrapRepositoryImpl` восстанавливает незавершённые обходы с правилом мёржа 1.3 в транзакции.

- [ ] **Task 4.1** `RemoteBootstrapResponse` — добавить `inspections`, `inspectionEquipmentResults`, `checklistItemResults`, `photos` + `@Serializable` DTO-классы (зеркало backend, поля по умолчанию `emptyList()`).
- [ ] **Task 4.2** `BootstrapRepositoryImpl` — инжектировать `inspectionStorage`, `photoStorage`, `ToirDatabase` (или `TransactionRunner`). Обернуть весь `loadAndSaveBootstrap` в `db.transaction { }`. Добавить блоки записи inspections → IER → CIR → photo-метаданные через `applyServerX` / `insertRestoredPhoto`.
- [ ] **Task 4.3** После `BootstrapResult.Success` в `BootstrapExecutor` (или в репозитории) — `syncManager.syncNow(SyncTrigger.Bootstrap)` для запуска докачки фото. Инжектировать `SyncManager` в feature-bootstrap (модуль-зависимость + DI).
- [ ] **Task 4.4** Тесты `BootstrapRepositoryImpl` — 5 сценариев мёржа: (1) пустая БД → вставка; (2) всё `synced` → no-op; (3) локальный `pending` → пропуск, локальное сохранено; (4) частично `pending` дерево → точечный пропуск; (5) повторный bootstrap идемпотентен.
- [ ] **Verification:** `./gradlew :shared:feature-bootstrap:impl:assemble :android:app:assembleDebug detekt` зелёный; тесты Task 4.4 проходят.
- [ ] Коммит: `feat(sync): restore incomplete inspections on bootstrap with merge (Phase 4)`.

### Phase 5 — Client: докачка фото

**Цель:** файлы фото восстановленных обходов скачиваются и доступны офлайн.

- [ ] **Task 5.1** `SyncApiClient` (+Impl) — `downloadPhoto(photoId): ByteArray` (`GET /mobile/photos/{photoId}`).
- [ ] **Task 5.2** `SyncRepository.downloadMissingPhotos(): Result<Long>` + реализация в `SyncRepositoryImpl`: `photoStorage.selectMissingFiles()` → для каждого `downloadPhoto` → запись байтов в локальный сторадж устройства (через `FileReader`/платформенный writer — проверить, есть ли writer; если нет — добавить `expect/actual`) → `photoStorage.setFileUri(id, uri)`. Ошибка одного фото не валит весь шаг (лог + продолжаем; retry на следующем цикле).
- [ ] **Task 5.3** `SyncManager.executeSyncCycle` — добавить шаг `downloadMissingPhotos()` (после `fetchAndApplyDeltaChanges` или рядом с `uploadPendingPhotos`; порядок: upload → push → delta → downloadMissing).
- [ ] **Task 5.4** Потребители `file_uri == null` в UI — состояние «фото загружается» в просмотрщике `feature-photo-capture`. **UI делается строго через дизайн-систему:** сверяемся с `docs/design-system/MASTER.md` + `docs/design-system/pages/photo-capture.md`; если состояния «файл ещё не скачан» в спеке нет — сначала дописываем раздел в `photo-capture.md`, потом код. При развилках (иконка, цвет, анимация плейсхолдера) — вызвать дизайн-скилл `ui-ux-pro-max:ui-ux-pro-max`. Не импровизировать.
- [ ] **Task 5.5** Тесты: `downloadMissingPhotos` скачивает и проставляет `file_uri`; ошибка скачивания одного фото не роняет остальные.
- [ ] **Verification:** `./gradlew :shared:sync-manager:assemble :shared:sync-manager:allTests :android:app:assembleDebug detekt` зелёный.
- [ ] Коммит: `feat(sync): background photo download for restored inspections (Phase 5)`.

### Phase 6 — Client: восстановление в delta + интеграционные тесты

**Цель:** кнопка «Обновить» восстанавливает данные; сквозное покрытие.

- [ ] **Task 6.1** `RemoteConfigChangesResponse` — 4 поля восстановления.
- [ ] **Task 6.2** `ConfigChangesApplier` — применить inspections/IER/CIR/photo-метаданные тем же правилом мёржа 1.3 (переиспользовать `applyServerX` из Phase 3), внутри существующей транзакции applier-а.
- [ ] **Task 6.3** Проверить сценарий `SYNC_REQUIRED`: после `delta` с восстановленным обходом локальный `Inspection` появляется → статус `SYNC_REQUIRED` исчезает (логика из `project_sync_required_status`).
- [ ] **Task 6.4** Интеграционный тест на готовой инфре (`project_test_infra`: TestDatabase, TestSyncApi): сквозной сценарий «БД с обходом → wipe → bootstrap → обход восстановлен, фото докачаны, прогресс виден»; «локальный pending + delta → локальное не затёрто».
- [ ] **Verification:** `./gradlew :shared:sync-manager:allTests :android:app:assembleDebug detekt` зелёный; backend `./gradlew test` зелёный.
- [ ] Коммит: `feat(sync): delta restore + integration tests (Phase 6)`.

---

## 4. Приёмочные критерии (mapping к ВКР)

| Требование ВКР | Критерий приёмки | Проверка |
|---|---|---|
| 1. Сохранность данных при потере локальной БД | После переустановки приложения логин → bootstrap восстанавливает незавершённый обход с результатами и фото; прогресс виден | integration-тест Phase 6 + smoke |
| 6. Разрешение конфликтов | Локальная `pending`-запись не затирается серверной копией при bootstrap/delta; `synced` обновляется | unit-тесты мёржа Phase 4, 5 сценариев |
| 7. Невозможность частичной потери | Мёрж bootstrap-ответа в одной транзакции; kill посреди мёржа не оставляет полудерево | unit + ручной kill-test |
| 7. Связность данных | Порядок записи сверху вниз по FK; FK SQLDelight | существующие FK + тест |
| (хэндофф) Сценарий «потеря БД посреди обхода» | Строка таблицы надёжности из хэндофф-ноты переходит из ❌ в OK | integration-тест Phase 6 |
| (Waypoint 10) кнопка «Обновить» при `SYNC_REQUIRED` | Реально восстанавливает обход, а не только перезапуск приложения | smoke Phase 6 |

---

## 5. Риски и открытые вопросы

| Риск | Митигация |
|---|---|
| `file_uri` nullable — потребители ждут non-null | Task 3.4 + 5.4: явный аудит потребителей, плейсхолдер в UI |
| Рост payload bootstrap (выполняется на каждом старте) | Незавершённых обходов у пользователя единицы; фото — только метаданные, бинари отдельным каналом. Приемлемо, конфиг и так шлётся целиком |
| Гонка bootstrap-мёрж vs параллельный push | Весь мёрж в одной транзакции SQLDelight (§1.4) |
| IDOR на `GET /photos/{photoId}` | Проверка цепочки photo→CIR→IER→inspection→userId (Task 2.1), паттерн уже есть в `MobileInspectionRoutes` |
| Конфликт «обе стороны изменены» (два устройства) | Out of scope, фиксируется в тексте ВКР как ограничение модели «один пользователь — одно устройство» (§1.5) |
| Запись байтов фото в локальный сторадж устройства — нет writer-а | Task 5.2: проверить наличие, при отсутствии — `expect/actual` writer рядом с `FileReader` |
| Делать платформенный writer фото на iOS и Android | Зеркало существующего `FileReader.android.kt` / `.ios.kt` |

---

## 6. Что НЕ в scope

- Разрешение истинных конфликтов (vector clock / CRDT) — модель «одно устройство».
- Шифрование локальной БД.
- Восстановление `action_logs` — это аудит-журнал, для продолжения обхода не нужен.
- Дозагрузка фото по требованию при открытии просмотрщика (lazy) — делаем eager-докачку фоном; lazy можно добавить позже как оптимизацию.

---

## 7. Порядок исполнения и ветки

1. Phase 1 — `feature/backend-bootstrap-restore` (репо toir-backend), PR.
2. Phase 2 — `feature/backend-photo-download` (репо toir-backend), PR.
3. Phase 3 — `feature/client-restore-schema`, PR.
4. Phase 4 — `feature/client-bootstrap-restore`, PR.
5. Phase 5 — `feature/client-photo-download`, PR.
6. Phase 6 — `feature/client-delta-restore`, PR.

После Phase 4 переустановка уже восстанавливает обход (без фото-файлов); Phase 5 добавляет фото; Phase 6 — кнопку «Обновить» и сквозные тесты.
