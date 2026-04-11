# Waypoint 01 — Core Database (SQLDelight)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Добавить SQLDelight в проект и создать `shared/core-database` модуль со всеми таблицами, DAOs и DI-регистрацией.

**Architecture:** Один database-модуль для всей shared-логики. SQLDelight генерирует типобезопасные Kotlin-запросы из `.sq` файлов. Платформозависимые драйверы (Android/iOS) регистрируются через `expect/actual`.

**Tech Stack:** SQLDelight 2.0.2, Koin 4.1.1, KMM (commonMain + androidMain + iosMain).

---

## Файловая структура

```
gradle/libs.versions.toml                                    # MODIFY — добавить sqldelight
build-logic/build.gradle.kts                                 # MODIFY — добавить плагин в classpath
settings.gradle.kts                                          # MODIFY — include core-database

shared/core-database/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/ru/mirea/toir/core/database/
    │   ├── di/CoreDatabaseModule.kt
    │   ├── driver/DatabaseDriverFactory.kt          # expect
    │   └── dao/
    │       ├── UserDao.kt
    │       ├── EquipmentDao.kt
    │       ├── RouteDao.kt
    │       ├── InspectionDao.kt
    │       ├── ChecklistDao.kt
    │       ├── PhotoDao.kt
    │       ├── ActionLogDao.kt
    │       └── SyncMetaDao.kt
    ├── commonMain/sqldelight/ru/mirea/toir/core/database/
    │   ├── User.sq
    │   ├── Device.sq
    │   ├── Location.sq
    │   ├── Equipment.sq
    │   ├── Checklist.sq
    │   ├── ChecklistItem.sq
    │   ├── Route.sq
    │   ├── RoutePoint.sq
    │   ├── RouteAssignment.sq
    │   ├── Inspection.sq
    │   ├── InspectionEquipmentResult.sq
    │   ├── ChecklistItemResult.sq
    │   ├── Photo.sq
    │   ├── ActionLog.sq
    │   ├── SyncBatch.sq
    │   └── SyncMeta.sq
    ├── androidMain/kotlin/ru/mirea/toir/core/database/
    │   └── driver/DatabaseDriverFactory.android.kt  # actual
    └── iosMain/kotlin/ru/mirea/toir/core/database/
        └── driver/DatabaseDriverFactory.ios.kt      # actual
```

---

### Task 1: Добавить SQLDelight в version catalog и build-logic

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `build-logic/build.gradle.kts`

- [ ] **Step 1: Добавить версию и зависимости в libs.versions.toml**

В секцию `[versions]` добавить:
```toml
sqldelight = "2.0.2"
```

В секцию `[libraries]` добавить:
```toml
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
sqldelight-android = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-native = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }
sqldelight-primitiveAdapters = { module = "app.cash.sqldelight:primitive-adapters", version.ref = "sqldelight" }
gradleplugin-sqldelight = { module = "app.cash.sqldelight:gradle-plugin", version.ref = "sqldelight" }
```

В секцию `[plugins]` добавить:
```toml
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
```

- [ ] **Step 2: Добавить плагин в classpath build-logic**

В `build-logic/build.gradle.kts` в блок `dependencies { }` добавить строку после остальных `compileOnly`:
```kotlin
compileOnly(libs.gradleplugin.sqldelight)
```

- [ ] **Step 3: Проверить, что build-logic компилируется**

```bash
./gradlew :build-logic:assemble
```
Ожидаемый результат: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**
```bash
git add gradle/libs.versions.toml build-logic/build.gradle.kts
git commit -m "build: add SQLDelight 2.0.2 to version catalog and build-logic classpath"
```

---

### Task 2: Создать модуль core-database

**Files:**
- Create: `shared/core-database/build.gradle.kts`
- Modify: `settings.gradle.kts`

- [ ] **Step 1: Создать директорию модуля**

```bash
mkdir -p shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/di
mkdir -p shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/driver
mkdir -p shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/dao
mkdir -p shared/core-database/src/commonMain/sqldelight/ru/mirea/toir/core/database
mkdir -p shared/core-database/src/androidMain/kotlin/ru/mirea/toir/core/database/driver
mkdir -p shared/core-database/src/iosMain/kotlin/ru/mirea/toir/core/database/driver
```

- [ ] **Step 2: Создать build.gradle.kts**

Создать `shared/core-database/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
import extensions.androidMainDependencies
import extensions.commonMainDependencies
import extensions.iosMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.sqldelight)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.core.database"
}

sqldelight {
    databases {
        create("ToirDatabase") {
            packageName.set("ru.mirea.toir.core.database")
            generateAsync = false
        }
    }
}

commonMainDependencies {
    implementations(
        libs.sqldelight.runtime,
        libs.sqldelight.coroutines,
        libs.sqldelight.primitiveAdapters,
    )
}

androidMainDependencies {
    implementations(libs.sqldelight.android)
}

iosMainDependencies {
    implementations(libs.sqldelight.native)
}
```

- [ ] **Step 3: Зарегистрировать модуль в settings.gradle.kts**

В `settings.gradle.kts` в блок `// Shared feature modules` добавить перед include демо-модулей:
```kotlin
// Core database
include(":shared:core-database")
```

- [ ] **Step 4: Проверить, что Gradle синхронизируется без ошибок**

```bash
./gradlew :shared:core-database:tasks --dry-run 2>&1 | head -20
```
Ожидаемый результат: список тасков без ошибок.

---

### Task 3: Написать SQL-схемы (.sq файлы)

**Files:**
- Create: все `.sq` файлы в `shared/core-database/src/commonMain/sqldelight/ru/mirea/toir/core/database/`

- [ ] **Step 1: Создать User.sq**

```sql
CREATE TABLE IF NOT EXISTS users (
    id TEXT NOT NULL PRIMARY KEY,
    login TEXT NOT NULL,
    display_name TEXT NOT NULL,
    role TEXT NOT NULL
);

upsertUser:
INSERT OR REPLACE INTO users (id, login, display_name, role)
VALUES (?, ?, ?, ?);

selectAll:
SELECT * FROM users;

selectById:
SELECT * FROM users WHERE id = ?;

deleteAll:
DELETE FROM users;
```

- [ ] **Step 2: Создать Device.sq**

```sql
CREATE TABLE IF NOT EXISTS devices (
    id TEXT NOT NULL PRIMARY KEY,
    device_code TEXT NOT NULL
);

upsertDevice:
INSERT OR REPLACE INTO devices (id, device_code)
VALUES (?, ?);

selectFirst:
SELECT * FROM devices LIMIT 1;

deleteAll:
DELETE FROM devices;
```

- [ ] **Step 3: Создать Location.sq**

```sql
CREATE TABLE IF NOT EXISTS locations (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT
);

upsertLocation:
INSERT OR REPLACE INTO locations (id, name, description)
VALUES (?, ?, ?);

selectAll:
SELECT * FROM locations;

selectById:
SELECT * FROM locations WHERE id = ?;

deleteAll:
DELETE FROM locations;
```

- [ ] **Step 4: Создать Equipment.sq**

```sql
CREATE TABLE IF NOT EXISTS equipment (
    id TEXT NOT NULL PRIMARY KEY,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    location_id TEXT NOT NULL
);

upsertEquipment:
INSERT OR REPLACE INTO equipment (id, code, name, type, location_id)
VALUES (?, ?, ?, ?, ?);

selectAll:
SELECT * FROM equipment;

selectById:
SELECT * FROM equipment WHERE id = ?;

deleteAll:
DELETE FROM equipment;
```

- [ ] **Step 5: Создать Checklist.sq**

```sql
CREATE TABLE IF NOT EXISTS checklists (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    equipment_id TEXT
);

upsertChecklist:
INSERT OR REPLACE INTO checklists (id, name, equipment_id)
VALUES (?, ?, ?);

selectAll:
SELECT * FROM checklists;

selectById:
SELECT * FROM checklists WHERE id = ?;

deleteAll:
DELETE FROM checklists;
```

- [ ] **Step 6: Создать ChecklistItem.sq**

```sql
CREATE TABLE IF NOT EXISTS checklist_items (
    id TEXT NOT NULL PRIMARY KEY,
    checklist_id TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    answer_type TEXT NOT NULL,
    is_required INTEGER NOT NULL DEFAULT 0,
    requires_photo INTEGER NOT NULL DEFAULT 0,
    select_options TEXT,
    order_index INTEGER NOT NULL DEFAULT 0
);

upsertChecklistItem:
INSERT OR REPLACE INTO checklist_items (
    id, checklist_id, title, description, answer_type,
    is_required, requires_photo, select_options, order_index
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);

selectByChecklistId:
SELECT * FROM checklist_items WHERE checklist_id = ? ORDER BY order_index ASC;

selectById:
SELECT * FROM checklist_items WHERE id = ?;

deleteAll:
DELETE FROM checklist_items;
```

- [ ] **Step 7: Создать Route.sq**

```sql
CREATE TABLE IF NOT EXISTS routes (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT
);

upsertRoute:
INSERT OR REPLACE INTO routes (id, name, description)
VALUES (?, ?, ?);

selectAll:
SELECT * FROM routes;

selectById:
SELECT * FROM routes WHERE id = ?;

deleteAll:
DELETE FROM routes;
```

- [ ] **Step 8: Создать RoutePoint.sq**

```sql
CREATE TABLE IF NOT EXISTS route_points (
    id TEXT NOT NULL PRIMARY KEY,
    route_id TEXT NOT NULL,
    equipment_id TEXT NOT NULL,
    checklist_id TEXT NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0
);

upsertRoutePoint:
INSERT OR REPLACE INTO route_points (id, route_id, equipment_id, checklist_id, order_index)
VALUES (?, ?, ?, ?, ?);

selectByRouteId:
SELECT * FROM route_points WHERE route_id = ? ORDER BY order_index ASC;

selectById:
SELECT * FROM route_points WHERE id = ?;

deleteAll:
DELETE FROM route_points;
```

- [ ] **Step 9: Создать RouteAssignment.sq**

```sql
CREATE TABLE IF NOT EXISTS route_assignments (
    id TEXT NOT NULL PRIMARY KEY,
    route_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    status TEXT NOT NULL,
    assigned_at TEXT NOT NULL,
    due_date TEXT
);

upsertRouteAssignment:
INSERT OR REPLACE INTO route_assignments (id, route_id, user_id, status, assigned_at, due_date)
VALUES (?, ?, ?, ?, ?, ?);

selectAll:
SELECT * FROM route_assignments;

selectById:
SELECT * FROM route_assignments WHERE id = ?;

updateStatus:
UPDATE route_assignments SET status = ? WHERE id = ?;

deleteAll:
DELETE FROM route_assignments;
```

- [ ] **Step 10: Создать Inspection.sq**

```sql
CREATE TABLE IF NOT EXISTS inspections (
    id TEXT NOT NULL PRIMARY KEY,
    assignment_id TEXT NOT NULL,
    route_id TEXT NOT NULL,
    status TEXT NOT NULL,
    started_at TEXT NOT NULL,
    completed_at TEXT,
    sync_status TEXT NOT NULL DEFAULT 'PENDING'
);

insertInspection:
INSERT INTO inspections (id, assignment_id, route_id, status, started_at, completed_at, sync_status)
VALUES (?, ?, ?, ?, ?, ?, ?);

selectByAssignmentId:
SELECT * FROM inspections WHERE assignment_id = ?;

selectById:
SELECT * FROM inspections WHERE id = ?;

updateStatus:
UPDATE inspections SET status = ?, completed_at = ? WHERE id = ?;

selectPending:
SELECT * FROM inspections WHERE sync_status = 'PENDING';

updateSyncStatus:
UPDATE inspections SET sync_status = ? WHERE id = ?;
```

- [ ] **Step 11: Создать InspectionEquipmentResult.sq**

```sql
CREATE TABLE IF NOT EXISTS inspection_equipment_results (
    id TEXT NOT NULL PRIMARY KEY,
    inspection_id TEXT NOT NULL,
    route_point_id TEXT NOT NULL,
    equipment_id TEXT NOT NULL,
    status TEXT NOT NULL,
    started_at TEXT,
    completed_at TEXT,
    sync_status TEXT NOT NULL DEFAULT 'PENDING'
);

insertResult:
INSERT INTO inspection_equipment_results (
    id, inspection_id, route_point_id, equipment_id, status, started_at, completed_at, sync_status
) VALUES (?, ?, ?, ?, ?, ?, ?, ?);

selectByInspectionId:
SELECT * FROM inspection_equipment_results WHERE inspection_id = ?;

selectById:
SELECT * FROM inspection_equipment_results WHERE id = ?;

selectByRoutePointAndInspection:
SELECT * FROM inspection_equipment_results
WHERE route_point_id = ? AND inspection_id = ?
LIMIT 1;

updateStatus:
UPDATE inspection_equipment_results SET status = ?, started_at = ?, completed_at = ? WHERE id = ?;

selectPending:
SELECT * FROM inspection_equipment_results WHERE sync_status = 'PENDING';

updateSyncStatus:
UPDATE inspection_equipment_results SET sync_status = ? WHERE id = ?;
```

- [ ] **Step 12: Создать ChecklistItemResult.sq**

```sql
CREATE TABLE IF NOT EXISTS checklist_item_results (
    id TEXT NOT NULL PRIMARY KEY,
    inspection_equipment_result_id TEXT NOT NULL,
    checklist_item_id TEXT NOT NULL,
    value_boolean INTEGER,
    value_number REAL,
    value_text TEXT,
    value_select TEXT,
    is_confirmed INTEGER NOT NULL DEFAULT 0,
    answered_at TEXT,
    sync_status TEXT NOT NULL DEFAULT 'PENDING'
);

insertOrReplaceResult:
INSERT OR REPLACE INTO checklist_item_results (
    id, inspection_equipment_result_id, checklist_item_id,
    value_boolean, value_number, value_text, value_select,
    is_confirmed, answered_at, sync_status
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

selectByEquipmentResultId:
SELECT * FROM checklist_item_results WHERE inspection_equipment_result_id = ?;

selectByChecklistItemAndEquipmentResult:
SELECT * FROM checklist_item_results
WHERE checklist_item_id = ? AND inspection_equipment_result_id = ?
LIMIT 1;

selectPending:
SELECT * FROM checklist_item_results WHERE sync_status = 'PENDING';

updateSyncStatus:
UPDATE checklist_item_results SET sync_status = ? WHERE id = ?;
```

- [ ] **Step 13: Создать Photo.sq**

```sql
CREATE TABLE IF NOT EXISTS photos (
    id TEXT NOT NULL PRIMARY KEY,
    checklist_item_result_id TEXT NOT NULL,
    file_uri TEXT NOT NULL,
    taken_at TEXT NOT NULL,
    sync_status TEXT NOT NULL DEFAULT 'PENDING',
    storage_key TEXT
);

insertPhoto:
INSERT INTO photos (id, checklist_item_result_id, file_uri, taken_at, sync_status, storage_key)
VALUES (?, ?, ?, ?, ?, ?);

selectByChecklistItemResultId:
SELECT * FROM photos WHERE checklist_item_result_id = ?;

selectPending:
SELECT * FROM photos WHERE sync_status = 'PENDING';

updateSyncStatus:
UPDATE photos SET sync_status = ?, storage_key = ? WHERE id = ?;
```

- [ ] **Step 14: Создать ActionLog.sq**

```sql
CREATE TABLE IF NOT EXISTS action_logs (
    id TEXT NOT NULL PRIMARY KEY,
    inspection_id TEXT NOT NULL,
    action_type TEXT NOT NULL,
    metadata TEXT,
    created_at TEXT NOT NULL,
    sync_status TEXT NOT NULL DEFAULT 'PENDING'
);

insertActionLog:
INSERT INTO action_logs (id, inspection_id, action_type, metadata, created_at, sync_status)
VALUES (?, ?, ?, ?, ?, ?);

selectByInspectionId:
SELECT * FROM action_logs WHERE inspection_id = ?;

selectPending:
SELECT * FROM action_logs WHERE sync_status = 'PENDING';

updateSyncStatus:
UPDATE action_logs SET sync_status = ? WHERE id = ?;
```

- [ ] **Step 15: Создать SyncBatch.sq и SyncMeta.sq**

`SyncBatch.sq`:
```sql
CREATE TABLE IF NOT EXISTS sync_batches (
    id TEXT NOT NULL PRIMARY KEY,
    sent_at TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING',
    error_message TEXT
);

insertBatch:
INSERT INTO sync_batches (id, sent_at, status, error_message) VALUES (?, ?, ?, ?);

updateStatus:
UPDATE sync_batches SET status = ?, error_message = ? WHERE id = ?;

selectLatest:
SELECT * FROM sync_batches ORDER BY sent_at DESC LIMIT 1;
```

`SyncMeta.sq`:
```sql
CREATE TABLE IF NOT EXISTS sync_meta (
    key TEXT NOT NULL PRIMARY KEY,
    value TEXT NOT NULL
);

upsert:
INSERT OR REPLACE INTO sync_meta (key, value) VALUES (?, ?);

selectByKey:
SELECT value FROM sync_meta WHERE key = ?;
```

---

### Task 4: Создать DatabaseDriverFactory (expect/actual)

**Files:**
- Create: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/driver/DatabaseDriverFactory.kt`
- Create: `shared/core-database/src/androidMain/kotlin/ru/mirea/toir/core/database/driver/DatabaseDriverFactory.android.kt`
- Create: `shared/core-database/src/iosMain/kotlin/ru/mirea/toir/core/database/driver/DatabaseDriverFactory.ios.kt`

- [ ] **Step 1: Создать expect-класс**

`commonMain/kotlin/ru/mirea/toir/core/database/driver/DatabaseDriverFactory.kt`:
```kotlin
package ru.mirea.toir.core.database.driver

import app.cash.sqldelight.db.SqlDriver

internal expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}
```

- [ ] **Step 2: Создать actual для Android**

`androidMain/kotlin/ru/mirea/toir/core/database/driver/DatabaseDriverFactory.android.kt`:
```kotlin
package ru.mirea.toir.core.database.driver

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import ru.mirea.toir.core.database.ToirDatabase

internal actual class DatabaseDriverFactory(private val context: Context) {
    actual fun create(): SqlDriver =
        AndroidSqliteDriver(
            schema = ToirDatabase.Schema,
            context = context,
            name = "toir.db",
        )
}
```

- [ ] **Step 3: Создать actual для iOS**

`iosMain/kotlin/ru/mirea/toir/core/database/driver/DatabaseDriverFactory.ios.kt`:
```kotlin
package ru.mirea.toir.core.database.driver

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import ru.mirea.toir.core.database.ToirDatabase

internal actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver =
        NativeSqliteDriver(
            schema = ToirDatabase.Schema,
            name = "toir.db",
        )
}
```

---

### Task 5: Создать DAO-обёртки

DAO — тонкие Kotlin-обёртки над сгенерированными SQLDelight-запросами. Каждый DAO — `internal class` в модуле.

**Files:**
- Create: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/dao/UserDao.kt`
- Create: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/dao/EquipmentDao.kt`
- Create: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/dao/RouteDao.kt`
- Create: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/dao/InspectionDao.kt`
- Create: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/dao/ChecklistDao.kt`
- Create: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/dao/PhotoDao.kt`
- Create: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/dao/ActionLogDao.kt`
- Create: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/dao/SyncMetaDao.kt`

- [ ] **Step 1: Создать UserDao**

```kotlin
package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.Users

internal class UserDao(db: ToirDatabase) {
    private val queries = db.userQueries

    fun upsert(id: String, login: String, displayName: String, role: String) {
        queries.upsertUser(id = id, login = login, display_name = displayName, role = role)
    }

    fun selectAll(): List<Users> = queries.selectAll().executeAsList()

    fun selectById(id: String): Users? = queries.selectById(id).executeAsOneOrNull()

    fun deleteAll() = queries.deleteAll()
}
```

- [ ] **Step 2: Создать EquipmentDao**

```kotlin
package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.Equipment
import ru.mirea.toir.core.database.ToirDatabase

internal class EquipmentDao(db: ToirDatabase) {
    private val queries = db.equipmentQueries

    fun upsert(id: String, code: String, name: String, type: String, locationId: String) {
        queries.upsertEquipment(id = id, code = code, name = name, type = type, location_id = locationId)
    }

    fun selectAll(): List<Equipment> = queries.selectAll().executeAsList()

    fun selectById(id: String): Equipment? = queries.selectById(id).executeAsOneOrNull()

    fun deleteAll() = queries.deleteAll()
}
```

- [ ] **Step 3: Создать RouteDao**

```kotlin
package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.Route_assignments
import ru.mirea.toir.core.database.Route_points
import ru.mirea.toir.core.database.Routes
import ru.mirea.toir.core.database.ToirDatabase

internal class RouteDao(db: ToirDatabase) {
    private val routeQueries = db.routeQueries
    private val pointQueries = db.routePointQueries
    private val assignmentQueries = db.routeAssignmentQueries

    fun upsertRoute(id: String, name: String, description: String?) {
        routeQueries.upsertRoute(id = id, name = name, description = description)
    }

    fun selectAllRoutes(): List<Routes> = routeQueries.selectAll().executeAsList()

    fun selectRouteById(id: String): Routes? = routeQueries.selectById(id).executeAsOneOrNull()

    fun upsertRoutePoint(id: String, routeId: String, equipmentId: String, checklistId: String, orderIndex: Long) {
        pointQueries.upsertRoutePoint(
            id = id, route_id = routeId, equipment_id = equipmentId,
            checklist_id = checklistId, order_index = orderIndex,
        )
    }

    fun selectPointsByRouteId(routeId: String): List<Route_points> =
        pointQueries.selectByRouteId(routeId).executeAsList()

    fun selectPointById(id: String): Route_points? = pointQueries.selectById(id).executeAsOneOrNull()

    fun upsertAssignment(id: String, routeId: String, userId: String, status: String, assignedAt: String, dueDate: String?) {
        assignmentQueries.upsertRouteAssignment(
            id = id, route_id = routeId, user_id = userId,
            status = status, assigned_at = assignedAt, due_date = dueDate,
        )
    }

    fun selectAllAssignments(): List<Route_assignments> = assignmentQueries.selectAll().executeAsList()

    fun selectAssignmentById(id: String): Route_assignments? = assignmentQueries.selectById(id).executeAsOneOrNull()

    fun updateAssignmentStatus(id: String, status: String) {
        assignmentQueries.updateStatus(status = status, id = id)
    }

    fun deleteAllRoutes() = routeQueries.deleteAll()

    fun deleteAllPoints() = pointQueries.deleteAll()

    fun deleteAllAssignments() = assignmentQueries.deleteAll()
}
```

- [ ] **Step 4: Создать InspectionDao**

```kotlin
package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.Checklist_item_results
import ru.mirea.toir.core.database.Inspection_equipment_results
import ru.mirea.toir.core.database.Inspections
import ru.mirea.toir.core.database.ToirDatabase

internal class InspectionDao(db: ToirDatabase) {
    private val inspectionQueries = db.inspectionQueries
    private val equipmentResultQueries = db.inspectionEquipmentResultQueries
    private val checklistItemResultQueries = db.checklistItemResultQueries

    fun insertInspection(id: String, assignmentId: String, routeId: String, status: String, startedAt: String) {
        inspectionQueries.insertInspection(
            id = id, assignment_id = assignmentId, route_id = routeId,
            status = status, started_at = startedAt, completed_at = null, sync_status = "PENDING",
        )
    }

    fun selectByAssignmentId(assignmentId: String): Inspections? =
        inspectionQueries.selectByAssignmentId(assignmentId).executeAsOneOrNull()

    fun selectById(id: String): Inspections? = inspectionQueries.selectById(id).executeAsOneOrNull()

    fun updateInspectionStatus(id: String, status: String, completedAt: String?) {
        inspectionQueries.updateStatus(status = status, completed_at = completedAt, id = id)
    }

    fun selectPendingInspections(): List<Inspections> =
        inspectionQueries.selectPending().executeAsList()

    fun updateInspectionSyncStatus(id: String, syncStatus: String) {
        inspectionQueries.updateSyncStatus(sync_status = syncStatus, id = id)
    }

    fun insertEquipmentResult(id: String, inspectionId: String, routePointId: String, equipmentId: String, status: String) {
        equipmentResultQueries.insertResult(
            id = id, inspection_id = inspectionId, route_point_id = routePointId,
            equipment_id = equipmentId, status = status, started_at = null,
            completed_at = null, sync_status = "PENDING",
        )
    }

    fun selectEquipmentResultsByInspectionId(inspectionId: String): List<Inspection_equipment_results> =
        equipmentResultQueries.selectByInspectionId(inspectionId).executeAsList()

    fun selectEquipmentResultById(id: String): Inspection_equipment_results? =
        equipmentResultQueries.selectById(id).executeAsOneOrNull()

    fun selectEquipmentResultByRoutePoint(routePointId: String, inspectionId: String): Inspection_equipment_results? =
        equipmentResultQueries.selectByRoutePointAndInspection(routePointId, inspectionId).executeAsOneOrNull()

    fun updateEquipmentResultStatus(id: String, status: String, startedAt: String?, completedAt: String?) {
        equipmentResultQueries.updateStatus(status = status, started_at = startedAt, completed_at = completedAt, id = id)
    }

    fun selectPendingEquipmentResults(): List<Inspection_equipment_results> =
        equipmentResultQueries.selectPending().executeAsList()

    fun updateEquipmentResultSyncStatus(id: String, syncStatus: String) {
        equipmentResultQueries.updateSyncStatus(sync_status = syncStatus, id = id)
    }

    fun insertOrReplaceChecklistItemResult(
        id: String,
        equipmentResultId: String,
        checklistItemId: String,
        valueBoolean: Long?,
        valueNumber: Double?,
        valueText: String?,
        valueSelect: String?,
        isConfirmed: Long,
        answeredAt: String?,
    ) {
        checklistItemResultQueries.insertOrReplaceResult(
            id = id,
            inspection_equipment_result_id = equipmentResultId,
            checklist_item_id = checklistItemId,
            value_boolean = valueBoolean,
            value_number = valueNumber,
            value_text = valueText,
            value_select = valueSelect,
            is_confirmed = isConfirmed,
            answered_at = answeredAt,
            sync_status = "PENDING",
        )
    }

    fun selectChecklistItemResultsByEquipmentResult(equipmentResultId: String): List<Checklist_item_results> =
        checklistItemResultQueries.selectByEquipmentResultId(equipmentResultId).executeAsList()

    fun selectChecklistItemResult(checklistItemId: String, equipmentResultId: String): Checklist_item_results? =
        checklistItemResultQueries.selectByChecklistItemAndEquipmentResult(checklistItemId, equipmentResultId)
            .executeAsOneOrNull()

    fun selectPendingChecklistItemResults(): List<Checklist_item_results> =
        checklistItemResultQueries.selectPending().executeAsList()

    fun updateChecklistItemResultSyncStatus(id: String, syncStatus: String) {
        checklistItemResultQueries.updateSyncStatus(sync_status = syncStatus, id = id)
    }
}
```

- [ ] **Step 5: Создать ChecklistDao**

```kotlin
package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.Checklist_items
import ru.mirea.toir.core.database.Checklists
import ru.mirea.toir.core.database.ToirDatabase

internal class ChecklistDao(db: ToirDatabase) {
    private val checklistQueries = db.checklistQueries
    private val itemQueries = db.checklistItemQueries

    fun upsertChecklist(id: String, name: String, equipmentId: String?) {
        checklistQueries.upsertChecklist(id = id, name = name, equipment_id = equipmentId)
    }

    fun selectById(id: String): Checklists? = checklistQueries.selectById(id).executeAsOneOrNull()

    fun upsertItem(
        id: String,
        checklistId: String,
        title: String,
        description: String?,
        answerType: String,
        isRequired: Long,
        requiresPhoto: Long,
        selectOptions: String?,
        orderIndex: Long,
    ) {
        itemQueries.upsertChecklistItem(
            id = id, checklist_id = checklistId, title = title, description = description,
            answer_type = answerType, is_required = isRequired, requires_photo = requiresPhoto,
            select_options = selectOptions, order_index = orderIndex,
        )
    }

    fun selectItemsByChecklistId(checklistId: String): List<Checklist_items> =
        itemQueries.selectByChecklistId(checklistId).executeAsList()

    fun selectItemById(id: String): Checklist_items? = itemQueries.selectById(id).executeAsOneOrNull()

    fun deleteAll() {
        checklistQueries.deleteAll()
        itemQueries.deleteAll()
    }
}
```

- [ ] **Step 6: Создать PhotoDao**

```kotlin
package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.Photos
import ru.mirea.toir.core.database.ToirDatabase

internal class PhotoDao(db: ToirDatabase) {
    private val queries = db.photoQueries

    fun insert(id: String, checklistItemResultId: String, fileUri: String, takenAt: String) {
        queries.insertPhoto(
            id = id,
            checklist_item_result_id = checklistItemResultId,
            file_uri = fileUri,
            taken_at = takenAt,
            sync_status = "PENDING",
            storage_key = null,
        )
    }

    fun selectByChecklistItemResultId(checklistItemResultId: String): List<Photos> =
        queries.selectByChecklistItemResultId(checklistItemResultId).executeAsList()

    fun selectPending(): List<Photos> = queries.selectPending().executeAsList()

    fun updateSyncStatus(id: String, syncStatus: String, storageKey: String?) {
        queries.updateSyncStatus(sync_status = syncStatus, storage_key = storageKey, id = id)
    }
}
```

- [ ] **Step 7: Создать ActionLogDao**

```kotlin
package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.Action_logs
import ru.mirea.toir.core.database.ToirDatabase

internal class ActionLogDao(db: ToirDatabase) {
    private val queries = db.actionLogQueries

    fun insert(id: String, inspectionId: String, actionType: String, metadata: String?, createdAt: String) {
        queries.insertActionLog(
            id = id,
            inspection_id = inspectionId,
            action_type = actionType,
            metadata = metadata,
            created_at = createdAt,
            sync_status = "PENDING",
        )
    }

    fun selectPending(): List<Action_logs> = queries.selectPending().executeAsList()

    fun updateSyncStatus(id: String, syncStatus: String) {
        queries.updateSyncStatus(sync_status = syncStatus, id = id)
    }
}
```

- [ ] **Step 8: Создать SyncMetaDao**

```kotlin
package ru.mirea.toir.core.database.dao

import ru.mirea.toir.core.database.ToirDatabase

internal class SyncMetaDao(db: ToirDatabase) {
    private val queries = db.syncMetaQueries

    fun upsert(key: String, value: String) {
        queries.upsert(key = key, value = value)
    }

    fun selectByKey(key: String): String? = queries.selectByKey(key).executeAsOneOrNull()

    companion object {
        const val KEY_LAST_SYNC_TIME = "last_sync_time"
    }
}
```

---

### Task 6: Создать DI-модуль для core-database

**Files:**
- Create: `shared/core-database/src/commonMain/kotlin/ru/mirea/toir/core/database/di/CoreDatabaseModule.kt`
- Create: `shared/core-database/src/androidMain/kotlin/ru/mirea/toir/core/database/di/PlatformCoreDatabaseModule.android.kt`
- Create: `shared/core-database/src/iosMain/kotlin/ru/mirea/toir/core/database/di/PlatformCoreDatabaseModule.ios.kt`

- [ ] **Step 1: Создать commonMain DI**

```kotlin
package ru.mirea.toir.core.database.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.core.database.ToirDatabase
import ru.mirea.toir.core.database.dao.ActionLogDao
import ru.mirea.toir.core.database.dao.ChecklistDao
import ru.mirea.toir.core.database.dao.EquipmentDao
import ru.mirea.toir.core.database.dao.InspectionDao
import ru.mirea.toir.core.database.dao.PhotoDao
import ru.mirea.toir.core.database.dao.RouteDao
import ru.mirea.toir.core.database.dao.SyncMetaDao
import ru.mirea.toir.core.database.dao.UserDao

internal expect val platformCoreDatabaseModule: Module

val coreDatabaseModule = module {
    includes(platformCoreDatabaseModule)

    single<ToirDatabase> { ToirDatabase(driver = get<ru.mirea.toir.core.database.driver.DatabaseDriverFactory>().create()) }

    factory { new(::UserDao) }
    factory { new(::EquipmentDao) }
    factory { new(::RouteDao) }
    factory { new(::InspectionDao) }
    factory { new(::ChecklistDao) }
    factory { new(::PhotoDao) }
    factory { new(::ActionLogDao) }
    factory { new(::SyncMetaDao) }
}
```

- [ ] **Step 2: Создать Android actual DI**

`androidMain/.../di/PlatformCoreDatabaseModule.android.kt`:
```kotlin
package ru.mirea.toir.core.database.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.mirea.toir.core.database.driver.DatabaseDriverFactory

internal actual val platformCoreDatabaseModule = module {
    factory { DatabaseDriverFactory(context = androidContext()) }
}
```

- [ ] **Step 3: Создать iOS actual DI**

`iosMain/.../di/PlatformCoreDatabaseModule.ios.kt`:
```kotlin
package ru.mirea.toir.core.database.di

import org.koin.dsl.module
import ru.mirea.toir.core.database.driver.DatabaseDriverFactory

internal actual val platformCoreDatabaseModule = module {
    factory { DatabaseDriverFactory() }
}
```

- [ ] **Step 4: Зарегистрировать coreDatabaseModule в Koin**

В `shared/main/src/commonMain/kotlin/ru/mirea/toir/di/Koin.kt` добавить:
```kotlin
import ru.mirea.toir.core.database.di.coreDatabaseModule
// ...
modules(
    // ...
    coreDatabaseModule,
    // ...
)
```

---

### Task 7: Финальная проверка

- [ ] **Step 1: Собрать Android APK**

```bash
./gradlew :android:app:assembleDebug
```
Ожидаемый результат: `BUILD SUCCESSFUL`

- [ ] **Step 2: Запустить Detekt**

```bash
./gradlew detekt
```
Ожидаемый результат: `BUILD SUCCESSFUL` или предупреждения без ошибок.

- [ ] **Step 3: Commit**

```bash
git add shared/core-database/ settings.gradle.kts shared/main/src/
git commit -m "feat: add SQLDelight core-database module with all entity tables and DAOs"
```
