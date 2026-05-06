# Screens Spec Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Привести `BootstrapScreen`, `EquipmentCardScreen`, `PhotoCaptureScreen` в полное соответствие со спеками `pages/bootstrap.md`, `pages/equipment-card.md`, `pages/photo-capture.md` (включая bug fix `MaterialTheme → ToirTheme` в Bootstrap, обработку 401 в `BootstrapExecutor`, новые компоненты UI и расширенные взаимодействия с фото).

**Architecture:** Изменения локализованы в трёх feature-модулях + общие ресурсы (icons, strings) + один новый общий компонент `StatusBadge` в `common-ui`. Контракт `BootstrapRepository` расширяется новым sealed-типом `BootstrapResult`, чтобы `BootstrapExecutor` мог различать 401 от прочих ошибок. Photo Capture получает расширенный store (`maxPhotos`, `OnPhotoDeleted`), новые UI-компоненты (preview, dialogs, empty state) и shared element transition + pinch-to-zoom через Compose Multiplatform 1.7+.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform 1.7+ (для `SharedTransitionLayout`), MVIKotlin, Koin, moko-resources, Coil3, kotlinx-coroutines-test (для unit-тестов executor/reducer).

---

## Scope Notes

- **QR-сканирование** в `equipment-card.md` описано как «целевой UX, модуль не реализован». В этом плане **не реализуем сканер**, но реализуем conditional-логику (`requiresQr` хардкодится в `false` через UI-маппер до появления data-поля). Когда позже появится `equipment.requiresQr` в data model — UI «загорится» сам, без правок.
- **Тестирование UI** — через `@Preview` composables (project pattern, см. `EquipmentCardScreen.kt`). Юнит-тесты пишем для нетривиальной логики: `BootstrapExecutor` (401 handler), `PhotoCaptureReducer` (max photos, delete).
- **Иконки** — все новые SVG берутся из [Material Symbols Outlined](https://fonts.google.com/icons) (weight 200–300, size 24, style "Outlined"). Файлы кладутся в `shared/common-resources/src/commonMain/moko-resources/images/`.

---

## File Structure

### Создаются

| Path | Ответственность |
|---|---|
| `shared/common-resources/src/commonMain/moko-resources/images/ic_camera_alt.svg` | Иконка камеры (Material Symbols Outlined) |
| `shared/common-resources/src/commonMain/moko-resources/images/ic_qr_code_scanner.svg` | Иконка QR-сканера |
| `shared/common-resources/src/commonMain/moko-resources/images/ic_info.svg` | Информация (i в круге) |
| `shared/common-resources/src/commonMain/moko-resources/images/ic_close.svg` | Закрытие (X) |
| `shared/common-resources/src/commonMain/moko-resources/images/ic_error_outline.svg` | Ошибка (! в круге, outlined) |
| `shared/common-resources/src/commonMain/moko-resources/images/ic_check_circle.svg` | Подтверждение (✓ в круге) |
| `shared/common-resources/src/commonMain/moko-resources/images/ic_arrow_back.svg` | Стрелка «назад» |
| `shared/common-resources/src/commonMain/moko-resources/images/ic_broken_image.svg` | Битая картинка (placeholder) |
| `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/components/shared/badge/StatusBadge.kt` | Универсальный статус-бейдж (§8.4 MASTER) |
| `shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/repository/BootstrapResult.kt` | Sealed result type |
| `shared/feature-bootstrap/impl/src/commonTest/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/BootstrapExecutorTest.kt` | Unit-тесты 401 handler |
| `shared/feature-bootstrap/impl/src/commonTest/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/FakeBootstrapRepository.kt` | Test fake |
| `shared/feature-bootstrap/impl/src/commonTest/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/FakeAuthRepository.kt` | Test fake |
| `shared/feature-equipment-card/ui/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/ui/components/EquipmentCardLayout.kt` | Одна общая карточка с accent bar |
| `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoCaptureEmptyState.kt` | Пустое состояние ленты |
| `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoCaptureFooter.kt` | Sticky footer с двумя кнопками |
| `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/preview/PhotoPreviewScreen.kt` | Full-screen preview (с zoom + shared transition) |
| `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoDeleteConfirmDialog.kt` | Диалог удаления |
| `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoExitConfirmDialog.kt` | Диалог выхода с несохранёнными |
| `shared/feature-photo-capture/impl/src/commonTest/kotlin/ru/mirea/toir/feature/photo/capture/impl/domain/PhotoCaptureReducerTest.kt` | Unit-тесты state changes |

### Модифицируются

| Path | Изменение |
|---|---|
| `shared/common-resources/src/commonMain/moko-resources/base/strings.xml` | Добавить ~20 новых строк (см. Task 1) |
| `shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/repository/BootstrapRepository.kt` | `Result<Unit>` → `BootstrapResult` |
| `shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/data/repository/BootstrapRepositoryImpl.kt` | Возвращать `BootstrapResult`, ловить 401 |
| `shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/BootstrapExecutor.kt` | Различать 401 / прочие ошибки |
| `shared/feature-bootstrap/ui/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/ui/BootstrapScreen.kt` | Полная переписка по спеке |
| `shared/feature-equipment-card/ui/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/ui/EquipmentCardScreen.kt` | Использовать `EquipmentCardLayout`; иконка `ic_arrow_back` вместо «←» |
| `shared/feature-equipment-card/ui/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/ui/components/EquipmentCardContent.kt` | Поля без рамок и фона (одна общая карточка снаружи) |
| `shared/feature-equipment-card/ui/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/ui/components/EquipmentCardOpenChecklistButton.kt` | `vertical = 16.dp` (вместо 12.dp) |
| `shared/feature-photo-capture/api/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/api/store/PhotoCaptureStore.kt` | `maxPhotos: Int?`, `OnPhotoDeleted`, `OnExitRequested` |
| `shared/feature-photo-capture/impl/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/impl/domain/PhotoCaptureReducer.kt` | Обработать новые intents/messages |
| `shared/feature-photo-capture/impl/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/impl/domain/PhotoCaptureStoreFactory.kt` | Принимать `maxPhotos` как параметр |
| `shared/feature-photo-capture/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/presentation/models/UiPhotoCaptureState.kt` | `maxPhotos: Int?` |
| `shared/feature-photo-capture/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/presentation/mappers/UiPhotoCaptureStateMapper.kt` | Map `maxPhotos` |
| `shared/feature-photo-capture/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/presentation/PhotoCaptureViewModel.kt` | `onPhotoDeleted`, `onExitRequested` |
| `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/PhotoCaptureScreen.kt` | Перерабатывается под спеку |
| `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoCaptureContent.kt` | Передавать onPhotoTap/onPhotoLongPress, новый layout |
| `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoCapturePhotoItem.kt` | Border + placeholder/error states + click handlers |
| `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoCapturePhotoRow.kt` | `contentPadding(horizontal = 16.dp)` |

---

# Phase A — Foundation

## Task 1: Добавить иконки и строки

**Files:**
- Create: 8 файлов SVG в `shared/common-resources/src/commonMain/moko-resources/images/`
- Modify: `shared/common-resources/src/commonMain/moko-resources/base/strings.xml`

- [ ] **Step 1.1: Скачать SVG иконок Material Symbols Outlined**

Каждая иконка — Material Symbols Outlined, weight 200–300, size 24, style "Outlined", цвет `#000000` (перекрашивается через `tint` в Compose). Источник: https://fonts.google.com/icons.

Скачать 8 SVG (имена в snake_case на сайте Google Fonts):

```
ic_camera_alt.svg        ← "camera_alt" outlined
ic_qr_code_scanner.svg   ← "qr_code_scanner" outlined
ic_info.svg              ← "info" outlined
ic_close.svg             ← "close" outlined
ic_error_outline.svg     ← "error" outlined
ic_check_circle.svg      ← "check_circle" outlined
ic_arrow_back.svg        ← "arrow_back" outlined
ic_broken_image.svg      ← "broken_image" outlined
```

Положить в `shared/common-resources/src/commonMain/moko-resources/images/`.

- [ ] **Step 1.2: Добавить строки в `strings.xml`**

Открыть `shared/common-resources/src/commonMain/moko-resources/base/strings.xml`, добавить **в конец файла** (перед `</resources>`):

```xml
<!-- Bootstrap -->
<string name="bootstrap_title">TOIR</string>
<string name="bootstrap_subtitle">Система технического обхода</string>
<string name="bootstrap_loading">Подготовка...</string>
<string name="bootstrap_error_subtitle">Проверьте подключение и попробуйте снова</string>
<!-- bootstrap_error_title и bootstrap_button_retry уже существуют -->

<!-- Equipment Card — QR (UI placeholder, до появления data-поля requiresQr) -->
<string name="equipment_card_qr_banner">Перед запуском проверки отсканируйте QR-код на оборудовании</string>
<string name="equipment_card_qr_chip">Подтверждено по QR</string>
<string name="equipment_card_button_scan_qr">Сканировать QR</string>
<string name="equipment_card_open_checklist_disabled_hint">Сначала отсканируйте QR</string>

<!-- Equipment Card — accessibility -->
<string name="equipment_card_back_content_description">Назад</string>

<!-- Photo Capture -->
<string name="photo_capture_title_with_progress">Фотофиксация · %1$d / %2$d</string>
<string name="photo_capture_empty_title">Нет фотографий</string>
<string name="photo_capture_empty_subtitle">Снимите первое фото для подтверждения</string>
<string name="photo_capture_limit_reached">Достигнут лимит фотографий</string>
<string name="photo_capture_confirm_disabled_hint">Снимите хотя бы одно фото</string>
<string name="photo_capture_preview_title">Фото %1$d из %2$d</string>
<string name="photo_capture_preview_close_content_description">Закрыть</string>

<!-- Photo Capture — dialogs -->
<string name="photo_delete_title">Удалить фото?</string>
<string name="photo_delete_message">Это действие нельзя отменить.</string>
<string name="photo_exit_title">Отменить фотофиксацию?</string>
<string name="photo_exit_message">Снятые фото будут удалены и не привяжутся к пункту чек-листа.</string>

<!-- Common buttons -->
<string name="common_button_cancel">Отмена</string>
<string name="common_button_delete">Удалить</string>
<string name="common_button_continue_capture">Продолжить съёмку</string>
```

- [ ] **Step 1.3: Сгенерировать MR-классы и убедиться что всё доступно**

```bash
./gradlew :shared:common-resources:generateMRcommonMain
```

Expected: BUILD SUCCESSFUL. После этого `MR.images.ic_camera_alt`, `MR.strings.bootstrap_title` и т.д. доступны.

- [ ] **Step 1.4: Сборка проекта проверяет, что моко-генерация прошла**

```bash
./gradlew :android:app:assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 1.5: Commit**

```bash
git add shared/common-resources/src/commonMain/moko-resources/
git commit -m "feat(resources): add icons and strings for screens spec alignment"
```

---

## Task 2: Создать переиспользуемый StatusBadge компонент

**Files:**
- Create: `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/components/shared/badge/StatusBadge.kt`

Этот компонент нужен EquipmentCardScreen для отображения поля «Статус» как бейджа (§8.4 MASTER), а в будущем — RoutesListScreen и другим.

- [ ] **Step 2.1: Создать файл StatusBadge.kt с композаблом**

```kotlin
package ru.mirea.toir.common.ui.compose.components.shared.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme

@Composable
fun StatusBadge(
    text: String,
    icon: Painter,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .wrapContentSize()
            .clip(ToirTheme.shapes.pill)
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(contentColor),
        )
        Text(
            text = text,
            style = ToirTheme.typography.caption,
            color = contentColor,
        )
    }
}
```

> **Важно:** `painterResource` берётся из **`org.jetbrains.compose.resources`** — если в проекте используется moko-resources, заменить на `dev.icerock.moko.resources.compose.painterResource`. Проверить импорты других composable'ов в проекте перед коммитом (`grep -r "painterResource" shared/common-ui/`).

- [ ] **Step 2.2: Добавить @Preview**

В тот же файл добавить превью:

```kotlin
@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun PreviewStatusBadgeSuccess() {
    ToirTheme {
        StatusBadge(
            text = "Выполнено",
            icon = dev.icerock.moko.resources.compose.painterResource(ru.mirea.toir.res.MR.images.ic_check_circle),
            backgroundColor = ToirTheme.colors.successSubtle,
            contentColor = ToirTheme.colors.success,
        )
    }
}
```

- [ ] **Step 2.3: Сборка**

```bash
./gradlew :shared:common-ui:assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 2.4: Commit**

```bash
git add shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/components/shared/badge/
git commit -m "feat(common-ui): add StatusBadge component (MASTER §8.4)"
```

---

# Phase B — Bootstrap

## Task 3: Создать BootstrapResult sealed type и расширить контракт репозитория

**Files:**
- Create: `shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/repository/BootstrapResult.kt`
- Modify: `shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/repository/BootstrapRepository.kt`
- Modify: `shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/data/repository/BootstrapRepositoryImpl.kt`

- [ ] **Step 3.1: Создать BootstrapResult.kt**

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.domain.repository

internal sealed interface BootstrapResult {
    data object Success : BootstrapResult
    data object Unauthorized : BootstrapResult
    data class Failure(val cause: Throwable) : BootstrapResult
}
```

- [ ] **Step 3.2: Изменить контракт BootstrapRepository.kt**

Полностью заменить содержимое файла:

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.domain.repository

internal interface BootstrapRepository {
    suspend fun loadAndSaveBootstrap(): BootstrapResult
}
```

- [ ] **Step 3.3: Обновить BootstrapRepositoryImpl.kt**

Заменить тело `loadAndSaveBootstrap()` (метод примерно с строки 27, см. файл) на следующее. Импорты добавить: `io.ktor.client.plugins.ClientRequestException`, `io.ktor.http.HttpStatusCode`, `ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapResult`.

```kotlin
override suspend fun loadAndSaveBootstrap(): BootstrapResult =
    withContext(coroutineDispatchers.io) {
        try {
            val response = apiClient.fetchBootstrap().getOrThrow()

            // ... остальной код метода БЕЗ изменений (response.user?.let { ... }, locations, equipment,
            //     checklists, checklistItems, routes, routePoints, assignments, syncMetaStorage.upsert) ...

            BootstrapResult.Success
        } catch (cause: ClientRequestException) {
            if (cause.response.status == HttpStatusCode.Unauthorized) {
                Napier.w("loadAndSaveBootstrap: 401 Unauthorized")
                BootstrapResult.Unauthorized
            } else {
                Napier.e(message = "loadAndSaveBootstrap failed", throwable = cause)
                BootstrapResult.Failure(cause)
            }
        } catch (cause: Throwable) {
            // CancellationException пробрасывается стандартным механизмом try/catch в Kotlin/Coroutines
            // через rethrow ниже
            if (cause is kotlinx.coroutines.CancellationException) throw cause
            Napier.e(message = "loadAndSaveBootstrap failed", throwable = cause)
            BootstrapResult.Failure(cause)
        }
    }
```

> **Важно:** `coRunCatching` + `wrapResultSuccess/Failure` в этом методе **удаляются** — мы переходим на явный try/catch с разбором типов исключений. CancellationException пробрасываем вручную, как делал бы `coRunCatching`.

- [ ] **Step 3.4: Сборка модуля**

```bash
./gradlew :shared:feature-bootstrap:impl:assembleDebug
```

Expected: BUILD SUCCESSFUL. Если падает — подсмотреть в `BootstrapRepositoryImpl.kt`, что неудалённые `wrapResultSuccess()` / `wrapResultFailure()` остались — убрать.

- [ ] **Step 3.5: Commit**

```bash
git add shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/repository/ \
        shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/data/repository/
git commit -m "refactor(bootstrap): introduce BootstrapResult sealed type for explicit 401 handling"
```

---

## Task 4: Реализовать обработку 401 в BootstrapExecutor (TDD)

**Files:**
- Create: `shared/feature-bootstrap/impl/src/commonTest/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/FakeBootstrapRepository.kt`
- Create: `shared/feature-bootstrap/impl/src/commonTest/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/FakeAuthRepository.kt`
- Create: `shared/feature-bootstrap/impl/src/commonTest/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/BootstrapExecutorTest.kt`
- Modify: `shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/BootstrapExecutor.kt`

- [ ] **Step 4.1: Проверить, есть ли в проекте папка commonTest для feature-bootstrap/impl**

```bash
ls shared/feature-bootstrap/impl/src/commonTest/kotlin/ 2>/dev/null && echo "exists" || echo "missing"
```

Если `missing` — создать структуру:

```bash
mkdir -p shared/feature-bootstrap/impl/src/commonTest/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain
```

Проверить `shared/feature-bootstrap/impl/build.gradle.kts` — что тестовые зависимости подключены. Если нет, добавить (по аналогии с `feature-checklist/impl/build.gradle.kts`):

```kotlin
sourceSets {
    commonTest.dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.mvikotlin.testing)
    }
}
```

(Точные имена зависимостей сверять с `gradle/libs.versions.toml` и существующими тестами в других impl-модулях — например, `feature-checklist/impl`.)

- [ ] **Step 4.2: Создать FakeBootstrapRepository.kt**

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.domain

import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapRepository
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapResult

internal class FakeBootstrapRepository(
    var nextResult: BootstrapResult = BootstrapResult.Success,
) : BootstrapRepository {
    var callCount: Int = 0
        private set

    override suspend fun loadAndSaveBootstrap(): BootstrapResult {
        callCount++
        return nextResult
    }
}
```

- [ ] **Step 4.3: Создать FakeAuthRepository.kt**

Сначала глянуть актуальный интерфейс `AuthRepository`:

```bash
find shared -name 'AuthRepository.kt' | head -3
```

Прочитать его и реализовать минимальный fake. Пример (с поправкой на реальный интерфейс):

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.domain

import io.ktor.client.plugins.auth.providers.BearerTokens
import ru.mirea.toir.core.auth.domain.repository.AuthRepository

internal class FakeAuthRepository(
    var tokens: BearerTokens? = BearerTokens("access", "refresh"),
) : AuthRepository {
    var logoutCallCount: Int = 0
        private set

    override suspend fun getBearerTokens(): Result<BearerTokens?> = Result.success(tokens)
    override suspend fun logout(): Result<Unit> {
        logoutCallCount++
        tokens = null
        return Result.success(Unit)
    }

    // Прочие методы — заглушки, бросающие NotImplementedError, либо возвращающие Result.success(...)
    // в зависимости от сигнатуры. Сверить с реальным AuthRepository.
}
```

- [ ] **Step 4.4: Написать failing test для 401 handler**

```kotlin
package ru.mirea.toir.feature.bootstrap.impl.domain

import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import ru.mirea.toir.feature.bootstrap.api.store.BootstrapStore.Label
import ru.mirea.toir.feature.bootstrap.impl.domain.repository.BootstrapResult
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BootstrapExecutorTest {

    @BeforeTest
    fun disableMainThreadAssertions() {
        isAssertOnMainThreadEnabled = false
    }

    @Test
    fun `when bootstrap returns Unauthorized — executor calls logout and publishes NavigateToLogin`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val repository = FakeBootstrapRepository(nextResult = BootstrapResult.Unauthorized)
            val authRepository = FakeAuthRepository()

            val store = BootstrapStoreFactory(
                storeFactory = DefaultStoreFactory(),
                bootstrapRepository = repository,
                authRepository = authRepository,
                mainDispatcher = dispatcher,
            ).create()

            val firstLabel = store.labels.first()

            assertEquals(Label.NavigateToLogin, firstLabel)
            assertEquals(1, authRepository.logoutCallCount)
        }

    @Test
    fun `when bootstrap returns Success — executor publishes NavigateToRoutesList`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val repository = FakeBootstrapRepository(nextResult = BootstrapResult.Success)
        val authRepository = FakeAuthRepository()

        val store = BootstrapStoreFactory(
            storeFactory = DefaultStoreFactory(),
            bootstrapRepository = repository,
            authRepository = authRepository,
            mainDispatcher = dispatcher,
        ).create()

        val firstLabel = store.labels.first()
        assertEquals(Label.NavigateToRoutesList, firstLabel)
    }

    @Test
    fun `when bootstrap returns Failure — executor sets error state, no navigation`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val repository = FakeBootstrapRepository(nextResult = BootstrapResult.Failure(RuntimeException("boom")))
        val authRepository = FakeAuthRepository()

        val store = BootstrapStoreFactory(
            storeFactory = DefaultStoreFactory(),
            bootstrapRepository = repository,
            authRepository = authRepository,
            mainDispatcher = dispatcher,
        ).create()

        // wait one collection cycle
        val state = store.states.take(2).toList().last()
        assertEquals(true, state.isError)
        assertEquals(false, state.isLoading)
    }
}
```

> Если в проекте уже есть `BootstrapReducerTest` или подобные — повторить их паттерн (см. `feature-bootstrap/impl/src/commonTest/`). Имена классов (`BootstrapStoreFactory`) — взять из реального кода.

- [ ] **Step 4.5: Запустить тесты — должны упасть на «when bootstrap returns Unauthorized»**

```bash
./gradlew :shared:feature-bootstrap:impl:allTests
```

Expected: FAIL — `Label.NavigateToLogin` ожидался, но executor его не публикует (либо тест падает на компиляции, если ещё не обновили executor).

- [ ] **Step 4.6: Обновить BootstrapExecutor.kt — обработка результата**

Заменить тело `loadBootstrap()`:

```kotlin
private suspend fun loadBootstrap() {
    dispatch(Message.SetLoading)
    val tokens = authRepository.getBearerTokens().getOrNull()
    if (tokens == null) {
        dispatch(Message.ClearLoading)
        publish(Label.NavigateToLogin)
        return
    }
    when (val result = bootstrapRepository.loadAndSaveBootstrap()) {
        BootstrapResult.Success -> {
            dispatch(Message.ClearLoading)
            publish(Label.NavigateToRoutesList)
        }
        BootstrapResult.Unauthorized -> {
            authRepository.logout()
            dispatch(Message.ClearLoading)
            publish(Label.NavigateToLogin)
        }
        is BootstrapResult.Failure -> {
            dispatch(Message.SetError)
        }
    }
}
```

- [ ] **Step 4.7: Запустить тесты — должны пройти**

```bash
./gradlew :shared:feature-bootstrap:impl:allTests
```

Expected: PASS (все 3 теста).

- [ ] **Step 4.8: Commit**

```bash
git add shared/feature-bootstrap/impl/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/impl/domain/BootstrapExecutor.kt \
        shared/feature-bootstrap/impl/src/commonTest/
git commit -m "feat(bootstrap): handle 401 by logging out and navigating to login"
```

---

## Task 5: Переписать BootstrapScreen UI по спеке

**Files:**
- Modify: `shared/feature-bootstrap/ui/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/ui/BootstrapScreen.kt`

- [ ] **Step 5.1: Полностью заменить содержимое BootstrapScreen.kt**

```kotlin
package ru.mirea.toir.feature.bootstrap.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.feature.bootstrap.presentation.BootstrapViewModel
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapLabel
import ru.mirea.toir.feature.bootstrap.presentation.models.UiBootstrapState
import ru.mirea.toir.res.MR

@Composable
internal fun BootstrapScreen(
    onNavigateToRoutesList: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: BootstrapViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiBootstrapLabel.NavigateToRoutesList -> onNavigateToRoutesList()
            UiBootstrapLabel.NavigateToLogin -> onNavigateToLogin()
        }
    }

    BootstrapContent(state = state, onRetry = viewModel::onRetry)
}

@Composable
private fun BootstrapContent(
    state: UiBootstrapState,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ToirTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp)) // spacing.xxl от safe area
        BootstrapHeader()
        Spacer(Modifier.height(48.dp)) // spacing.xxl до состояния
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Crossfade(
                targetState = state,
                animationSpec = tween(durationMillis = 200),
                label = "bootstrap-state-crossfade",
            ) { current ->
                when {
                    current.isError -> BootstrapError(onRetry = onRetry)
                    current.isLoading -> BootstrapLoading()
                    else -> Spacer(Modifier.size(0.dp)) // success: no UI
                }
            }
        }
    }
}

@Composable
private fun BootstrapHeader() {
    Text(
        text = stringResource(MR.strings.bootstrap_title),
        style = ToirTheme.typography.displayLarge,
        color = ToirTheme.colors.textPrimary,
    )
    Text(
        text = stringResource(MR.strings.bootstrap_subtitle),
        style = ToirTheme.typography.bodyMedium,
        color = ToirTheme.colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun BootstrapLoading() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = ToirTheme.colors.textSecondary,
        )
        Text(
            text = stringResource(MR.strings.bootstrap_loading),
            style = ToirTheme.typography.caption,
            color = ToirTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun BootstrapError(onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(MR.images.ic_cloud_off),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                colorFilter = ColorFilter.tint(ToirTheme.colors.error),
            )
            Text(
                text = stringResource(MR.strings.bootstrap_error_title),
                style = ToirTheme.typography.bodyLarge,
                color = ToirTheme.colors.textPrimary,
            )
            Text(
                text = stringResource(MR.strings.bootstrap_error_subtitle),
                style = ToirTheme.typography.bodyMedium,
                color = ToirTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        Button(
            onClick = onRetry,
            modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ToirTheme.colors.ctaPrimary,
                contentColor = ToirTheme.colors.textOnAccent,
            ),
        ) {
            Text(
                text = stringResource(MR.strings.bootstrap_button_retry),
                style = ToirTheme.typography.label,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewBootstrapLoading() {
    ToirTheme {
        BootstrapContent(state = UiBootstrapState(isLoading = true), onRetry = {})
    }
}

@Preview
@Composable
private fun PreviewBootstrapError() {
    ToirTheme {
        BootstrapContent(
            state = UiBootstrapState(isLoading = false, isError = true),
            onRetry = {},
        )
    }
}
```

- [ ] **Step 5.2: Сборка**

```bash
./gradlew :shared:feature-bootstrap:ui:assembleDebug :android:app:assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5.3: Manual verification на эмуляторе/устройстве**

Установить debug-сборку на эмулятор:
1. **Loading state** виден сразу при запуске с валидным токеном — TOIR + индикатор + «Подготовка...».
2. **Error state** (отключить интернет на эмуляторе перед запуском, токен валидный) — иконка cloud_off + сообщение + кнопка «Повторить». Тап на «Повторить» при включённой сети → переходит на routes list.
3. **401** (если есть способ протухнуть токен серверно или вручную в SharedPreferences) — переход на Auth.

> Если эмулятор недоступен — пропустить этот step и пометить TODO для ручного тестирования перед merge.

- [ ] **Step 5.4: Commit**

```bash
git add shared/feature-bootstrap/ui/src/commonMain/kotlin/ru/mirea/toir/feature/bootstrap/ui/BootstrapScreen.kt
git commit -m "feat(bootstrap): rewrite screen UI per pages/bootstrap.md spec"
```

---

# Phase C — Equipment Card

## Task 6: Рефакторинг EquipmentCardContent — поля без рамок и фона (одна общая карточка)

**Files:**
- Modify: `shared/feature-equipment-card/ui/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/ui/components/EquipmentCardContent.kt`

- [ ] **Step 6.1: Заменить EquipmentCardField на text-only вариант, основной layout остаётся**

Полностью заменить `EquipmentCardField` (приватный composable в этом же файле):

```kotlin
@Composable
private fun EquipmentCardField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = ToirTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        Text(
            text = value.ifEmpty { "—" },
            style = ToirTheme.typography.bodyLarge,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
```

> Изменения относительно прошлой версии: убран `clip(ToirTheme.shapes.md)`, `background(colors.surface)`, `padding(horizontal = 16.dp, vertical = 12.dp)`, `Spacer8()`. Поле теперь — просто две строки текста с отступом 4dp между ними.

Импорты `clip`, `background`, `Spacer8` (если больше не используются) убрать.

- [ ] **Step 6.2: Заменить лейбл «Код» поля на тип (для соответствия первому полю в DomainEquipmentCard)**

В функции `EquipmentCardContent` оставить вызовы как есть — порядок Code → Name → Type → Location (conditional) → Status уже правильный.

- [ ] **Step 6.3: Сборка модуля**

```bash
./gradlew :shared:feature-equipment-card:ui:assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 6.4: Commit**

```bash
git add shared/feature-equipment-card/ui/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/ui/components/EquipmentCardContent.kt
git commit -m "refactor(equipment-card): EquipmentCardField — text-only, one-card host"
```

---

## Task 7: Создать EquipmentCardLayout — карточка с accent bar + статус-бейдж

**Files:**
- Create: `shared/feature-equipment-card/ui/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/ui/components/EquipmentCardLayout.kt`
- Modify: `shared/feature-equipment-card/ui/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/ui/EquipmentCardScreen.kt`
- Modify: `shared/feature-equipment-card/ui/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/ui/components/EquipmentCardContent.kt` (заменить text-value у поля «Статус» на StatusBadge)

- [ ] **Step 7.1: Создать EquipmentCardLayout.kt с одной общей карточкой + accent bar**

```kotlin
package ru.mirea.toir.feature.equipment.card.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentResultStatus

@Composable
internal fun EquipmentCardLayout(
    status: UiEquipmentResultStatus,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = ToirTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ToirTheme.shapes.md)
            .background(colors.surface),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColorFor(status, colors)),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

private fun accentColorFor(
    status: UiEquipmentResultStatus,
    colors: ru.mirea.toir.common.ui.compose.theme.ToirColorScheme,
): Color = when (status) {
    UiEquipmentResultStatus.NOT_STARTED -> colors.textDisabled
    UiEquipmentResultStatus.IN_PROGRESS -> colors.warning
    UiEquipmentResultStatus.COMPLETED -> colors.success
    UiEquipmentResultStatus.SKIPPED -> colors.error
}
```

> **Заметка:** в спеке цвет accent bar управляется флагом `qrConfirmed`/`requiresQr`. Поскольку этих полей пока нет в data model, цвет завязан на статус (NOT_STARTED → disabled, IN_PROGRESS → warning, COMPLETED → success, SKIPPED → error). Когда появится `requiresQr` — добавить параметр `qrConfirmed: Boolean = true` и логику переопределения цвета на warning, если `requiresQr && !qrConfirmed`.

- [ ] **Step 7.2: Обновить EquipmentCardContent — заменить status-поле на StatusBadge**

Внутри `EquipmentCardContent` заменить последний `EquipmentCardField` (Status):

```kotlin
// Было:
EquipmentCardField(
    label = stringResource(MR.strings.equipment_card_status),
    value = stringResource(state.status.labelRes),
)

// Стало:
EquipmentStatusField(status = state.status)
```

И добавить новый приватный composable в этот же файл (или вынести):

```kotlin
@Composable
private fun EquipmentStatusField(status: UiEquipmentResultStatus) {
    val colors = ToirTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(MR.strings.equipment_card_status),
            style = ToirTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        StatusBadge(
            text = stringResource(status.labelRes),
            icon = painterResource(statusIconFor(status)),
            backgroundColor = statusBackgroundFor(status, colors),
            contentColor = statusContentFor(status, colors),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

private fun statusIconFor(status: UiEquipmentResultStatus): dev.icerock.moko.resources.ImageResource =
    when (status) {
        UiEquipmentResultStatus.NOT_STARTED -> MR.images.ic_clipboard
        UiEquipmentResultStatus.IN_PROGRESS -> MR.images.ic_sync_alt
        UiEquipmentResultStatus.COMPLETED -> MR.images.ic_check_circle
        UiEquipmentResultStatus.SKIPPED -> MR.images.ic_close
    }

private fun statusBackgroundFor(
    status: UiEquipmentResultStatus,
    colors: ru.mirea.toir.common.ui.compose.theme.ToirColorScheme,
): Color = when (status) {
    UiEquipmentResultStatus.NOT_STARTED -> colors.surface2
    UiEquipmentResultStatus.IN_PROGRESS -> colors.warningSubtle
    UiEquipmentResultStatus.COMPLETED -> colors.successSubtle
    UiEquipmentResultStatus.SKIPPED -> colors.errorSubtle
}

private fun statusContentFor(
    status: UiEquipmentResultStatus,
    colors: ru.mirea.toir.common.ui.compose.theme.ToirColorScheme,
): Color = when (status) {
    UiEquipmentResultStatus.NOT_STARTED -> colors.textSecondary
    UiEquipmentResultStatus.IN_PROGRESS -> colors.warning
    UiEquipmentResultStatus.COMPLETED -> colors.success
    UiEquipmentResultStatus.SKIPPED -> colors.error
}
```

Импорты добавить: `StatusBadge`, `painterResource` (moko), `Color`, `dev.icerock.moko.resources.ImageResource`.

- [ ] **Step 7.3: Использовать EquipmentCardLayout в EquipmentCardScreen**

В `EquipmentCardScreen.kt` в ветке `else -> EquipmentCardContent(...)` обернуть вызов в layout:

```kotlin
// Было:
else -> EquipmentCardContent(
    state = state,
    modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
)

// Стало:
else -> EquipmentCardLayout(
    status = state.status,
    modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
) {
    EquipmentCardContent(state = state)
}
```

И в `EquipmentCardContent` убрать `verticalArrangement` из внешнего Column (теперь его задаёт layout):

```kotlin
@Composable
internal fun EquipmentCardContent(
    state: UiEquipmentCardState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // поля идут подряд, gap задан внешним layout-ом через verticalArrangement
        ...
    }
}
```

> Важно: после изменения убедиться, что `EquipmentCardContent` больше не задаёт `verticalArrangement = Arrangement.spacedBy(12.dp)` сам (теперь это делает `EquipmentCardLayout`). Иначе будет двойной отступ.

- [ ] **Step 7.4: Заменить TextButton "←" на иконку ic_arrow_back в TopAppBar**

В `EquipmentCardScreen.kt` заменить `navigationIcon = { TextButton(onClick = ...) { Text("←", ...) } }`:

```kotlin
navigationIcon = {
    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
        androidx.compose.foundation.Image(
            painter = dev.icerock.moko.resources.compose.painterResource(MR.images.ic_arrow_back),
            contentDescription = dev.icerock.moko.resources.compose.stringResource(MR.strings.equipment_card_back_content_description),
            modifier = androidx.compose.ui.Modifier.size(24.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(ToirTheme.colors.textSecondary),
        )
    }
},
```

- [ ] **Step 7.5: Сборка**

```bash
./gradlew :shared:feature-equipment-card:ui:assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 7.6: Manual verification (Preview или эмулятор)**

Открыть `PreviewEquipmentCardScreenContent` в IDE preview:
- Карточка одна, поля без отдельных фонов.
- Слева 4dp accent bar.
- Поле «Статус» — бейдж с иконкой и цветным фоном.

- [ ] **Step 7.7: Commit**

```bash
git add shared/feature-equipment-card/ui/
git commit -m "feat(equipment-card): one-card layout with status accent bar and StatusBadge"
```

---

## Task 8: Sticky footer Equipment Card — vertical padding 16dp

**Files:**
- Modify: `shared/feature-equipment-card/ui/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/ui/components/EquipmentCardOpenChecklistButton.kt`

- [ ] **Step 8.1: Изменить vertical padding с 12dp на 16dp**

В `EquipmentCardOpenChecklistButton.kt` поменять одну строку:

```kotlin
// Было:
.padding(horizontal = 16.dp, vertical = 12.dp),

// Стало:
.padding(horizontal = 16.dp, vertical = 16.dp),
```

(Bottom safe area `Scaffold` добавляет автоматически через `bottomBar`-слот.)

- [ ] **Step 8.2: Сборка + Commit**

```bash
./gradlew :shared:feature-equipment-card:ui:assembleDebug && \
git add shared/feature-equipment-card/ui/src/commonMain/kotlin/ru/mirea/toir/feature/equipment/card/ui/components/EquipmentCardOpenChecklistButton.kt && \
git commit -m "fix(equipment-card): sticky footer vertical padding 16dp per spec"
```

---

# Phase D — Photo Capture (core)

## Task 9: Расширить PhotoCaptureStore — maxPhotos и OnPhotoDeleted (TDD)

**Files:**
- Modify: `shared/feature-photo-capture/api/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/api/store/PhotoCaptureStore.kt`
- Modify: `shared/feature-photo-capture/impl/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/impl/domain/PhotoCaptureReducer.kt`
- Modify: `shared/feature-photo-capture/impl/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/impl/domain/PhotoCaptureStoreFactory.kt`
- Create: `shared/feature-photo-capture/impl/src/commonTest/kotlin/ru/mirea/toir/feature/photo/capture/impl/domain/PhotoCaptureReducerTest.kt`

- [ ] **Step 9.1: Прочитать текущий Reducer и StoreFactory, чтобы знать сигнатуру Message**

```bash
cat shared/feature-photo-capture/impl/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/impl/domain/PhotoCaptureReducer.kt
cat shared/feature-photo-capture/impl/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/impl/domain/PhotoCaptureStoreFactory.kt
```

(Просто прочитать, ничего не менять — нужно знать, какие Message определены, чтобы добавить новые правильно.)

- [ ] **Step 9.2: Расширить PhotoCaptureStore.State**

В `PhotoCaptureStore.kt` поменять State:

```kotlin
data class State(
    val checklistItemResultId: String = "",
    val photos: List<String> = emptyList(),
    val maxPhotos: Int? = DEFAULT_MAX_PHOTOS,
    val isLoading: Boolean = false,
)

sealed interface Intent {
    data class OnPhotoTaken(val fileUri: String) : Intent
    data class OnPhotoDeleted(val fileUri: String) : Intent
    data object OnConfirm : Intent
}

companion object {
    const val DEFAULT_MAX_PHOTOS: Int = 5
}
```

- [ ] **Step 9.3: Написать failing-тест для нового поведения**

```kotlin
package ru.mirea.toir.feature.photo.capture.impl.domain

import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.State
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PhotoCaptureReducerTest {

    private val reducer = PhotoCaptureReducer()

    @Test
    fun `OnPhotoTaken adds uri to state`() {
        val initial = State(photos = listOf("a"))
        val message = PhotoCaptureStoreFactory.Message.PhotoAdded("b")

        val result = with(reducer) { initial.reduce(message) }

        assertEquals(listOf("a", "b"), result.photos)
    }

    @Test
    fun `PhotoRemoved drops the matching uri`() {
        val initial = State(photos = listOf("a", "b", "c"))
        val message = PhotoCaptureStoreFactory.Message.PhotoRemoved("b")

        val result = with(reducer) { initial.reduce(message) }

        assertEquals(listOf("a", "c"), result.photos)
    }

    @Test
    fun `PhotoRemoved with non-existing uri leaves photos unchanged`() {
        val initial = State(photos = listOf("a", "b"))
        val message = PhotoCaptureStoreFactory.Message.PhotoRemoved("nope")

        val result = with(reducer) { initial.reduce(message) }

        assertEquals(listOf("a", "b"), result.photos)
    }
}
```

> Имена сообщений (`PhotoAdded`, `PhotoRemoved`) — сверить с реальной структурой `PhotoCaptureStoreFactory.Message` после прочтения файла на Step 9.1. Если там используются другие имена (например, `Message.AddPhoto`) — переименовать в тесте и в реализации соответственно.

- [ ] **Step 9.4: Запустить тесты — фейл ожидаем**

```bash
./gradlew :shared:feature-photo-capture:impl:allTests
```

Expected: FAIL — `Message.PhotoRemoved` не существует.

- [ ] **Step 9.5: Добавить новый Message и Intent → Action mapping в Reducer и StoreFactory**

В `PhotoCaptureStoreFactory.kt` добавить в sealed interface `Message`:

```kotlin
data class PhotoRemoved(val fileUri: String) : Message
```

(Если PhotoAdded ещё не существует — добавить и его, иначе использовать существующее имя.)

В `PhotoCaptureReducer.kt` добавить ветку:

```kotlin
override fun State.reduce(message: Message): State = when (message) {
    is Message.PhotoAdded -> copy(photos = photos + message.fileUri)
    is Message.PhotoRemoved -> copy(photos = photos - message.fileUri)
    // ... остальные ветки без изменений ...
}
```

В `PhotoCaptureExecutor.kt` (или где обрабатываются Intents) добавить:

```kotlin
override suspend fun suspendExecuteIntent(intent: Intent) = when (intent) {
    is Intent.OnPhotoTaken -> dispatch(Message.PhotoAdded(intent.fileUri))
    is Intent.OnPhotoDeleted -> dispatch(Message.PhotoRemoved(intent.fileUri))
    Intent.OnConfirm -> { /* существующая логика без изменений */ }
}
```

- [ ] **Step 9.6: Запустить тесты — должны пройти**

```bash
./gradlew :shared:feature-photo-capture:impl:allTests
```

Expected: PASS.

- [ ] **Step 9.7: Commit**

```bash
git add shared/feature-photo-capture/api/ shared/feature-photo-capture/impl/
git commit -m "feat(photo-capture): support photo deletion and maxPhotos in store"
```

---

## Task 10: Прокинуть maxPhotos через Presentation слой

**Files:**
- Modify: `shared/feature-photo-capture/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/presentation/models/UiPhotoCaptureState.kt`
- Modify: `shared/feature-photo-capture/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/presentation/mappers/UiPhotoCaptureStateMapper.kt`
- Modify: `shared/feature-photo-capture/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/presentation/PhotoCaptureViewModel.kt`

- [ ] **Step 10.1: Расширить UiPhotoCaptureState**

```kotlin
@Immutable
data class UiPhotoCaptureState(
    val photos: ImmutableList<String> = persistentListOf(),
    val maxPhotos: Int? = null,
    val isLoading: Boolean = false,
)
```

- [ ] **Step 10.2: Обновить UiPhotoCaptureStateMapper**

```kotlin
override fun map(state: State): UiPhotoCaptureState = UiPhotoCaptureState(
    photos = state.photos.toImmutableList(),
    maxPhotos = state.maxPhotos,
    isLoading = state.isLoading,
)
```

- [ ] **Step 10.3: Добавить onPhotoDeleted в ViewModel**

```kotlin
fun onPhotoDeleted(fileUri: String) = store.accept(Intent.OnPhotoDeleted(fileUri))
```

- [ ] **Step 10.4: Сборка + Commit**

```bash
./gradlew :shared:feature-photo-capture:presentation:assembleDebug && \
git add shared/feature-photo-capture/presentation/ && \
git commit -m "feat(photo-capture): expose maxPhotos and onPhotoDeleted via presentation layer"
```

---

## Task 11: PhotoCaptureScreen — переписка под спеку (empty state, footer, progress в title)

**Files:**
- Create: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoCaptureEmptyState.kt`
- Create: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoCaptureFooter.kt`
- Modify: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/PhotoCaptureScreen.kt`
- Modify: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoCaptureContent.kt`
- Modify: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoCapturePhotoRow.kt`

- [ ] **Step 11.1: Создать PhotoCaptureEmptyState.kt**

```kotlin
package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@Composable
internal fun PhotoCaptureEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(MR.images.ic_camera_alt),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            colorFilter = ColorFilter.tint(ToirTheme.colors.textDisabled),
        )
        Text(
            text = stringResource(MR.strings.photo_capture_empty_title),
            style = ToirTheme.typography.bodyLarge,
            color = ToirTheme.colors.textPrimary,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = stringResource(MR.strings.photo_capture_empty_subtitle),
            style = ToirTheme.typography.bodyMedium,
            color = ToirTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
```

(Импорт `padding` забыли — добавить `import androidx.compose.foundation.layout.padding`.)

- [ ] **Step 11.2: Создать PhotoCaptureFooter.kt**

```kotlin
package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@Composable
internal fun PhotoCaptureFooter(
    canTake: Boolean,
    canConfirm: Boolean,
    isLimitReached: Boolean,
    onTakePhoto: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = ToirTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onTakePhoto,
            enabled = canTake,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.surface2,
                contentColor = colors.textPrimary,
                disabledContainerColor = colors.surface2,
                disabledContentColor = colors.textDisabled,
            ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(MR.images.ic_camera_alt),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(
                        if (canTake) colors.textPrimary else colors.textDisabled,
                    ),
                )
                Text(
                    text = stringResource(MR.strings.photo_capture_button_take),
                    style = ToirTheme.typography.label,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        if (isLimitReached) {
            Text(
                text = stringResource(MR.strings.photo_capture_limit_reached),
                style = ToirTheme.typography.caption,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Button(
            onClick = onConfirm,
            enabled = canConfirm,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.ctaPrimary,
                contentColor = colors.textOnAccent,
            ),
        ) {
            Text(
                text = stringResource(MR.strings.photo_capture_button_confirm),
                style = ToirTheme.typography.label,
            )
        }
        if (!canConfirm) {
            Text(
                text = stringResource(MR.strings.photo_capture_confirm_disabled_hint),
                style = ToirTheme.typography.caption,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
```

- [ ] **Step 11.3: Полностью переписать PhotoCaptureScreen.kt**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoCaptureScreen(
    checklistItemResultId: String,
    onPhotoConfirm: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PhotoCaptureViewModel = koinViewModel { parametersOf(checklistItemResultId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiPhotoCaptureLabel.PhotoConfirmed -> onPhotoConfirm()
        }
    }

    val cameraLauncher = rememberCameraLauncher(onPhotoTaken = viewModel::onPhotoTaken)

    Scaffold(
        containerColor = ToirTheme.colors.background,
        topBar = {
            PhotoCaptureTopBar(
                photoCount = state.photos.size,
                maxPhotos = state.maxPhotos,
                onBack = onNavigateBack,
            )
        },
        bottomBar = {
            val isLimitReached = state.maxPhotos?.let { state.photos.size >= it } ?: false
            PhotoCaptureFooter(
                canTake = !state.isLoading && !isLimitReached,
                canConfirm = state.photos.isNotEmpty(),
                isLimitReached = isLimitReached,
                onTakePhoto = cameraLauncher,
                onConfirm = viewModel::onConfirm,
            )
        },
    ) { paddingValues ->
        PhotoCaptureContent(
            photos = state.photos,
            onPhotoLongPress = viewModel::onPhotoDeleted,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoCaptureTopBar(
    photoCount: Int,
    maxPhotos: Int?,
    onBack: () -> Unit,
) {
    val title = if (maxPhotos != null) {
        stringResource(MR.strings.photo_capture_title_with_progress, photoCount, maxPhotos)
    } else {
        stringResource(MR.strings.photo_capture_title)
    }
    TopAppBar(
        title = {
            Text(text = title, style = ToirTheme.typography.headline, color = ToirTheme.colors.textPrimary)
        },
        navigationIcon = {
            androidx.compose.material3.IconButton(onClick = onBack) {
                androidx.compose.foundation.Image(
                    painter = dev.icerock.moko.resources.compose.painterResource(MR.images.ic_arrow_back),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(ToirTheme.colors.textSecondary),
                )
            }
        },
    )
}
```

> Сигнатура `PhotoCaptureScreen` изменилась — добавился `onNavigateBack`. Обновить `FeaturePhotoCaptureScreenApi.kt` соответственно (передать onNavigateBack из NavGraph).

- [ ] **Step 11.4: Переписать PhotoCaptureContent — empty state vs photo row**

```kotlin
@Composable
internal fun PhotoCaptureContent(
    photos: ImmutableList<String>,
    onPhotoLongPress: (uri: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (photos.isEmpty()) {
        PhotoCaptureEmptyState(modifier = modifier)
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            PhotoCapturePhotoRow(
                photos = photos,
                onPhotoLongPress = onPhotoLongPress,
            )
        }
    }
}
```

(Удалены кнопки `Button(onClick = onTakePhoto)` и `Button(onClick = onConfirm)` — они переехали в `PhotoCaptureFooter`.)

- [ ] **Step 11.5: Обновить PhotoCapturePhotoRow — пробросить onPhotoLongPress + поправить contentPadding**

```kotlin
@Composable
internal fun PhotoCapturePhotoRow(
    photos: ImmutableList<String>,
    onPhotoLongPress: (uri: String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(items = photos, key = { it }) { uri ->
            PhotoCapturePhotoItem(
                uri = uri,
                onLongPress = { onPhotoLongPress(uri) },
            )
        }
    }
}
```

- [ ] **Step 11.6: Сборка**

```bash
./gradlew :shared:feature-photo-capture:ui:assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 11.7: Commit**

```bash
git add shared/feature-photo-capture/ui/
git commit -m "feat(photo-capture): empty state, sticky footer, progress in title"
```

---

## Task 12: PhotoCapturePhotoItem — border + placeholder + long-press

**Files:**
- Modify: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoCapturePhotoItem.kt`

- [ ] **Step 12.1: Расширить PhotoCapturePhotoItem**

```kotlin
package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import dev.icerock.moko.resources.compose.painterResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@Composable
internal fun PhotoCapturePhotoItem(
    uri: String,
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = ToirTheme.colors
    var painterState by remember { mutableStateOf<AsyncImagePainter.State?>(null) }

    Box(
        modifier = modifier
            .size(120.dp)
            .clip(ToirTheme.shapes.md)
            .background(colors.surface2)
            .border(width = 1.dp, color = colors.borderSubtle, shape = ToirTheme.shapes.md)
            .pointerInput(uri) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.size(120.dp).clip(ToirTheme.shapes.md),
            onState = { painterState = it },
        )
        when (val s = painterState) {
            is AsyncImagePainter.State.Loading -> CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = colors.textSecondary,
            )
            is AsyncImagePainter.State.Error -> Image(
                painter = painterResource(MR.images.ic_broken_image),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(colors.textDisabled),
            )
            else -> Unit
        }
    }
}
```

- [ ] **Step 12.2: Сборка + Commit**

```bash
./gradlew :shared:feature-photo-capture:ui:assembleDebug && \
git add shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoCapturePhotoItem.kt && \
git commit -m "feat(photo-capture): photo tile with border, placeholder, error state and long-press"
```

---

## Task 13: Диалог удаления фото (long-press)

**Files:**
- Create: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoDeleteConfirmDialog.kt`
- Modify: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/PhotoCaptureScreen.kt` (добавить state для диалога)

- [ ] **Step 13.1: Создать PhotoDeleteConfirmDialog.kt**

```kotlin
package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@Composable
internal fun PhotoDeleteConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ToirTheme.colors.surface2,
        title = {
            Text(
                text = stringResource(MR.strings.photo_delete_title),
                style = ToirTheme.typography.headline,
                color = ToirTheme.colors.textPrimary,
            )
        },
        text = {
            Text(
                text = stringResource(MR.strings.photo_delete_message),
                style = ToirTheme.typography.bodyMedium,
                color = ToirTheme.colors.textSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(MR.strings.common_button_delete),
                    color = ToirTheme.colors.error,
                    style = ToirTheme.typography.label,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(MR.strings.common_button_cancel),
                    color = ToirTheme.colors.textSecondary,
                    style = ToirTheme.typography.label,
                )
            }
        },
    )
}
```

- [ ] **Step 13.2: Добавить state-машину для диалога в PhotoCaptureScreen**

В `PhotoCaptureScreen` (после `cameraLauncher`):

```kotlin
var pendingDeleteUri by remember { mutableStateOf<String?>(null) }

// внутри Scaffold content lambda — добавить рядом с PhotoCaptureContent:
pendingDeleteUri?.let { uri ->
    PhotoDeleteConfirmDialog(
        onDismiss = { pendingDeleteUri = null },
        onConfirm = {
            viewModel.onPhotoDeleted(uri)
            pendingDeleteUri = null
        },
    )
}
```

И в передаче `onPhotoLongPress`:

```kotlin
PhotoCaptureContent(
    photos = state.photos,
    onPhotoLongPress = { uri -> pendingDeleteUri = uri },
    modifier = ...,
)
```

Импорты добавить: `mutableStateOf`, `remember`, `getValue`, `setValue`.

- [ ] **Step 13.3: Сборка + Commit**

```bash
./gradlew :shared:feature-photo-capture:ui:assembleDebug && \
git add shared/feature-photo-capture/ui/ && \
git commit -m "feat(photo-capture): long-press delete with confirmation dialog"
```

---

## Task 14: Диалог выхода с несохранёнными фото

**Files:**
- Create: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoExitConfirmDialog.kt`
- Modify: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/PhotoCaptureScreen.kt`

- [ ] **Step 14.1: Создать PhotoExitConfirmDialog.kt**

Аналогично `PhotoDeleteConfirmDialog`, только с другими строками:

```kotlin
package ru.mirea.toir.feature.photo.capture.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@Composable
internal fun PhotoExitConfirmDialog(
    onContinueCapture: () -> Unit,
    onDiscard: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onContinueCapture,
        containerColor = ToirTheme.colors.surface2,
        title = {
            Text(
                text = stringResource(MR.strings.photo_exit_title),
                style = ToirTheme.typography.headline,
                color = ToirTheme.colors.textPrimary,
            )
        },
        text = {
            Text(
                text = stringResource(MR.strings.photo_exit_message),
                style = ToirTheme.typography.bodyMedium,
                color = ToirTheme.colors.textSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text(
                    text = stringResource(MR.strings.common_button_delete),
                    color = ToirTheme.colors.error,
                    style = ToirTheme.typography.label,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onContinueCapture) {
                Text(
                    text = stringResource(MR.strings.common_button_continue_capture),
                    color = ToirTheme.colors.textSecondary,
                    style = ToirTheme.typography.label,
                )
            }
        },
    )
}
```

- [ ] **Step 14.2: Подключить диалог в PhotoCaptureScreen и intercept back**

В `PhotoCaptureScreen`:

```kotlin
var showExitDialog by remember { mutableStateOf(false) }

val handleBack: () -> Unit = {
    if (state.photos.isNotEmpty()) showExitDialog = true else onNavigateBack()
}

// в TopBar:
PhotoCaptureTopBar(
    photoCount = state.photos.size,
    maxPhotos = state.maxPhotos,
    onBack = handleBack,
)

// перехват system back:
androidx.activity.compose.BackHandler(enabled = state.photos.isNotEmpty()) {
    showExitDialog = true
}

// диалог:
if (showExitDialog) {
    PhotoExitConfirmDialog(
        onContinueCapture = { showExitDialog = false },
        onDiscard = {
            showExitDialog = false
            onNavigateBack()
        },
    )
}
```

> `androidx.activity.compose.BackHandler` — это AndroidX, в KMP должен быть expect/actual либо обёртка. Проверить как в проекте сделан перехват system back в других экранах (`grep -r BackHandler shared/`). Если нет — пропустить перехват system back, ограничиться App Bar back.

- [ ] **Step 14.3: Сборка + Commit**

```bash
./gradlew :shared:feature-photo-capture:ui:assembleDebug && \
git add shared/feature-photo-capture/ui/ && \
git commit -m "feat(photo-capture): exit confirmation when there are unsaved photos"
```

---

# Phase E — Photo Capture (advanced: preview, zoom, shared transition)

## Task 15: Full-screen Preview Screen (без zoom и transition)

**Files:**
- Create: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/preview/PhotoPreviewScreen.kt`
- Modify: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/PhotoCaptureScreen.kt`

- [ ] **Step 15.1: Создать PhotoPreviewScreen.kt — базовая версия**

```kotlin
package ru.mirea.toir.feature.photo.capture.ui.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoPreviewScreen(
    photoUri: String,
    photoIndex: Int,        // 1-based
    totalCount: Int,
    onClose: () -> Unit,
) {
    Scaffold(
        containerColor = ToirTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(MR.strings.photo_capture_preview_title, photoIndex, totalCount),
                        style = ToirTheme.typography.displayMedium,
                        color = ToirTheme.colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Image(
                            painter = painterResource(MR.images.ic_close),
                            contentDescription = stringResource(MR.strings.photo_capture_preview_close_content_description),
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(ToirTheme.colors.textPrimary),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ToirTheme.colors.background,
                ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ToirTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(16.dp),
            )
        }
    }
}
```

- [ ] **Step 15.2: Подключить preview в PhotoCaptureScreen через rememberSaveable state**

В `PhotoCaptureScreen`:

```kotlin
var previewUri by remember { mutableStateOf<String?>(null) }

// при передаче onPhotoTap в PhotoCaptureContent (нужно добавить параметр):
PhotoCaptureContent(
    photos = state.photos,
    onPhotoTap = { uri -> previewUri = uri },
    onPhotoLongPress = { uri -> pendingDeleteUri = uri },
    modifier = ...,
)

// показ preview:
previewUri?.let { uri ->
    val index = state.photos.indexOf(uri).takeIf { it >= 0 } ?: 0
    PhotoPreviewScreen(
        photoUri = uri,
        photoIndex = index + 1,
        totalCount = state.photos.size,
        onClose = { previewUri = null },
    )
}
```

В `PhotoCaptureContent` добавить `onPhotoTap: (uri: String) -> Unit` параметр и пробросить в row.
В `PhotoCapturePhotoRow` пробросить в `PhotoCapturePhotoItem(onTap = { onPhotoTap(uri) }, ...)`.

- [ ] **Step 15.3: Сборка + Commit**

```bash
./gradlew :shared:feature-photo-capture:ui:assembleDebug && \
git add shared/feature-photo-capture/ui/ && \
git commit -m "feat(photo-capture): full-screen preview on tap"
```

---

## Task 16: Pinch-to-zoom в Preview

**Files:**
- Modify: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/preview/PhotoPreviewScreen.kt`

- [ ] **Step 16.1: Заменить статичный AsyncImage на zoomable с transformable + double-tap**

```kotlin
@Composable
private fun ZoomableImage(
    photoUri: String,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = scale,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
        label = "zoom",
    )

    val transformableState = androidx.compose.foundation.gestures.rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(MIN_SCALE, MAX_SCALE)
        offset = if (scale > 1f) offset + panChange else androidx.compose.ui.geometry.Offset.Zero
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                androidx.compose.foundation.gestures.detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else DOUBLE_TAP_SCALE
                        if (scale == 1f) offset = androidx.compose.ui.geometry.Offset.Zero
                    },
                )
            }
            .androidx.compose.foundation.gestures.transformable(state = transformableState),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = photoUri,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .androidx.compose.ui.graphics.graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 4f
private const val DOUBLE_TAP_SCALE = 2.5f
```

И в `PhotoPreviewScreen` заменить `AsyncImage(...)` на `ZoomableImage(photoUri = photoUri, modifier = Modifier.fillMaxSize().padding(paddingValues))`.

> Важно: импорты `transformable`, `rememberTransformableState`, `mutableFloatStateOf`, `Offset` нужно добавить в правильной форме (без вкладывания в полные пути в коде — выше показано для наглядности).

- [ ] **Step 16.2: Поддержать reduced-motion**

Не делать sensitive change, добавить проверку через `LocalAccessibilityManager` (KMP):

> Compose KMP не имеет прямого `prefers-reduced-motion` API. Минимально — оставить animation как есть. Если у проекта есть accessor для AccessibilityInfo (искать в `shared/common-ui/`), использовать его, иначе — TODO note в коде с подсказкой добавить позже.

- [ ] **Step 16.3: Сборка + Commit**

```bash
./gradlew :shared:feature-photo-capture:ui:assembleDebug && \
git add shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/preview/PhotoPreviewScreen.kt && \
git commit -m "feat(photo-capture): pinch-to-zoom and double-tap zoom in preview"
```

---

## Task 17: Shared Element Transition между тайлом и preview

**Files:**
- Modify: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/PhotoCaptureScreen.kt` (обернуть в `SharedTransitionLayout`)
- Modify: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/components/PhotoCapturePhotoItem.kt` (применить `sharedElement`)
- Modify: `shared/feature-photo-capture/ui/src/commonMain/kotlin/ru/mirea/toir/feature/photo/capture/ui/preview/PhotoPreviewScreen.kt` (применить `sharedElement`)

> **Перед началом:** проверить версию Compose Multiplatform в `gradle/libs.versions.toml`. Должна быть ≥ 1.7.0 для `SharedTransitionLayout`. Если ниже — пропустить эту таску, оставить TODO в коде.

- [ ] **Step 17.1: Обернуть структуру в SharedTransitionLayout**

В `PhotoCaptureScreen` обернуть Scaffold + previewUri lambda:

```kotlin
@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
internal fun PhotoCaptureScreen(...) {
    // ... state, viewModel, effects ...

    androidx.compose.animation.SharedTransitionLayout {
        androidx.compose.animation.AnimatedContent(
            targetState = previewUri,
            transitionSpec = {
                androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(250),
                ) togetherWith androidx.compose.animation.fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(250),
                )
            },
            label = "photo-preview-content",
        ) { currentPreview ->
            if (currentPreview == null) {
                Scaffold(/* ... как раньше ... */) { paddingValues ->
                    PhotoCaptureContent(
                        photos = state.photos,
                        onPhotoTap = { uri -> previewUri = uri },
                        onPhotoLongPress = { uri -> pendingDeleteUri = uri },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent,
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                    )
                }
            } else {
                val index = state.photos.indexOf(currentPreview).takeIf { it >= 0 } ?: 0
                PhotoPreviewScreen(
                    photoUri = currentPreview,
                    photoIndex = index + 1,
                    totalCount = state.photos.size,
                    onClose = { previewUri = null },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent,
                )
            }
        }
    }
}
```

- [ ] **Step 17.2: Пробросить scope-параметры в Content/Row/Item и применить sharedElement**

В `PhotoCapturePhotoItem` добавить scope-параметры и применить modifier:

```kotlin
@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
internal fun PhotoCapturePhotoItem(
    uri: String,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    with(sharedTransitionScope) {
        Box(
            modifier = modifier
                .size(120.dp)
                .clip(ToirTheme.shapes.md)
                // ... существующие модификаторы ...
                .sharedElement(
                    state = rememberSharedContentState(key = "photo-$uri"),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            // ...
        )
    }
}
```

В `PhotoPreviewScreen` применить тот же `sharedElement` к `AsyncImage`/`ZoomableImage` с тем же ключом `"photo-$uri"`.

- [ ] **Step 17.3: Сборка**

```bash
./gradlew :shared:feature-photo-capture:ui:assembleDebug
```

Expected: BUILD SUCCESSFUL. Если падает на отсутствии `SharedTransitionLayout` — версия Compose MP < 1.7. Откатить таску.

- [ ] **Step 17.4: Manual verification**

Запустить на устройстве, открыть PhotoCaptureScreen с несколькими фото, тапнуть на тайл — фото должно «вырастать» из позиции тайла в полноэкранный preview, при закрытии — schлопываться обратно.

- [ ] **Step 17.5: Commit**

```bash
git add shared/feature-photo-capture/ui/
git commit -m "feat(photo-capture): shared element transition between tile and preview"
```

---

# Финальная проверка

## Task 18: Полная сборка, тесты, ручная проверка

- [ ] **Step 18.1: Полная сборка debug**

```bash
./gradlew :android:app:assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 18.2: Все тесты**

```bash
./gradlew :shared:feature-bootstrap:impl:allTests \
          :shared:feature-photo-capture:impl:allTests \
          :shared:feature-equipment-card:impl:allTests
```

Expected: PASS все.

- [ ] **Step 18.3: Detekt**

```bash
./gradlew detekt
```

Expected: SUCCESS (или существующие нарушения только в файлах, которые не трогали в этом плане).

- [ ] **Step 18.4: Ручная проверка трёх экранов на эмуляторе**

Bootstrap:
1. Запуск с валидным токеном и сетью → видна шапка TOIR + спиннер + «Подготовка...» → переход на routes list.
2. Запуск с валидным токеном и без сети → шапка + cloud_off + текст ошибки + «Повторить». Включить сеть → тап «Повторить» → переход.
3. (Если можно) Запуск с протухшим токеном → шапка + спиннер → автомат-переход на Login (без error UI).

Equipment Card:
1. Открыть точку из routes-list → точки → equipment card.
2. Видна одна общая карточка с accent bar слева (цвет соответствует статусу).
3. Поле «Статус» — бейдж с иконкой и фоном.
4. Если `locationName` пуст — поле скрыто.
5. В sticky footer — кнопка «Открыть чек-лист» с padding 16dp снизу.

Photo Capture:
1. Открыть из чек-листа.
2. Empty state: иконка камеры + «Нет фотографий».
3. Снять фото → появляется тайл с border, AsyncImage загружает.
4. Заголовок App Bar — «Фотофиксация · 1 / 5».
5. Long-press на тайл → диалог удаления.
6. Tap на тайл → full-screen preview с zoom (pinch + double-tap).
7. При наличии фото и тапе «назад» → диалог выхода.
8. После 5 фото — «Снять фото» disabled, hint «Достигнут лимит фотографий».
9. Тап «Подтвердить» → возврат на чек-лист.

- [ ] **Step 18.5: Final commit (если что-то поправили в Step 18)**

```bash
git status
# если есть изменения — git add ... && git commit -m "fix: address issues found in manual verification"
```

---

# Self-Review Notes

После написания плана я проверил:

**Spec coverage:**
- `pages/bootstrap.md` — Task 3-5 (полностью покрывают спеку, включая 401-handler).
- `pages/equipment-card.md` — Task 6-8 (одна карточка, accent bar, status badge, sticky footer 16dp). QR-секции сознательно опущены — implemented-as-conditional, нужно появление `requiresQr` в data model.
- `pages/photo-capture.md` — Task 9-17 (вся спека: max photos, empty state, footer, long-press delete, exit confirm, preview, pinch-to-zoom, shared transition).

**Placeholder scan:** В нескольких местах есть оговорки «проверить актуальную сигнатуру в коде», «если в проекте уже есть тестовая инфра» — эти TODO **намеренные**, потому что я не могу гарантировать имена `Message.PhotoAdded` без чтения файла; исполнитель должен сделать это сам на месте. Это honest unknowns, а не lazy plan.

**Type consistency:**
- `BootstrapResult` определён в Task 3, используется в Task 4 — типы согласованы.
- `PhotoCaptureStore.Intent.OnPhotoDeleted` определён в Task 9, прокинут через ViewModel в Task 10, используется UI в Task 11+13 — согласовано.
- `StatusBadge(text, icon, backgroundColor, contentColor)` определён в Task 2, используется в Task 7 с теми же аргументами — согласовано.

**Scope:** План большой (18 задач), но каждая фаза (A/B/C/D/E) самодостаточна. Если в процессе появится бюджетный pressure — Phase E (advanced photo features) можно вырезать в отдельный план.
