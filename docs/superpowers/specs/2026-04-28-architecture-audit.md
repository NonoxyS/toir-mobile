# Архитектурный аудит: toir-mobile × toir-backend

**Дата:** 2026-04-28  
**Ветка:** feature/sync-manager  
**Scope:** доменная логика, модели данных, контракт API, cross-layer mapping

---

## 1. Архитектура системы

```
┌─────────────────────────────────────────────────────────────────┐
│                         toir-backend                            │
│                                                                 │
│  Routes ──► DTOs ──► OpenAPI spec (documentation.yaml)         │
│   ↓                                                             │
│  Domain Entities ◄── Repository ◄── Exposed Tables             │
└─────────────────────────┬───────────────────────────────────────┘
                          │ HTTP/JSON (JWT Bearer)
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                        toir-mobile (KMM)                        │
│                                                                 │
│  feature-bootstrap ──► Remote Models ──► BootstrapRepositoryImpl│
│                              │                                  │
│                              ▼                                  │
│                      SQLDelight (Local DB)                      │
│                              │                                  │
│                              ▼                                  │
│  feature-* Repository ──► Local Models ──► Domain Models        │
│                                               │                 │
│                                               ▼                 │
│  MVI Store (MVIKotlin) ──► ViewModel ──► Compose UI             │
│                                                                 │
│  sync-manager ──► RemoteSyncPushRequest ──► /sync/push          │
└─────────────────────────────────────────────────────────────────┘
```

### Слои мобильного приложения

| Слой | Тип | Описание |
|------|-----|----------|
| Remote | `Remote*` data class | DTO для JSON deserialization (только в `impl`) |
| Local | `Local*` data class | Kotlin-представление SQLDelight строк |
| Domain | `Domain*` / `*Status` enum | Публичный API фичи (`api` модуль) |
| UI State | `Store.State` | Что видит Composable |

### Поток данных (Bootstrap)

```
Backend JSON
    → RemoteBootstrap* (десериализация)
    → Storage.upsert*() (сохранение в SQLite)
    → Local* (чтение из БД)
    → Domain* (маппинг в репозитории)
    → Store.State (MVI)
    → Composable
```

---

## 2. Инвентарь доменных моделей

### 2.1 feature-auth
- `DomainAuthUser`: id, displayName, role (хранится только в памяти/TokenStorage, не в БД)
- `BearerTokens`, `AccessToken`, `RefreshToken` — value types

### 2.2 feature-bootstrap
- Нет публичных доменных моделей — только результат `Result<Unit>`

### 2.3 feature-routes-list
- `DomainRouteAssignment`: assignmentId, routeId, routeName, status, assignedAt, totalPoints, completedPoints, inspectionId, hasPendingSync
- `RouteAssignmentStatus`: ASSIGNED, IN_PROGRESS, COMPLETED

### 2.4 feature-route-points
- `DomainRoutePoint`: routePointId, equipmentId, equipmentCode, equipmentName, locationName, equipmentResultId, status, hasIssues
- `EquipmentResultStatus`: NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED

### 2.5 feature-equipment-card
- `DomainEquipmentCard`: equipmentId, code, name, type, locationName, equipmentResultId, inspectionStatus
- `EquipmentResultStatus`: NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED

### 2.6 feature-photo-capture
- `List<String>` (file URIs) — нет отдельной доменной модели

---

## 3. Найденные проблемы

Проблемы сгруппированы по критичности. Каждая снабжена точным указанием расхождения.

---

### 🔴 КРИТИЧЕСКИЕ — runtime баги, ломают функциональность прямо сейчас

---

#### P1: Enum "confirm" vs "confirmation" — checklist items молча теряют тип

**Файл:** `shared/feature-bootstrap/impl/.../enums/RemoteAnswerType.kt`

Мобильная сторона:
```kotlin
RemoteAnswerType.CONFIRM to "confirm"   // ← ожидает строку "confirm"
```

Backend (`ChecklistResponseType`):
```
confirmation   // ← отправляет "confirmation"
```

**Последствие:** все элементы чеклиста с типом `confirmation` десериализуются в `UNKNOWN`, маппятся в строку `"unknown"` в БД, и функциональность подтверждения (confirm) полностью недоступна пользователю. Нет исключения — тихий баг.

**Исправление:** изменить `"confirm"` → `"confirmation"` в маппинге сериализатора.

---

#### P2: Sync push — `assignmentId` vs `routeAssignmentId`

**Файл:** `shared/sync-manager/.../RemoteSyncPushRequest.kt`, `RemoteSyncInspection`

Мобильная сторона:
```kotlin
@SerialName("assignmentId") val assignmentId: String
```

Backend `InspectionSyncDto`:
```
routeAssignmentId: String?   // ← другое имя поля + nullable
```

**Последствие:** backend не может прочитать `routeAssignmentId` из JSON — получит `null` вместо ID. Инспекции не связываются с назначениями на сервере.

**Исправление:** переименовать в `routeAssignmentId: String?`.

---

#### P3: Sync push — `valueSelect` vs `selectedOption`

**Файл:** `shared/sync-manager/.../RemoteSyncPushRequest.kt`, `RemoteSyncChecklistItemResult`

Мобильная сторона:
```kotlin
@SerialName("valueSelect") val valueSelect: String?
```

Backend `ChecklistItemResultSyncDto`:
```
selectedOption: String?
```

**Последствие:** ответы типа SELECT не доходят до сервера — поле приходит с неверным именем, сервер десериализует `selectedOption` как `null`.

**Исправление:** переименовать в `@SerialName("selectedOption")`.

---

#### P4: Sync push — `metadata` vs `payloadJson`, отсутствуют `entityType`/`entityId`/`actionTime`

**Файл:** `shared/sync-manager/.../RemoteSyncPushRequest.kt`, `RemoteSyncActionLog`

Мобильная сторона:
```kotlin
@SerialName("inspectionId") val inspectionId: String   // нет в spec
@SerialName("actionType") val actionType: String
@SerialName("metadata") val metadata: String?          // ← неверное имя
@SerialName("createdAt") val createdAt: String         // ← неверное имя
// отсутствуют: entityType, entityId
```

Backend `ActionLogSyncDto`:
```
actionType: String
entityType: String?      // ← отсутствует
entityId: String?        // ← отсутствует
payloadJson: String?     // ← отправляется как "metadata" с мобилы
actionTime: String       // ← отправляется как "createdAt" с мобилы
```

**Последствие:** action logs синхронизируются с неверной структурой. `entityType`/`entityId` всегда null на сервере, поле payload теряется из-за имени.

**Исправление:** переписать `RemoteSyncActionLog` под spec.

---

#### P5: Location name не резолвится — показывается locationId вместо имени

**Файл:** `shared/feature-equipment-card/impl/.../EquipmentCardRepositoryImpl.kt:54`

```kotlin
locationName = equipment.locationId.orEmpty(),   // ← locationId, не название!
```

Аналогично в `feature-route-points`. Таблица `locations` существует в БД, bootstrap её заполняет, но репозитории никогда не делают JOIN.

**Последствие:** пользователь видит UUID вместо названия локации в карточке оборудования и списке точек маршрута.

**Исправление:** добавить `LocationStorage`, сделать JOIN по `locationId` в репозиториях.

---

### 🟠 ВЫСОКИЕ — данные теряются или искажаются, но приложение не падает

---

#### P6: `partially_completed` и `cancelled` статусы назначений не поддерживаются

Backend `RouteAssignmentStatus`:
```
assigned | in_progress | completed | partially_completed | cancelled
```

Мобильный `RemoteAssignmentStatus`:
```kotlin
ASSIGNED, IN_PROGRESS, COMPLETED, UNKNOWN
```

**Последствие:** назначения со статусом `partially_completed` или `cancelled` десериализуются как `UNKNOWN`, что маппится в `LocalRouteStatus.ASSIGNED` (fallback в BootstrapRepositoryImpl). Задание, которое уже отменено, отображается как «Назначено».

**Исправление:** добавить `PARTIALLY_COMPLETED`, `CANCELLED` в enum (и в `LocalRouteStatus`).

---

#### P7: `numericMin` / `numericMax` из чеклиста не сохраняются

Backend `ChecklistItemDto` отправляет `numericMin: Double?` и `numericMax: Double?`. Мобильная схема таблицы `checklist_items` не имеет этих столбцов. Ограничения валидации числовых ответов теряются при bootstrap.

**Последствие:** пользователь может ввести любое число — граничная валидация недоступна.

**Исправление:** добавить столбцы `numeric_min REAL` / `numeric_max REAL` в `ChecklistItem.sq`.

---

#### P8: `qrCode` оборудования не сохраняется — эндпоинт by-qr нельзя использовать

Backend отправляет `qrCode: String?` в `EquipmentDto`. Локальная таблица `equipment` не имеет колонки `qr_code`. Эндпоинт `/api/v1/mobile/equipment/by-qr/{qrCode}` существует и предназначен для быстрого открытия карточки сканированием QR.

**Последствие:** feature QR-сканирования невозможно реализовать без сохранения QR-кода.

**Исправление:** добавить столбец `qr_code TEXT` в `Equipment.sq`.

---

#### P9: `code` маршрута не сохраняется

Backend отправляет `RouteDto.code: String`. Локальная таблица `routes` и `LocalRoute` не имеют поля `code`.

**Последствие:** внешняя идентификация маршрутов по коду невозможна; административный функционал типа «маршрут №Р-001» не может отображаться.

**Исправление:** добавить `code TEXT NOT NULL` в `Route.sq`.

---

#### P10: `RemoteSyncChecklistItemResult` содержит поля, которых нет в spec

Мобильная модель:
```kotlin
@SerialName("isConfirmed") val isConfirmed: Boolean    // нет в backend DTO
@SerialName("answeredAt") val answeredAt: String?      // нет в backend DTO
```

Backend `ChecklistItemResultSyncDto` не объявляет эти поля. Также отсутствует поле `comment: String?`, которое есть в backend.

**Последствие:** `isConfirmed`/`answeredAt` отправляются, но игнорируются сервером; `comment` никогда не синхронизируется.

---

### 🟡 СРЕДНИЕ — архитектурные несоответствия, технический долг

---

#### P11: Enum-to-String вместо Enum в некоторых слоях

`LocalRouteStatus` — kotlin enum (правильно).  
`answerType` в `LocalChecklistItem` — `String` (хранится как `"boolean"`, `"number"` и т.д.).  
`role` в `LocalUser` — `String` (`"executor"`, `"admin"`).

Непоследовательно. Строки не дают compile-time безопасности при использовании значений в репозиториях.

---

#### P12: `Location` сохраняется неполностью

Backend `LocationDto` содержит `code: String` и `parentLocationId: String?` (для иерархических локаций). Локальная таблица `locations` хранит только `id, name, description`. Иерархия и код теряются.

---

#### P13: Auth user и Bootstrap user живут в разных хранилищах, не синхронизированы

- `AuthRepositoryImpl` сохраняет пользователя как `DomainAuthUser` (в `TokenStorage` / памяти).
- `BootstrapRepositoryImpl` сохраняет пользователя в `LocalUser` (SQLite).

После смены роли пользователя на сервере: bootstrap обновит `LocalUser`, но `DomainAuthUser` в памяти останется со старой ролью до перелогина.

---

#### P14: `RemoteSyncInspection.startedAt` — non-nullable, хотя в spec nullable

```kotlin
@SerialName("startedAt") val startedAt: String   // должен быть String?
```

Backend `InspectionSyncDto.startedAt: String?`. Если инспекция создана, но не начата, мобила отправит пустую строку или упадёт с NPE.

---

#### P15: Sync push request — массивы nullable вместо defaultEmpty

```kotlin
val inspections: List<RemoteSyncInspection>?,        // должен быть emptyList()
val inspectionEquipmentResults: List<...>?,
val checklistItemResults: List<...>?,
val actionLogs: List<...>?,
```

Backend ожидает `default: []`. Nullable vs emptyList — различие в JSON: `null` vs `[]`. Некоторые backend-фреймворки могут отклонить `null` для array поля.

---

## 4. Сводная таблица расхождений Remote Models

### Bootstrap (чтение с сервера)

| Поле | Backend (spec) | Mobile (Remote) | Статус |
|------|---------------|-----------------|--------|
| `ChecklistResponseType.confirmation` | `"confirmation"` | `"confirm"` | 🔴 КРИТИЧНО |
| `RouteAssignmentStatus.partially_completed` | есть | нет | 🟠 |
| `RouteAssignmentStatus.cancelled` | есть | нет | 🟠 |
| `ChecklistItemDto.numericMin/Max` | `Double?` | не сохраняется | 🟠 |
| `EquipmentDto.qrCode` | `String?` | не сохраняется | 🟠 |
| `RouteDto.code` | `String` | не сохраняется | 🟠 |
| `LocationDto.code` | `String` | не сохраняется | 🟡 |
| `LocationDto.parentLocationId` | `String?` | не сохраняется | 🟡 |
| `LocationDto` → `locationName` resolve | JOIN нужен | ID используется как имя | 🔴 КРИТИЧНО |

### Sync Push (отправка на сервер)

| Поле | Backend (spec) | Mobile (Remote) | Статус |
|------|---------------|-----------------|--------|
| `InspectionSyncDto.routeAssignmentId` | `String?` | `assignmentId: String` | 🔴 КРИТИЧНО |
| `InspectionSyncDto.startedAt` | `String?` | `String` (non-null) | 🟠 |
| `InspectionSyncDto.createdAt` | есть | нет | 🟡 |
| `InspectionSyncDto.updatedAt` | есть | нет | 🟡 |
| `ChecklistItemResultSyncDto.selectedOption` | `String?` | `valueSelect: String?` | 🔴 КРИТИЧНО |
| `ChecklistItemResultSyncDto.comment` | `String?` | нет | 🟠 |
| `ChecklistItemResultSyncDto.isConfirmed` | нет | `Boolean` | 🟠 лишнее |
| `ChecklistItemResultSyncDto.answeredAt` | нет | `String?` | 🟠 лишнее |
| `ActionLogSyncDto.payloadJson` | `String?` | `metadata: String?` | 🔴 КРИТИЧНО |
| `ActionLogSyncDto.actionTime` | `String` | `createdAt: String` | 🔴 КРИТИЧНО |
| `ActionLogSyncDto.entityType` | `String?` | нет | 🟠 |
| `ActionLogSyncDto.entityId` | `String?` | нет | 🟠 |
| Все массивы | `default: []` | `nullable` | 🟡 |

---

## 5. Приоритизированный план исправлений

### Sprint 1 — Критические баги (до первого реального теста sync)

1. **P1** — Исправить `"confirm"` → `"confirmation"` в `RemoteAnswerType`
2. **P2** — Переименовать `assignmentId` → `routeAssignmentId: String?` в `RemoteSyncInspection`
3. **P3** — Переименовать `valueSelect` → `selectedOption` в `RemoteSyncChecklistItemResult`
4. **P4** — Переписать `RemoteSyncActionLog`: `metadata`→`payloadJson`, `createdAt`→`actionTime`, добавить `entityType`/`entityId`, убрать `inspectionId`
5. **P5** — Добавить `LocationStorage.selectById()`, зарезолвить `locationId → locationName` в `EquipmentCardRepositoryImpl` и `RoutePointsRepositoryImpl`

### Sprint 2 — Высокие (полнота данных)

6. **P6** — Добавить `PARTIALLY_COMPLETED`, `CANCELLED` в `RemoteAssignmentStatus` + `LocalRouteStatus`
7. **P7** — Добавить `numeric_min`, `numeric_max` в `ChecklistItem.sq` + `LocalChecklistItem` + storage/mapper
8. **P8** — Добавить `qr_code` в `Equipment.sq` + `LocalEquipment` + storage/mapper
9. **P9** — Добавить `code` в `Route.sq` + `LocalRoute` + storage/mapper
10. **P10** — Добавить `comment: String?` в `RemoteSyncChecklistItemResult`, убрать `isConfirmed`/`answeredAt`
11. **P14** — Исправить `startedAt: String` → `String?` в `RemoteSyncInspection`
12. **P15** — Заменить nullable списки на `emptyList()` в `RemoteSyncPushRequest`

### Sprint 3 — Средние (техдолг)

13. **P11** — Перевести `answerType` и `role` с String на sealed/enum в Local слое
14. **P12** — Добавить `code` и `parent_location_id` в таблицу `locations`
15. **P13** — Решить стратегию синхронизации Auth user ↔ Bootstrap user

---

## 6. Что работает корректно

- Базовый bootstrap flow: User, Equipment, Routes, Assignments, Checklists, ChecklistItems
- Маппинг `RemoteAssignmentStatus` → `LocalRouteStatus` (для ASSIGNED/IN_PROGRESS/COMPLETED)
- Деривация `checklistId` через `equipment.type → checklist.equipmentType`
- Сохранение инспекций и результатов оборудования локально
- Загрузка и отображение фотографий
- Трекинг `sync_status` (PENDING/SYNCED) на всех entity инспекций
- Структура `RemoteSyncPushResponse` / `RemoteSyncAccepted` / `RemoteSyncRejected` соответствует spec
- Fallback-десериализатор enum (`enumSerializerWithFallback`) — правильный паттерн

---

## 7. Общий вывод

Архитектура выстроена правильно: слои разделены, MVI стор изолирован от данных, offline-first через SQLDelight реализован корректно. Проблемы — в деталях контракта, накопившихся из-за отсутствия code-gen с OpenAPI spec.

**Самые срочные исправления** — P1–P4 (sync push) и P5 (location), так как они ломают работающую или планируемую функциональность прямо сейчас. Остальные — расширение возможностей и чистота данных.
