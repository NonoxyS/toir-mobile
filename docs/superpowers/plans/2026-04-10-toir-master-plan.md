# Toir Mobile — Master Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement each waypoint. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Реализовать мобильное приложение обхода оборудования (ТОиР) с offline-first архитектурой поверх существующего KMM-шаблона.

**Architecture:** Клиент-серверное приложение с SQLDelight для offline-хранения всех данных, синхронизацией через REST API. MVI (MVIKotlin) + Koin + Compose Multiplatform. Каждый экран — отдельный feature-модуль (4 подмодуля: api/impl/presentation/ui).

**Tech Stack:** Kotlin 2.3.10, Compose Multiplatform 1.10.1, MVIKotlin 4.3.0, Koin 4.1.1, Ktor 3.3.3, SQLDelight 2.0.2, moko-resources 0.26.0, DataStore 1.2.0.

---

## Waypoints

| # | Файл | Что строим | Входные данные |
|---|------|------------|----------------|
| 01 | [01-core-database.md](01-core-database.md) | SQLDelight DB: все таблицы, DAOs, DI | Спецификация сущностей (раздел 5 мобильной спеки) |
| 02 | [02-feature-auth.md](02-feature-auth.md) | Авторизация: экран входа, JWT-хранение, Ktor-перехватчик | `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh` |
| 03 | [03-bootstrap-shell.md](03-bootstrap-shell.md) | Bootstrap: начальная загрузка данных, рефактор NavHost, удаление demo | `GET /api/v1/mobile/bootstrap`, auth guard |
| 04 | [04-feature-routes-list.md](04-feature-routes-list.md) | Список маршрутов: главный экран, карточки с прогрессом | Данные из SQLDelight (RouteAssignment + Route) |
| 05 | [05-feature-inspection-flow.md](05-feature-inspection-flow.md) | Точки маршрута + Карточка оборудования | Inspection, RoutePoint, Equipment из DB |
| 06 | [06-feature-checklist.md](06-feature-checklist.md) | Чек-лист: все типы ответов, обязательность, валидация | ChecklistItem, ChecklistItemResult в DB |
| 07 | [07-feature-photo-capture.md](07-feature-photo-capture.md) | Фотофиксация: камера/галерея, привязка к пункту чек-листа | Photo в DB, `POST /api/v1/mobile/photos/upload` |
| 08 | [08-sync-manager.md](08-sync-manager.md) | SyncManager: push-синхронизация, дельта, загрузка фото | `POST /api/v1/mobile/sync/push`, `GET /api/v1/mobile/config/changes` |

---

## Порядок выполнения

```
01 → 02 → 03 → 04 → 05 → 06 → 07 → 08
```

Каждый waypoint — это самодостаточная единица работы, которая компилируется и имеет проверяемый результат. Не начинай следующий waypoint, пока предыдущий не завершён (зелёный билд + ручная проверка).

---

## Важные ограничения

- **API контракт:** все поля DTO берутся **только** из `~/IdeaProjects/toir-backend/src/main/resources/openapi/documentation.yaml`. Не угадывай поля по названию.
- **Пакет:** `ru.mirea.toir`
- **Строки:** только через `MR.strings.*` — нет хардкода в Composable.
- **Обработка ошибок:** `coRunCatching` в suspend-функциях, `Napier.e(...)` в репозиториях.
- **DTO:** `@Serializable` + `@SerialName` на каждом поле, nullable вместо default values.
- **Все зависимости** — через `gradle/libs.versions.toml`, не хардкодить версии.

---

## Новые модули, которые нужно создать

```
shared/core-database/                          # Waypoint 01
shared/feature-auth/{api,impl,presentation,ui} # Waypoint 02
shared/feature-routes-list/{api,...}           # Waypoint 04
shared/feature-route-points/{api,...}          # Waypoint 05
shared/feature-equipment-card/{api,...}        # Waypoint 05
shared/feature-checklist/{api,...}             # Waypoint 06
shared/feature-photo-capture/{api,...}         # Waypoint 07
shared/sync-manager/                           # Waypoint 08
```

Для создания модулей используй IntelliJ-плагин **KMP Module Generator** (шаблоны в `module-templates/mvikotlin-feature`) либо создавай файловую структуру вручную по образцу `shared/feature-demo-second`.
