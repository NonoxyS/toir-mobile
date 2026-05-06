# Checklist Input Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Заменить `Switch` в Boolean-полях чеклиста на tristate `ToirSegmentedControl` («Да | Нет», нейтральные цвета, повторный клик снимает выбор → `null`); заменить кнопку «Выполнено» в Confirm-полях на `ToirToggleChip`, который можно отжать; подсвечивать каждое незаполненное required-поле красным после неудачной попытки финиша.

**Architecture:** Создаём два новых DS-компонента в `common-ui/.../shared/segmented/` и `.../shared/chip/`. Расширяем контракт стора: `Intent.OnBooleanAnswer(Boolean?)` и `Intent.OnConfirm(Boolean)`. Репозиторий: `saveBooleanAnswer(Boolean?)` пишет `value_boolean = NULL` при `null`; `saveConfirm(Boolean)` пишет `1L` при `true` и `null` при `false` (отжатие). Подсветка required-полей — через производный флаг `showValidationError` в `UiChecklistItem`, вычисляемый маппером из `state.isValidationError + isRequired + isAnswered`. Маппер сам считает «is answered» по типу — domain не меняется. Тесты: unit на `UiChecklistStateMapper` (поведение `showValidationError`), плюс расширение `ChecklistReducerTest` уже не нужно (Reducer не меняется). UI верифицируем через `@Preview`.

**Tech Stack:** Compose Multiplatform 1.10.x, Material3 (`SingleChoiceSegmentedButtonRow`, `FilterChip`), MVIKotlin, moko-resources, `ToirTheme`, kotlinx-immutable-collections, `kotlin.test`.

---

## Scope Notes

- Никаких изменений в БД-схеме (`value_boolean INTEGER` уже nullable — `core-database/.../ChecklistItemResult.sq:7`).
- Никаких изменений в sync DTO (`RemoteSyncChecklistItemResult.valueBoolean: Boolean?` уже nullable — `RemoteSyncPushRequest.kt:51`).
- Никаких изменений в OpenAPI-контракте (push принимает любые upserts по id; backend-side ограничения по `inspectionEquipmentResult.status = completed` нас не касаются — мы редактируем только до финиша).
- Цвета segmented control «Да/Нет» — нейтральные оба (`surfaceVariant` для невыбранного, `primary`/`textPrimary` для выбранного), без красно-зелёной семантики.
- Подсветка required-полей включается только после `OnFinishChecklist` (когда `state.isValidationError = true`); при ответе на поле `reloadItems()` пересчитывает items, и маппер выключает подсветку для уже отвеченного поля автоматически.
- DI / навигация / Koin-модули не трогаем.
- Light theme не делаем (`ToirColorScheme` остаётся dark-only — см. предыдущий plan `2026-05-06-design-system-alignment.md` §Scope).
- Compatibility: эта работа поверх `feature/screens-spec-alignment`. Перед стартом — проверить, что ветка up-to-date с `main` и нет конфликтов.

---

## File Structure

### Создаются

| Path | Ответственность |
|---|---|
| `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/components/shared/segmented/ToirSegmentedControl.kt` | Generic tristate segmented control: `options`, `selected: T?`, повторный клик по выбранному → `onSelectedChange(null)`; визуально — обёртка над `SingleChoiceSegmentedButtonRow` с цветами `ToirTheme` |
| `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/components/shared/chip/ToirToggleChip.kt` | Toggle-чип на базе `FilterChip` (filled when `selected`, outlined when not), цвета `ToirTheme`, `onSelectedChange: (Boolean) -> Unit`; поддерживает `isError` для красной обводки |
| `shared/feature-checklist/presentation/src/commonTest/kotlin/ru/mirea/toir/feature/checklist/presentation/mappers/UiChecklistStateMapperTest.kt` | Unit-тесты на маппер: проверка вычисления `showValidationError` для каждого типа answerType при `isValidationError = true/false`, для required и optional items |

### Модифицируются

| Path | Изменение |
|---|---|
| `shared/feature-checklist/api/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/api/store/ChecklistStore.kt` | `Intent.OnBooleanAnswer.value: Boolean?` (было `Boolean`); `Intent.OnConfirm` получает поле `value: Boolean` |
| `shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/domain/repository/ChecklistRepository.kt` | `saveBooleanAnswer(value: Boolean?)`; `saveConfirm(value: Boolean)` |
| `shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/data/repository/ChecklistRepositoryImpl.kt` | Реализация: для Boolean — `value` → `null/0L/1L`; для Confirm — `true → 1L, false → null` |
| `shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/domain/ChecklistExecutor.kt` | Передача нового `value` из `Intent.OnBooleanAnswer` и `Intent.OnConfirm` в репозиторий |
| `shared/feature-checklist/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/presentation/models/UiChecklistItem.kt` | Новое поле `val showValidationError: Boolean` |
| `shared/feature-checklist/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/presentation/mappers/UiChecklistStateMapper.kt` | Маппер принимает `state.isValidationError`, вычисляет `showValidationError` для каждого item (`isValidationError && isRequired && !isAnswered()`) |
| `shared/feature-checklist/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/presentation/ChecklistViewModel.kt` | `onBooleanAnswer(itemId, value: Boolean?)`; `onConfirm(itemId, value: Boolean)` |
| `shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/ChecklistScreen.kt` | Сигнатуры коллбеков: `onBooleanAnswer: (String, Boolean?) -> Unit`, `onConfirm: (String, Boolean) -> Unit` пробрасываются до соответствующих item composable |
| `shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/BooleanChecklistItem.kt` | `Switch` → `ToirSegmentedControl` с `options = [true, false]`, `optionLabel = stringResource(yes/no)`; `onValueChange: (Boolean?) -> Unit`; обводка красная при `item.showValidationError` |
| `shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/ConfirmChecklistItem.kt` | `ToirPrimaryButton` → `ToirToggleChip`; `selected = item.isConfirmed`; `onSelectedChange: (Boolean) -> Unit`; `isError = item.showValidationError` |
| `shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/NumberChecklistItem.kt` | `isError = isOutOfRange \|\| item.showValidationError` |
| `shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/TextChecklistItem.kt` | `isError = item.showValidationError` |
| `shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/SelectChecklistItem.kt` | Когда `item.showValidationError`: подпись `titleWithRequiredMarker` красным (`ToirTheme.colors.error`) |
| `shared/common-resources/src/commonMain/moko-resources/base/strings.xml` | Новые: `checklist_button_yes`, `checklist_button_no`, `checklist_button_done` (заменяет `checklist_button_confirm`) |

### Удаляется

| Path | Причина |
|---|---|
| Строка `<string name="checklist_button_confirm">Выполнено</string>` в `strings.xml` | Заменена на `checklist_button_done` (новое имя точнее: тогл, а не подтверждение) |

---

## Phases

- **Phase 1 — Common UI компоненты** (T1–T2): новые wrapper'ы.
- **Phase 2 — Domain/Data контракт** (T3–T4): репозиторий принимает новые типы.
- **Phase 3 — Store + Executor + ViewModel** (T5–T7): расширяем Intent'ы.
- **Phase 4 — Presentation: showValidationError** (T8–T9): маппер + тесты + UI-модель.
- **Phase 5 — UI items** (T10–T13): новые composable + подсветка required.
- **Phase 6 — Resources + Wiring + Verification** (T14–T16): строки + ChecklistScreen + сборка.

---

## Tasks

### Task 1: ToirSegmentedControl (новый компонент)

**Files:**
- Create: `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/components/shared/segmented/ToirSegmentedControl.kt`

- [ ] **Step 1: Создать файл с реализацией**

```kotlin
package ru.mirea.toir.common.ui.compose.components.shared.segmented

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.theme.ToirTheme

/**
 * Tristate segmented control. Click on already-selected option → onSelectedChange(null).
 *
 * Uses ToirTheme tokens via SegmentedButtonDefaults.colors(...). Border turns red when isError.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ToirSegmentedControl(
    options: ImmutableList<T>,
    selected: T?,
    onSelectedChange: (T?) -> Unit,
    optionLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    val borderColor = if (isError) ToirTheme.colors.error else ToirTheme.colors.border
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            val isSelected = selected == option
            SegmentedButton(
                selected = isSelected,
                onClick = {
                    onSelectedChange(if (isSelected) null else option)
                },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                enabled = enabled,
                border = SegmentedButtonDefaults.borderStroke(borderColor),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = ToirTheme.colors.surface2,
                    activeContentColor = ToirTheme.colors.textPrimary,
                    activeBorderColor = borderColor,
                    inactiveContainerColor = Color.Transparent,
                    inactiveContentColor = ToirTheme.colors.textSecondary,
                    inactiveBorderColor = borderColor,
                    disabledActiveContainerColor = ToirTheme.colors.surface,
                    disabledActiveContentColor = ToirTheme.colors.textDisabled,
                    disabledInactiveContainerColor = Color.Transparent,
                    disabledInactiveContentColor = ToirTheme.colors.textDisabled,
                ),
                icon = {},
            ) {
                Text(
                    text = optionLabel(option),
                    style = ToirTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewToirSegmentedControlNoneSelected() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSegmentedControl(
                options = persistentListOf(true, false),
                selected = null,
                onSelectedChange = {},
                optionLabel = { value -> if (value) "Да" else "Нет" },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirSegmentedControlYesSelected() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSegmentedControl(
                options = persistentListOf(true, false),
                selected = true,
                onSelectedChange = {},
                optionLabel = { value -> if (value) "Да" else "Нет" },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirSegmentedControlError() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirSegmentedControl(
                options = persistentListOf(true, false),
                selected = null,
                onSelectedChange = {},
                optionLabel = { value -> if (value) "Да" else "Нет" },
                isError = true,
            )
        }
    }
}
```

- [ ] **Step 2: Запустить detekt и сборку common-ui**

Run: `./gradlew :shared:common-ui:detekt :shared:common-ui:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/components/shared/segmented/ToirSegmentedControl.kt
git commit -m "feat(common-ui): add ToirSegmentedControl with tristate selection"
```

---

### Task 2: ToirToggleChip (новый компонент)

**Files:**
- Create: `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/components/shared/chip/ToirToggleChip.kt`

- [ ] **Step 1: Создать файл с реализацией**

```kotlin
package ru.mirea.toir.common.ui.compose.components.shared.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

/**
 * Toggle chip for "done / acknowledge" semantics. Filled when selected, outlined otherwise.
 * Click toggles state via onSelectedChange. Border turns red when isError.
 */
@Composable
fun ToirToggleChip(
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    val borderColor = when {
        isError -> ToirTheme.colors.error
        selected -> ToirTheme.colors.success
        else -> ToirTheme.colors.border
    }
    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = {
            Text(
                text = text,
                style = ToirTheme.typography.bodyLarge,
            )
        },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = if (selected) {
            {
                Icon(
                    painter = painterResource(MR.images.ic_check),
                    contentDescription = null,
                    tint = ToirTheme.colors.success,
                )
            }
        } else {
            null
        },
        border = BorderStroke(width = 1.dp, color = borderColor),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = ToirTheme.colors.surface2,
            labelColor = ToirTheme.colors.textPrimary,
            selectedContainerColor = ToirTheme.colors.surface2,
            selectedLabelColor = ToirTheme.colors.textPrimary,
            disabledContainerColor = ToirTheme.colors.surface,
            disabledLabelColor = ToirTheme.colors.textDisabled,
        ),
    )
}

@Preview
@Composable
private fun PreviewToirToggleChipUnselected() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirToggleChip(
                selected = false,
                onSelectedChange = {},
                text = "Выполнено",
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirToggleChipSelected() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirToggleChip(
                selected = true,
                onSelectedChange = {},
                text = "Выполнено",
            )
        }
    }
}

@Preview
@Composable
private fun PreviewToirToggleChipError() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirToggleChip(
                selected = false,
                onSelectedChange = {},
                text = "Выполнено",
                isError = true,
            )
        }
    }
}
```

- [ ] **Step 2: Проверить, есть ли иконка `ic_check` в resources**

Run: `find shared/common-resources/src/commonMain/moko-resources -name "ic_check*"`
Expected: непустой результат. Если иконка отсутствует — вместо `leadingIcon` использовать символ `"✓"` через `Text` или вообще убрать иконку. В этом случае удалить `painterResource` import и блок `leadingIcon`, оставив только `label`.

- [ ] **Step 3: Detekt + сборка**

Run: `./gradlew :shared:common-ui:detekt :shared:common-ui:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/components/shared/chip/ToirToggleChip.kt
git commit -m "feat(common-ui): add ToirToggleChip for acknowledgement semantics"
```

---

### Task 3: ChecklistRepository contract — saveBooleanAnswer(Boolean?), saveConfirm(Boolean)

**Files:**
- Modify: `shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/domain/repository/ChecklistRepository.kt`

- [ ] **Step 1: Расширить контракт**

Заменить блок `saveBooleanAnswer` на:

```kotlin
    suspend fun saveBooleanAnswer(
        equipmentResultId: String,
        itemId: String,
        value: Boolean?,
    ): Result<Unit>
```

Заменить `saveConfirm` на:

```kotlin
    suspend fun saveConfirm(
        equipmentResultId: String,
        itemId: String,
        value: Boolean,
    ): Result<Unit>
```

- [ ] **Step 2: Сборка модуля**

Run: `./gradlew :shared:feature-checklist:impl:compileDebugKotlinAndroid`
Expected: FAIL — `ChecklistRepositoryImpl` и `ChecklistExecutor` не реализуют новый контракт. Это ожидаемо, фиксим в следующих task'ах.

(Не коммитим — контракт зависит от реализации, коммитим в Task 4 одним атомарным изменением.)

---

### Task 4: ChecklistRepositoryImpl — реализация tristate Boolean и Confirm с отжатием

**Files:**
- Modify: `shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/data/repository/ChecklistRepositoryImpl.kt:71-93,121-127`

- [ ] **Step 1: Заменить `saveBooleanAnswer`**

Старый блок (строки ~71-79):

```kotlin
    override suspend fun saveBooleanAnswer(
        equipmentResultId: String,
        itemId: String,
        value: Boolean,
    ): Result<Unit> = saveAnswer(
        equipmentResultId = equipmentResultId,
        itemId = itemId,
        valueBoolean = if (value) 1L else 0L,
    )
```

Новый:

```kotlin
    override suspend fun saveBooleanAnswer(
        equipmentResultId: String,
        itemId: String,
        value: Boolean?,
    ): Result<Unit> = saveAnswer(
        equipmentResultId = equipmentResultId,
        itemId = itemId,
        valueBoolean = value?.let { if (it) 1L else 0L },
    )
```

- [ ] **Step 2: Заменить `saveConfirm`**

Старый блок (строки ~121-127):

```kotlin
    override suspend fun saveConfirm(equipmentResultId: String, itemId: String): Result<Unit> =
        saveAnswer(
            equipmentResultId = equipmentResultId,
            itemId = itemId,
            valueBoolean = 1L,
        )
```

Новый:

```kotlin
    override suspend fun saveConfirm(
        equipmentResultId: String,
        itemId: String,
        value: Boolean,
    ): Result<Unit> = saveAnswer(
        equipmentResultId = equipmentResultId,
        itemId = itemId,
        valueBoolean = if (value) 1L else null,
    )
```

- [ ] **Step 3: Сборка модуля**

Run: `./gradlew :shared:feature-checklist:impl:compileDebugKotlinAndroid`
Expected: FAIL — `ChecklistExecutor` всё ещё вызывает методы по старой сигнатуре. Это ожидаемо, фиксим в Task 6.

(Коммитим Task 3 + Task 4 после Task 6, чтобы атомарно обновить весь impl-модуль.)

---

### Task 5: ChecklistStore Intent — расширить контракт OnBooleanAnswer и OnConfirm

**Files:**
- Modify: `shared/feature-checklist/api/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/api/store/ChecklistStore.kt:24-29`

- [ ] **Step 1: Заменить два Intent'а**

Старый блок:

```kotlin
        data class OnBooleanAnswer(val itemId: String, val value: Boolean) : Intent
        data class OnNumberAnswer(val itemId: String, val value: String) : Intent
        data class OnTextAnswer(val itemId: String, val value: String) : Intent
        data class OnSelectAnswer(val itemId: String, val value: String) : Intent
        data class OnConfirm(val itemId: String) : Intent
```

Новый:

```kotlin
        data class OnBooleanAnswer(val itemId: String, val value: Boolean?) : Intent
        data class OnNumberAnswer(val itemId: String, val value: String) : Intent
        data class OnTextAnswer(val itemId: String, val value: String) : Intent
        data class OnSelectAnswer(val itemId: String, val value: String) : Intent
        data class OnConfirm(val itemId: String, val value: Boolean) : Intent
```

- [ ] **Step 2: Сборка api-модуля**

Run: `./gradlew :shared:feature-checklist:api:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL (api-модуль не зависит от Executor/ViewModel).

(Не коммитим отдельно — атомарный коммит после Task 6.)

---

### Task 6: ChecklistExecutor — пробросить новые value в репозиторий

**Files:**
- Modify: `shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/domain/ChecklistExecutor.kt:30-58`

- [ ] **Step 1: Изменить ветку OnBooleanAnswer**

Старый блок:

```kotlin
            is Intent.OnBooleanAnswer -> {
                repository.saveBooleanAnswer(state().equipmentResultId, intent.itemId, intent.value)
                reloadItems()
            }
```

Новый — без изменений в логике, но компилятор уже знает что `intent.value: Boolean?`:

```kotlin
            is Intent.OnBooleanAnswer -> {
                repository.saveBooleanAnswer(state().equipmentResultId, intent.itemId, intent.value)
                reloadItems()
            }
```

(Шаг тривиален — но нужен для проверки, что новый тип проходит компиляцию.)

- [ ] **Step 2: Изменить ветку OnConfirm**

Старый блок:

```kotlin
            is Intent.OnConfirm -> {
                repository.saveConfirm(state().equipmentResultId, intent.itemId)
                reloadItems()
            }
```

Новый:

```kotlin
            is Intent.OnConfirm -> {
                repository.saveConfirm(state().equipmentResultId, intent.itemId, intent.value)
                reloadItems()
            }
```

- [ ] **Step 3: Сборка impl-модуля**

Run: `./gradlew :shared:feature-checklist:impl:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Прогнать существующие reducer-тесты**

Run: `./gradlew :shared:feature-checklist:impl:allTests`
Expected: BUILD SUCCESSFUL — `ChecklistReducerTest` ничего не использует от Intent (только Message), должен пройти.

- [ ] **Step 5: Commit (Tasks 3+4+5+6 одним коммитом — атомарное расширение контракта)**

```bash
git add shared/feature-checklist/api/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/api/store/ChecklistStore.kt \
        shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/domain/repository/ChecklistRepository.kt \
        shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/data/repository/ChecklistRepositoryImpl.kt \
        shared/feature-checklist/impl/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/impl/domain/ChecklistExecutor.kt
git commit -m "feat(checklist): extend store contract with tristate Boolean and toggleable Confirm"
```

---

### Task 7: ChecklistViewModel — расширить onBooleanAnswer и onConfirm

**Files:**
- Modify: `shared/feature-checklist/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/presentation/ChecklistViewModel.kt:25-39`

- [ ] **Step 1: Заменить два метода**

Старый:

```kotlin
    fun onBooleanAnswer(itemId: String, value: Boolean) =
        store.accept(Intent.OnBooleanAnswer(itemId, value))
```

Новый:

```kotlin
    fun onBooleanAnswer(itemId: String, value: Boolean?) =
        store.accept(Intent.OnBooleanAnswer(itemId, value))
```

Старый:

```kotlin
    fun onConfirm(itemId: String) = store.accept(Intent.OnConfirm(itemId))
```

Новый:

```kotlin
    fun onConfirm(itemId: String, value: Boolean) = store.accept(Intent.OnConfirm(itemId, value))
```

- [ ] **Step 2: Сборка presentation-модуля**

Run: `./gradlew :shared:feature-checklist:presentation:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

(Не коммитим — атомарный коммит presentation+ui в следующих task'ах.)

---

### Task 8: UiChecklistItem — добавить showValidationError

**Files:**
- Modify: `shared/feature-checklist/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/presentation/models/UiChecklistItem.kt:14-30`

- [ ] **Step 1: Добавить поле в data class**

Заменить блок `data class UiChecklistItem`:

```kotlin
@Immutable
data class UiChecklistItem(
    val id: String,
    val title: String,
    val description: String?,
    val answerType: UiAnswerType,
    val isRequired: Boolean,
    val requiresPhoto: Boolean,
    val resultId: String?,
    val valueBoolean: Boolean?,
    val valueNumber: String,
    val valueText: String,
    val valueSelect: String?,
    val isConfirmed: Boolean,
    val photoCount: Int,
    val numericMin: String?,
    val numericMax: String?,
    val showValidationError: Boolean,
)
```

- [ ] **Step 2: Сборка presentation-модуля**

Run: `./gradlew :shared:feature-checklist:presentation:compileDebugKotlinAndroid`
Expected: FAIL — `UiChecklistStateMapperImpl` не передаёт `showValidationError`. Это ожидаемо, фиксим в Task 9.

---

### Task 9: UiChecklistStateMapper — TDD на showValidationError

**Files:**
- Create: `shared/feature-checklist/presentation/src/commonTest/kotlin/ru/mirea/toir/feature/checklist/presentation/mappers/UiChecklistStateMapperTest.kt`
- Modify: `shared/feature-checklist/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/presentation/mappers/UiChecklistStateMapper.kt`

- [ ] **Step 1: Написать failing test**

```kotlin
package ru.mirea.toir.feature.checklist.presentation.mappers

import kotlinx.collections.immutable.persistentListOf
import ru.mirea.toir.feature.checklist.api.models.DomainAnswerType
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UiChecklistStateMapperTest {

    private val mapper = UiChecklistStateMapperImpl()

    private fun item(
        id: String = "i1",
        type: DomainAnswerType = DomainAnswerType.Boolean,
        isRequired: Boolean = true,
        valueBoolean: Boolean? = null,
        valueNumber: Double? = null,
        valueText: String? = null,
        valueSelect: String? = null,
        isConfirmed: Boolean = false,
    ) = DomainChecklistItem(
        id = id,
        title = "Q",
        description = null,
        answerType = type,
        isRequired = isRequired,
        requiresPhoto = false,
        resultId = null,
        valueBoolean = valueBoolean,
        valueNumber = valueNumber,
        valueText = valueText,
        valueSelect = valueSelect,
        isConfirmed = isConfirmed,
        photoCount = 0,
        numericMin = null,
        numericMax = null,
    )

    @Test
    fun `showValidationError is false when isValidationError is false`() {
        val state = ChecklistStore.State(
            isValidationError = false,
            items = persistentListOf(item(isRequired = true, valueBoolean = null)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError is true for required Boolean with null value`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(isRequired = true, valueBoolean = null)),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError is false for required Boolean with non-null value`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(isRequired = true, valueBoolean = false)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError is false for optional unanswered item`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(isRequired = false, valueBoolean = null)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError true for required Number without value`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(type = DomainAnswerType.Number, valueNumber = null)),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError false for required Number with value`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(type = DomainAnswerType.Number, valueNumber = 42.0)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError true for required Text with blank value`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(type = DomainAnswerType.Text, valueText = "   ")),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError true for required Select with null option`() {
        val select = DomainAnswerType.Select(persistentListOf("a", "b"))
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(type = select, valueSelect = null)),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError true for required Confirm not confirmed`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(type = DomainAnswerType.Confirm, isConfirmed = false)),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError false for required Confirm when confirmed`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(type = DomainAnswerType.Confirm, isConfirmed = true)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `mapper preserves item count`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(id = "i1"), item(id = "i2")),
        )
        val ui = mapper.map(state)
        assertEquals(2, ui.items.size)
    }
}
```

- [ ] **Step 2: Запустить тесты — должны падать на отсутствии showValidationError в маппере**

Run: `./gradlew :shared:feature-checklist:presentation:allTests`
Expected: FAIL — компиляция теста или падающий ассерт `assertTrue(showValidationError)` (сейчас всегда `false`).

- [ ] **Step 3: Реализовать showValidationError в маппере**

Заменить блок `internal class UiChecklistStateMapperImpl`:

```kotlin
internal class UiChecklistStateMapperImpl : UiChecklistStateMapper {

    override fun map(item: ChecklistStore.State): UiChecklistState = UiChecklistState(
        items = item.items
            .map { it.toUi(showValidationErrors = item.isValidationError) }
            .toImmutableList(),
        isLoading = item.isLoading,
        isError = item.isError,
        isValidationError = item.isValidationError,
        isPhotoValidationError = item.isPhotoValidationError,
        isCompleted = item.isCompleted,
    )

    private fun DomainChecklistItem.toUi(showValidationErrors: Boolean): UiChecklistItem {
        val showValidationError = showValidationErrors && isRequired && !isAnswered()
        return UiChecklistItem(
            id = id,
            title = title,
            description = description,
            answerType = answerType.toUi(),
            isRequired = isRequired,
            requiresPhoto = requiresPhoto,
            resultId = resultId,
            valueBoolean = valueBoolean,
            valueNumber = valueNumber?.formatNumber().orEmpty(),
            valueText = valueText.orEmpty(),
            valueSelect = valueSelect,
            isConfirmed = isConfirmed,
            photoCount = photoCount,
            numericMin = numericMin?.formatNumber(),
            numericMax = numericMax?.formatNumber(),
            showValidationError = showValidationError,
        )
    }

    private fun DomainChecklistItem.isAnswered(): Boolean = when (answerType) {
        DomainAnswerType.Boolean -> valueBoolean != null
        DomainAnswerType.Number -> valueNumber != null
        DomainAnswerType.Text -> !valueText.isNullOrBlank()
        is DomainAnswerType.Select -> !valueSelect.isNullOrBlank()
        DomainAnswerType.Confirm -> isConfirmed
    }

    private fun DomainAnswerType.toUi(): UiAnswerType = when (this) {
        DomainAnswerType.Boolean -> UiAnswerType.Boolean
        DomainAnswerType.Number -> UiAnswerType.Number
        DomainAnswerType.Text -> UiAnswerType.Text
        is DomainAnswerType.Select -> UiAnswerType.Select(options.toImmutableList())
        DomainAnswerType.Confirm -> UiAnswerType.Confirm
    }

    private fun Double.formatNumber(): String =
        if (this % 1.0 == 0.0) toLong().toString() else toString()
}
```

- [ ] **Step 4: Запустить тесты — должны проходить**

Run: `./gradlew :shared:feature-checklist:presentation:allTests`
Expected: BUILD SUCCESSFUL — все 11 тестов в `UiChecklistStateMapperTest` зелёные.

- [ ] **Step 5: Commit (Tasks 7+8+9 одним коммитом — presentation layer)**

```bash
git add shared/feature-checklist/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/presentation/ChecklistViewModel.kt \
        shared/feature-checklist/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/presentation/models/UiChecklistItem.kt \
        shared/feature-checklist/presentation/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/presentation/mappers/UiChecklistStateMapper.kt \
        shared/feature-checklist/presentation/src/commonTest/kotlin/ru/mirea/toir/feature/checklist/presentation/mappers/UiChecklistStateMapperTest.kt
git commit -m "feat(checklist): derive showValidationError per item in state mapper"
```

---

### Task 10: BooleanChecklistItem — Switch → ToirSegmentedControl

**Files:**
- Modify: `shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/BooleanChecklistItem.kt`

- [ ] **Step 1: Заменить файл целиком**

```kotlin
package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.persistentListOf
import ru.mirea.toir.common.ui.compose.components.shared.segmented.ToirSegmentedControl
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.res.MR

@Composable
internal fun BooleanChecklistItem(
    item: UiChecklistItem,
    onValueChange: (Boolean?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = remember { persistentListOf(true, false) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    ) {
        Text(
            text = item.titleWithRequiredMarker(),
            style = ToirTheme.typography.bodyLarge,
            color = if (item.showValidationError) ToirTheme.colors.error else ToirTheme.colors.textPrimary,
        )
        ToirSegmentedControl(
            options = options,
            selected = item.valueBoolean,
            onSelectedChange = onValueChange,
            optionLabel = { value ->
                stringResource(
                    if (value) MR.strings.checklist_button_yes else MR.strings.checklist_button_no,
                )
            },
            isError = item.showValidationError,
        )
    }
}
```

- [ ] **Step 2: Сборка ui-модуля (на этом этапе ChecklistScreen не компилируется — это ожидаемо)**

Run: `./gradlew :shared:feature-checklist:ui:compileDebugKotlinAndroid`
Expected: FAIL — `ChecklistScreen` всё ещё передаёт `(Boolean) -> Unit` в `BooleanChecklistItem`. Фиксим в Task 13.

(Не коммитим отдельно — атомарный коммит UI после Task 13.)

---

### Task 11: ConfirmChecklistItem — Button → ToirToggleChip

**Files:**
- Modify: `shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/ConfirmChecklistItem.kt`

- [ ] **Step 1: Заменить файл целиком**

```kotlin
package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.components.shared.chip.ToirToggleChip
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.res.MR

@Composable
internal fun ConfirmChecklistItem(
    item: UiChecklistItem,
    onConfirmChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    ) {
        Text(
            text = item.titleWithRequiredMarker(),
            style = ToirTheme.typography.bodyLarge,
            color = if (item.showValidationError) ToirTheme.colors.error else ToirTheme.colors.textPrimary,
        )
        ToirToggleChip(
            selected = item.isConfirmed,
            onSelectedChange = onConfirmChange,
            text = stringResource(MR.strings.checklist_button_done),
            isError = item.showValidationError,
        )
    }
}
```

(Не коммитим отдельно — атомарный коммит UI после Task 13.)

---

### Task 12: NumberChecklistItem, TextChecklistItem, SelectChecklistItem — добавить showValidationError

**Files:**
- Modify: `shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/NumberChecklistItem.kt:30-32`
- Modify: `shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/TextChecklistItem.kt`
- Modify: `shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/SelectChecklistItem.kt:34-37`

- [ ] **Step 1: NumberChecklistItem — `isError = isOutOfRange || item.showValidationError`**

В блоке `val isOutOfRange = ...` (строки ~27-30) добавить вычисление:

```kotlin
    val isOutOfRange = parsed != null &&
        ((min != null && parsed < min) || (max != null && parsed > max))
    val isInvalid = isOutOfRange || item.showValidationError
    val rangeHint = rangeHint(item.numericMin, item.numericMax)
    val supportingText = when {
        isOutOfRange -> stringResource(MR.strings.checklist_number_error_out_of_range)
        item.showValidationError -> stringResource(MR.strings.checklist_validation_error_required)
        rangeHint != null -> rangeHint
        else -> null
    }
```

И заменить `isError = isOutOfRange,` на `isError = isInvalid,`.

- [ ] **Step 2: TextChecklistItem — заменить файл**

```kotlin
package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.components.shared.textfield.ToirOutlinedTextField
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.res.MR

@Composable
internal fun TextChecklistItem(
    item: UiChecklistItem,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var input by remember(item.id, item.valueText) { mutableStateOf(item.valueText) }
    ToirOutlinedTextField(
        value = input,
        onValueChange = { newValue ->
            input = newValue
            onValueChange(newValue)
        },
        modifier = modifier.fillMaxWidth(),
        label = item.title,
        isRequired = item.isRequired,
        isError = item.showValidationError,
        supportingText = if (item.showValidationError) {
            stringResource(MR.strings.checklist_validation_error_required)
        } else {
            null
        },
        singleLine = true,
    )
}
```

- [ ] **Step 3: SelectChecklistItem — подсветка label при ошибке**

Заменить блок `Text(...)` для заголовка (строки ~34-38):

```kotlin
        Text(
            text = item.titleWithRequiredMarker(),
            style = ToirTheme.typography.bodyLarge,
            color = if (item.showValidationError) {
                ToirTheme.colors.error
            } else {
                ToirTheme.colors.textPrimary
            },
        )
```

(Не коммитим отдельно — атомарный коммит UI после Task 13.)

---

### Task 13: ChecklistScreen — обновить сигнатуры коллбеков

**Files:**
- Modify: `shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/ChecklistScreen.kt:84-89,213-218,225-228,283-287`

- [ ] **Step 1: ChecklistScreenContent сигнатура**

Заменить параметры `onBooleanAnswer` и `onConfirm`:

Старые (строки ~84-89):

```kotlin
    onBooleanAnswer: (String, Boolean) -> Unit,
    onNumberAnswer: (String, String) -> Unit,
    onTextAnswer: (String, String) -> Unit,
    onSelectAnswer: (String, String) -> Unit,
    onConfirm: (String) -> Unit,
```

Новые:

```kotlin
    onBooleanAnswer: (String, Boolean?) -> Unit,
    onNumberAnswer: (String, String) -> Unit,
    onTextAnswer: (String, String) -> Unit,
    onSelectAnswer: (String, String) -> Unit,
    onConfirm: (String, Boolean) -> Unit,
```

- [ ] **Step 2: ChecklistList сигнатура**

Тот же блок параметров обновить в `private fun ChecklistList(...)` (строки ~213-220) и `private fun ChecklistItemRow(...)` (строки ~225-232) — `onBooleanAnswer` и `onConfirm` приобретают новые типы.

- [ ] **Step 3: ChecklistItemRow — передача value в onConfirm**

Старый блок (строки ~283-286):

```kotlin
            is UiAnswerType.Confirm -> ConfirmChecklistItem(
                item = item,
                onConfirm = { onConfirm(item.id) },
            )
```

Новый:

```kotlin
            is UiAnswerType.Confirm -> ConfirmChecklistItem(
                item = item,
                onConfirmChange = { value -> onConfirm(item.id, value) },
            )
```

- [ ] **Step 4: Сборка ui-модуля**

Run: `./gradlew :shared:feature-checklist:ui:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit (Tasks 10+11+12+13 одним коммитом — UI layer)**

```bash
git add shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/BooleanChecklistItem.kt \
        shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/ConfirmChecklistItem.kt \
        shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/NumberChecklistItem.kt \
        shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/TextChecklistItem.kt \
        shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/items/SelectChecklistItem.kt \
        shared/feature-checklist/ui/src/commonMain/kotlin/ru/mirea/toir/feature/checklist/ui/ChecklistScreen.kt
git commit -m "feat(checklist-ui): tristate Boolean, toggleable Confirm, per-item required highlight"
```

---

### Task 14: Resources — новые строки

**Files:**
- Modify: `shared/common-resources/src/commonMain/moko-resources/base/strings.xml`

- [ ] **Step 1: Добавить три строки и удалить устаревшую**

В блоке checklist-ресурсов (строки 75-85):

```xml
    <string name="checklist_button_finish">Завершить проверку</string>
    <string name="checklist_button_add_photo">Добавить фото</string>
    <string name="checklist_button_done">Выполнено</string>
    <string name="checklist_button_yes">Да</string>
    <string name="checklist_button_no">Нет</string>
```

Удалить старую запись:

```xml
    <string name="checklist_button_confirm">Выполнено</string>
```

(Если в проекте есть другие локализации — `strings-en.xml`, `strings-ru.xml` и т.п. — добавить переводы туда же. Сейчас в файле только `base/strings.xml` — этим и ограничиваемся.)

- [ ] **Step 2: Прогнать moko-resources gen**

Run: `./gradlew :shared:common-resources:generateMRcommonMain`
Expected: BUILD SUCCESSFUL — генерируется `MR.strings.checklist_button_yes/no/done`.

- [ ] **Step 3: Сборка всего checklist стека**

Run: `./gradlew :shared:feature-checklist:ui:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add shared/common-resources/src/commonMain/moko-resources/base/strings.xml
git commit -m "chore(resources): add yes/no/done strings, drop checklist_button_confirm"
```

---

### Task 15: Финальная сборка + detekt + Android debug APK

**Files:** (только команды)

- [ ] **Step 1: Detekt по всему проекту**

Run: `./gradlew detekt`
Expected: BUILD SUCCESSFUL без warnings.

- [ ] **Step 2: Прогнать все тесты по затронутым модулям**

Run: `./gradlew :shared:feature-checklist:impl:allTests :shared:feature-checklist:presentation:allTests :shared:common-ui:allTests`
Expected: BUILD SUCCESSFUL — `ChecklistReducerTest` (старые тесты) + `UiChecklistStateMapperTest` (новые).

- [ ] **Step 3: Android debug APK**

Run: `./gradlew :android:app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Ручная верификация на эмуляторе/устройстве**

Установить APK, открыть чеклист с разными типами полей и проверить:

1. **Boolean** — есть `[Да | Нет]`-сегмент, по умолчанию ничего не выбрано. Тап на «Да» → подсветка «Да». Повторный тап на «Да» → выбор снят (оба сегмента нейтральные).
2. **Confirm** — чип «Выполнено» сначала outlined, тап → filled с галочкой/border success. Повторный тап → выключается обратно.
3. **Required-валидация** — оставить любой обязательный пункт без ответа, нажать «Завершить проверку». Ожидаемо: глобальный баннер сверху + красная подсветка каждого незаполненного required (label красный для Text/Number/Select/Boolean/Confirm; рамка красная для Boolean SegmentedControl, ToirToggleChip и Number/Text TextField).
4. **Снятие подсветки** — ответить на любой подсвеченный required → подсветка для именно этого пункта пропадает (другие остаются).

- [ ] **Step 5: Если ручная проверка прошла — коммит уже не нужен (всё в commits 1-14)**

Run: `git log --oneline main..HEAD`
Expected: 5 новых коммитов (Tasks 1, 2, 3-6, 7-9, 10-13, 14) — итого 5-6 атомарных коммитов на ветке.

---

## Self-Review Checklist

**1. Spec coverage:**
- ✅ Boolean → segmented "Да | Нет" — Task 10
- ✅ Tristate / повторный клик снимает выбор — Task 1 (`if (isSelected) onSelectedChange(null)`)
- ✅ Confirm → toggle-chip — Task 2, 11
- ✅ Confirm отжимается — Task 4 (`if (value) 1L else null` в saveConfirm)
- ✅ Required-подсветка после finish — Task 9 (`showValidationError` в маппере) + Tasks 10-12 (подсветка в каждом item)
- ✅ Нейтральные цвета Да/Нет — Task 1 (`activeContainerColor = surface2`, `activeContentColor = textPrimary`, без success/error)
- ✅ Ничего не меняется в БД/sync DTO/OpenAPI — отмечено в Scope Notes

**2. Placeholder scan:**
- Все шаги содержат конкретный код или конкретные команды.
- Имена символов согласованы между task'ами:
  - `ToirSegmentedControl` (T1, T10) ✓
  - `ToirToggleChip` (T2, T11) ✓
  - `saveBooleanAnswer(Boolean?)` (T3, T4, T6) ✓
  - `saveConfirm(Boolean)` (T3, T4, T6) ✓
  - `Intent.OnBooleanAnswer(itemId, value: Boolean?)` (T5, T6, T7) ✓
  - `Intent.OnConfirm(itemId, value: Boolean)` (T5, T6, T7) ✓
  - `UiChecklistItem.showValidationError` (T8, T9, T10, T11, T12) ✓
  - `onConfirmChange: (Boolean) -> Unit` (T11, T13) ✓
  - `onValueChange: (Boolean?) -> Unit` для BooleanChecklistItem (T10, T13) ✓
  - `onBooleanAnswer: (String, Boolean?) -> Unit` (T7, T13) ✓
  - `onConfirm: (String, Boolean) -> Unit` (T7, T13) ✓

**3. Type consistency:** в порядке, см. выше.

**4. Известные риски:**
- M3 `SegmentedButton.icon = {}` — пустой icon-slot убирает чекмарк, который M3 рисует у выбранного сегмента по дефолту. Если визуально не устраивает — оставить чекмарк (но он будет в `activeContentColor = textPrimary`, нейтральный).
- В Task 2 проверяется наличие `MR.images.ic_check`. Если иконки нет — fallback на текст без иконки прописан там же.
- `presentation` модуль на момент написания плана не имеет `commonTest` source-set. KMP convention plugin обычно создаёт его автоматически при наличии файлов в `src/commonTest`. Если тесты не запустятся — добавить пустой `src/commonTest/kotlin/.gitkeep`, перезапустить gradle sync. В крайнем случае — перенести `UiChecklistStateMapperTest` в `feature-checklist/impl/src/commonTest/...` (зависимость presentation→impl уже есть транзитивно? — нет, impl зависит от api+core, presentation зависит от api+impl. Перенести тест в impl нельзя из-за visibility: `UiChecklistStateMapperImpl internal class` в `presentation`. Если convention не поднимет commonTest — придётся открыть `internal` модификатор маппера или добавить test-only публичный конструктор-фактори).
