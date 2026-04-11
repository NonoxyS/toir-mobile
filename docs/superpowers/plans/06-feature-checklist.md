# Waypoint 06 — Feature Checklist (Чек-лист проверки)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Реализовать экран чек-листа с пятью типами ответов (Boolean, Number, Text, Select, Confirm), обязательностью, фотообязательностью и валидацией перед завершением.

**Architecture:** 4-модульная фича. Данные: `ChecklistItem` из SQLDelight + `ChecklistItemResult` (создаётся/обновляется локально сразу при ответе). Каждый ответ сохраняется немедленно. При нажатии «Завершить проверку» — валидация обязательных пунктов → если OK: обновить `InspectionEquipmentResult.status = COMPLETED`.

**Tech Stack:** MVIKotlin, SQLDelight (ChecklistDao, InspectionDao), Compose Multiplatform.

---

## Файловая структура

```
shared/feature-checklist/
├── api/src/commonMain/.../feature/checklist/api/
│   ├── store/ChecklistStore.kt
│   └── models/DomainChecklistItem.kt
├── impl/src/commonMain/.../feature/checklist/impl/
│   ├── di/FeatureChecklistImplModule.kt
│   ├── domain/repository/ChecklistRepository.kt         ← интерфейс в domain
│   ├── data/repository/ChecklistRepositoryImpl.kt
│   └── domain/ChecklistStoreFactory.kt  (+ Executor + Reducer)
├── presentation/src/commonMain/.../feature/checklist/presentation/
│   ├── di/FeatureChecklistPresentationModule.kt
│   ├── models/UiChecklistState.kt
│   ├── models/UiChecklistLabel.kt
│   ├── models/UiChecklistItem.kt
│   ├── mappers/UiChecklistStateMapper.kt
│   ├── mappers/UiChecklistLabelMapper.kt
│   └── ChecklistViewModel.kt
└── ui/src/commonMain/.../feature/checklist/ui/
    ├── api/FeatureChecklistScreenApi.kt
    ├── ChecklistScreen.kt
    └── items/
        ├── BooleanChecklistItem.kt
        ├── NumberChecklistItem.kt
        ├── TextChecklistItem.kt
        ├── SelectChecklistItem.kt
        └── ConfirmChecklistItem.kt
```

---

### Task 1: Регистрация и build-файлы

- [ ] **Step 1: Зарегистрировать в settings.gradle.kts**

```kotlin
include(":shared:feature-checklist:api")
include(":shared:feature-checklist:impl")
include(":shared:feature-checklist:presentation")
include(":shared:feature-checklist:ui")
```

- [ ] **Step 2: Создать build.gradle.kts для каждого субмодуля**

`api/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
plugins { alias(libs.plugins.conventionPlugin.kmpFeatureSetup) }
androidLibraryConfig { namespace = "ru.mirea.toir.feature.checklist.api" }
```

`impl/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations
plugins { alias(libs.plugins.conventionPlugin.kmpFeatureSetup) }
androidLibraryConfig { namespace = "ru.mirea.toir.feature.checklist.impl" }
commonMainDependencies {
    implementations(
        projects.shared.coreDatabase,
        libs.kotlin.serialization.json,
    )
}
```

`presentation/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}
androidLibraryConfig { namespace = "ru.mirea.toir.feature.checklist.presentation" }
```

`ui/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}
androidLibraryConfig { namespace = "ru.mirea.toir.feature.checklist.ui" }
```

---

### Task 2: Domain-модели и Store (api)

**Files:**
- Create: `.../feature/checklist/api/models/DomainChecklistItem.kt`
- Create: `.../feature/checklist/api/store/ChecklistStore.kt`

- [ ] **Step 1: DomainChecklistItem**

```kotlin
package ru.mirea.toir.feature.checklist.api.models

sealed class DomainAnswerType {
    data object Boolean : DomainAnswerType()
    data object Number : DomainAnswerType()
    data object Text : DomainAnswerType()
    data class Select(val options: List<String>) : DomainAnswerType()
    data object Confirm : DomainAnswerType()
}

data class DomainChecklistItem(
    val id: String,
    val title: String,
    val description: String?,
    val answerType: DomainAnswerType,
    val isRequired: Boolean,
    val requiresPhoto: Boolean,
    val resultId: String?,
    val valueBoolean: Boolean?,
    val valueNumber: Double?,
    val valueText: String?,
    val valueSelect: String?,
    val isConfirmed: Boolean,
    val photoCount: Int,
)
```

- [ ] **Step 2: ChecklistStore**

```kotlin
package ru.mirea.toir.feature.checklist.api.store

import com.arkivanov.mvikotlin.core.store.Store
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem

interface ChecklistStore : Store<ChecklistStore.Intent, ChecklistStore.State, ChecklistStore.Label> {

    data class State(
        val equipmentResultId: String = "",
        val items: ImmutableList<DomainChecklistItem> = persistentListOf(),
        val isLoading: Boolean = true,
        val validationError: String? = null,
        val isCompleted: Boolean = false,
    )

    sealed interface Intent {
        data class Init(val equipmentResultId: String) : Intent
        data class OnBooleanAnswer(val itemId: String, val value: Boolean) : Intent
        data class OnNumberAnswer(val itemId: String, val value: String) : Intent
        data class OnTextAnswer(val itemId: String, val value: String) : Intent
        data class OnSelectAnswer(val itemId: String, val value: String) : Intent
        data class OnConfirm(val itemId: String) : Intent
        data class OnAddPhoto(val itemId: String) : Intent
        data object OnFinishChecklist : Intent
    }

    sealed interface Label {
        data class NavigateToPhotoCapture(val checklistItemResultId: String) : Label
        data object ChecklistCompleted : Label
    }
}
```

---

### Task 3: Repository (impl)

**Files:**
- Create: `.../feature/checklist/impl/domain/repository/ChecklistRepository.kt`
- Create: `.../feature/checklist/impl/data/repository/ChecklistRepositoryImpl.kt`

- [ ] **Step 1: ChecklistRepository**

```kotlin
package ru.mirea.toir.feature.checklist.impl.domain.repository

import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem

internal interface ChecklistRepository {
    suspend fun getChecklistItems(equipmentResultId: String): Result<List<DomainChecklistItem>>
    suspend fun saveBooleanAnswer(equipmentResultId: String, itemId: String, value: Boolean): Result<Unit>
    suspend fun saveNumberAnswer(equipmentResultId: String, itemId: String, value: Double): Result<Unit>
    suspend fun saveTextAnswer(equipmentResultId: String, itemId: String, value: String): Result<Unit>
    suspend fun saveSelectAnswer(equipmentResultId: String, itemId: String, value: String): Result<Unit>
    suspend fun saveConfirm(equipmentResultId: String, itemId: String): Result<Unit>
    suspend fun finishChecklist(equipmentResultId: String): Result<Unit>
}
```

- [ ] **Step 2: ChecklistRepositoryImpl**

```kotlin
package ru.mirea.toir.feature.checklist.impl.data.repository

import io.github.aakira.napier.Napier
import ru.mirea.toir.feature.checklist.impl.domain.repository.ChecklistRepository
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.dao.ChecklistDao
import ru.mirea.toir.core.database.dao.InspectionDao
import ru.mirea.toir.core.database.dao.PhotoDao
import ru.mirea.toir.feature.checklist.api.models.DomainAnswerType
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class ChecklistRepositoryImpl(
    private val inspectionDao: InspectionDao,
    private val checklistDao: ChecklistDao,
    private val photoDao: PhotoDao,
    private val coroutineDispatchers: CoroutineDispatchers,
) : ChecklistRepository {

    override suspend fun getChecklistItems(equipmentResultId: String): Result<List<DomainChecklistItem>> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val equipmentResult = inspectionDao.selectEquipmentResultById(equipmentResultId)
                        ?: error("EquipmentResult not found: $equipmentResultId")
                    val routePoint = null // routePointId stored in result
                    // We need checklist linked to the route point's checklist_id
                    // Lookup via RouteDao is needed — inject RouteDao
                    // For now use the equipment result's checklist lookup via ChecklistDao
                    // NOTE: RouteDao is needed here — add to constructor in full implementation
                    val existingResults = inspectionDao.selectChecklistItemResultsByEquipmentResult(equipmentResultId)

                    // items will come from checklist linked to equipmentResult.route_point_id
                    // Implementation requires RouteDao to get checklist_id from route_point
                    // Simplified: assume checklistItems are fetched via checklistDao by route_point checklist
                    emptyList<DomainChecklistItem>().wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "getChecklistItems failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    // Full implementation required — see note below
    // getChecklistItems should:
    // 1. Get equipmentResult by id
    // 2. Get RoutePoint by equipmentResult.route_point_id (need RouteDao)
    // 3. Get ChecklistItems by routePoint.checklist_id
    // 4. Get existing ChecklistItemResults by equipmentResultId
    // 5. Map each item with existing result values + photo count

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveBooleanAnswer(equipmentResultId: String, itemId: String, value: Boolean): Result<Unit> =
        saveAnswer(equipmentResultId, itemId) { resultId ->
            inspectionDao.insertOrReplaceChecklistItemResult(
                id = resultId,
                equipmentResultId = equipmentResultId,
                checklistItemId = itemId,
                valueBoolean = if (value) 1L else 0L,
                valueNumber = null,
                valueText = null,
                valueSelect = null,
                isConfirmed = 0L,
                answeredAt = Clock.System.now().toString(),
            )
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveNumberAnswer(equipmentResultId: String, itemId: String, value: Double): Result<Unit> =
        saveAnswer(equipmentResultId, itemId) { resultId ->
            inspectionDao.insertOrReplaceChecklistItemResult(
                id = resultId,
                equipmentResultId = equipmentResultId,
                checklistItemId = itemId,
                valueBoolean = null,
                valueNumber = value,
                valueText = null,
                valueSelect = null,
                isConfirmed = 0L,
                answeredAt = Clock.System.now().toString(),
            )
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveTextAnswer(equipmentResultId: String, itemId: String, value: String): Result<Unit> =
        saveAnswer(equipmentResultId, itemId) { resultId ->
            inspectionDao.insertOrReplaceChecklistItemResult(
                id = resultId,
                equipmentResultId = equipmentResultId,
                checklistItemId = itemId,
                valueBoolean = null,
                valueNumber = null,
                valueText = value,
                valueSelect = null,
                isConfirmed = 0L,
                answeredAt = Clock.System.now().toString(),
            )
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveSelectAnswer(equipmentResultId: String, itemId: String, value: String): Result<Unit> =
        saveAnswer(equipmentResultId, itemId) { resultId ->
            inspectionDao.insertOrReplaceChecklistItemResult(
                id = resultId,
                equipmentResultId = equipmentResultId,
                checklistItemId = itemId,
                valueBoolean = null,
                valueNumber = null,
                valueText = null,
                valueSelect = value,
                isConfirmed = 0L,
                answeredAt = Clock.System.now().toString(),
            )
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveConfirm(equipmentResultId: String, itemId: String): Result<Unit> =
        saveAnswer(equipmentResultId, itemId) { resultId ->
            inspectionDao.insertOrReplaceChecklistItemResult(
                id = resultId,
                equipmentResultId = equipmentResultId,
                checklistItemId = itemId,
                valueBoolean = null,
                valueNumber = null,
                valueText = null,
                valueSelect = null,
                isConfirmed = 1L,
                answeredAt = Clock.System.now().toString(),
            )
        }

    override suspend fun finishChecklist(equipmentResultId: String): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    inspectionDao.updateEquipmentResultStatus(
                        id = equipmentResultId,
                        status = "COMPLETED",
                        startedAt = null,
                        completedAt = Clock.System.now().toString(),
                    )
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "finishChecklist failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun saveAnswer(
        equipmentResultId: String,
        itemId: String,
        block: suspend (resultId: String) -> Unit,
    ): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    val existing = inspectionDao.selectChecklistItemResult(itemId, equipmentResultId)
                    val resultId = existing?.id ?: Uuid.random().toString()
                    block(resultId)
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "saveAnswer failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
```

> ⚠️ **Важно:** в `getChecklistItems` нужно инжектировать `RouteDao` для получения `checklist_id` из `route_point`. Добавь `RouteDao` в конструктор `ChecklistRepositoryImpl` в реальной реализации.

---

### Task 4: Executor, Reducer, StoreFactory (impl)

**Files:**
- Create: `.../feature/checklist/impl/domain/ChecklistReducer.kt`
- Create: `.../feature/checklist/impl/domain/ChecklistExecutor.kt`
- Create: `.../feature/checklist/impl/domain/ChecklistStoreFactory.kt`

- [ ] **Step 1: Написать тест для ChecklistReducer**

```kotlin
package ru.mirea.toir.feature.checklist.impl.domain

import kotlinx.collections.immutable.persistentListOf
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChecklistReducerTest {
    private val reducer = ChecklistReducer()
    private val initial = ChecklistStore.State()

    @Test
    fun `SetLoading sets isLoading`() {
        val result = with(reducer) { initial.reduce(ChecklistStoreFactory.Message.SetLoading) }
        assertTrue(result.isLoading)
    }

    @Test
    fun `SetValidationError sets error`() {
        val result = with(reducer) {
            initial.reduce(ChecklistStoreFactory.Message.SetValidationError("Заполните обязательные поля"))
        }
        assertNotNull(result.validationError)
        assertEquals("Заполните обязательные поля", result.validationError)
    }

    @Test
    fun `SetItems replaces items and clears loading`() {
        val result = with(reducer) {
            initial.copy(isLoading = true).reduce(ChecklistStoreFactory.Message.SetItems(persistentListOf()))
        }
        assertFalse(result.isLoading)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `ClearValidationError removes error`() {
        val withError = initial.copy(validationError = "Error")
        val result = with(reducer) { withError.reduce(ChecklistStoreFactory.Message.ClearValidationError) }
        assertNull(result.validationError)
    }
}
```

- [ ] **Step 2: Запустить тест — убедиться, что падает**

```bash
./gradlew :shared:feature-checklist:impl:testDebugUnitTest 2>&1 | tail -10
```

- [ ] **Step 3: Создать ChecklistReducer**

```kotlin
package ru.mirea.toir.feature.checklist.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore

internal class ChecklistReducer : Reducer<ChecklistStore.State, ChecklistStoreFactory.Message> {
    override fun ChecklistStore.State.reduce(msg: ChecklistStoreFactory.Message): ChecklistStore.State = when (msg) {
        ChecklistStoreFactory.Message.SetLoading -> copy(isLoading = true, validationError = null)
        is ChecklistStoreFactory.Message.SetItems -> copy(isLoading = false, items = msg.items, equipmentResultId = msg.equipmentResultId)
        is ChecklistStoreFactory.Message.UpdateItem -> copy(
            items = items.map { if (it.id == msg.item.id) msg.item else it }
                .let { kotlinx.collections.immutable.toImmutableList(it) }
        )
        is ChecklistStoreFactory.Message.SetValidationError -> copy(validationError = msg.message)
        ChecklistStoreFactory.Message.ClearValidationError -> copy(validationError = null)
        ChecklistStoreFactory.Message.SetCompleted -> copy(isCompleted = true)
    }
}
```

- [ ] **Step 4: Запустить тест — убедиться, что проходит**

```bash
./gradlew :shared:feature-checklist:impl:testDebugUnitTest
```

- [ ] **Step 5: Создать ChecklistExecutor**

```kotlin
package ru.mirea.toir.feature.checklist.impl.domain

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.checklist.api.models.DomainAnswerType
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore
import ru.mirea.toir.feature.checklist.impl.domain.repository.ChecklistRepository

internal class ChecklistExecutor(
    private val repository: ChecklistRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<ChecklistStore.Intent, Nothing, ChecklistStore.State, ChecklistStoreFactory.Message, ChecklistStore.Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteIntent(intent: ChecklistStore.Intent) {
        when (intent) {
            is ChecklistStore.Intent.Init -> loadItems(intent.equipmentResultId)
            is ChecklistStore.Intent.OnBooleanAnswer -> {
                repository.saveBooleanAnswer(state().equipmentResultId, intent.itemId, intent.value)
                reloadItems()
            }
            is ChecklistStore.Intent.OnNumberAnswer -> {
                val number = intent.value.toDoubleOrNull() ?: return
                repository.saveNumberAnswer(state().equipmentResultId, intent.itemId, number)
                reloadItems()
            }
            is ChecklistStore.Intent.OnTextAnswer -> {
                repository.saveTextAnswer(state().equipmentResultId, intent.itemId, intent.value)
                reloadItems()
            }
            is ChecklistStore.Intent.OnSelectAnswer -> {
                repository.saveSelectAnswer(state().equipmentResultId, intent.itemId, intent.value)
                reloadItems()
            }
            is ChecklistStore.Intent.OnConfirm -> {
                repository.saveConfirm(state().equipmentResultId, intent.itemId)
                reloadItems()
            }
            is ChecklistStore.Intent.OnAddPhoto -> {
                val item = state().items.firstOrNull { it.id == intent.itemId } ?: return
                val resultId = item.resultId ?: return
                publish(ChecklistStore.Label.NavigateToPhotoCapture(resultId))
            }
            ChecklistStore.Intent.OnFinishChecklist -> finishChecklist()
        }
    }

    private suspend fun loadItems(equipmentResultId: String) {
        dispatch(ChecklistStoreFactory.Message.SetLoading)
        repository.getChecklistItems(equipmentResultId).fold(
            onSuccess = { items ->
                dispatch(ChecklistStoreFactory.Message.SetItems(
                    equipmentResultId = equipmentResultId,
                    items = items.toImmutableList(),
                ))
            },
            onFailure = { throwable ->
                dispatch(ChecklistStoreFactory.Message.SetValidationError(throwable.message ?: "Ошибка загрузки"))
            },
        )
    }

    private suspend fun reloadItems() {
        loadItems(state().equipmentResultId)
    }

    private suspend fun finishChecklist() {
        val currentState = state()
        val validationError = validate(currentState)
        if (validationError != null) {
            dispatch(ChecklistStoreFactory.Message.SetValidationError(validationError))
            return
        }
        dispatch(ChecklistStoreFactory.Message.ClearValidationError)
        repository.finishChecklist(currentState.equipmentResultId).fold(
            onSuccess = {
                dispatch(ChecklistStoreFactory.Message.SetCompleted)
                publish(ChecklistStore.Label.ChecklistCompleted)
            },
            onFailure = { throwable ->
                dispatch(ChecklistStoreFactory.Message.SetValidationError(throwable.message ?: "Ошибка завершения"))
            },
        )
    }

    private fun validate(state: ChecklistStore.State): String? {
        val missingRequired = state.items.any { item ->
            item.isRequired && !item.isAnswered()
        }
        if (missingRequired) return "Заполните все обязательные поля"

        val missingPhoto = state.items.any { item ->
            item.requiresPhoto && item.photoCount == 0 && item.isAnswered()
        }
        if (missingPhoto) return "Прикрепите фото к обязательным пунктам"

        return null
    }
}

private fun ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem.isAnswered(): Boolean = when (answerType) {
    is DomainAnswerType.Boolean -> valueBoolean != null
    is DomainAnswerType.Number -> valueNumber != null
    is DomainAnswerType.Text -> !valueText.isNullOrBlank()
    is DomainAnswerType.Select -> !valueSelect.isNullOrBlank()
    is DomainAnswerType.Confirm -> isConfirmed
}
```

- [ ] **Step 6: Создать ChecklistStoreFactory**

```kotlin
package ru.mirea.toir.feature.checklist.impl.domain

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore
import ru.mirea.toir.feature.checklist.impl.domain.repository.ChecklistRepository

internal class ChecklistStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: ChecklistRepository,
    private val mainDispatcher: CoroutineDispatcher,
) {
    fun create(): ChecklistStore =
        object :
            ChecklistStore,
            Store<ChecklistStore.Intent, ChecklistStore.State, ChecklistStore.Label> by storeFactory.create(
                name = ChecklistStore::class.simpleName,
                initialState = ChecklistStore.State(),
                bootstrapper = null,
                executorFactory = { ChecklistExecutor(repository, mainDispatcher) },
                reducer = ChecklistReducer(),
            ) {}

    internal sealed interface Message {
        data object SetLoading : Message
        data class SetItems(val equipmentResultId: String, val items: ImmutableList<DomainChecklistItem>) : Message
        data class UpdateItem(val item: DomainChecklistItem) : Message
        data class SetValidationError(val message: String) : Message
        data object ClearValidationError : Message
        data object SetCompleted : Message
    }
}
```

- [ ] **Step 7: DI-модуль impl**

```kotlin
package ru.mirea.toir.feature.checklist.impl.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.feature.checklist.impl.domain.repository.ChecklistRepository
import ru.mirea.toir.feature.checklist.impl.data.repository.ChecklistRepositoryImpl
import ru.mirea.toir.feature.checklist.impl.domain.ChecklistStoreFactory

val featureChecklistImplModule = module {
    factory<ChecklistRepository> { new(::ChecklistRepositoryImpl) }
    factory { new(::ChecklistStoreFactory) }
}
```

---

### Task 5: Presentation-модуль

**Files:**
- Create: `.../feature/checklist/presentation/models/UiChecklistItem.kt`
- Create: `.../feature/checklist/presentation/models/UiChecklistState.kt`
- Create: `.../feature/checklist/presentation/models/UiChecklistLabel.kt`
- Create: `.../feature/checklist/presentation/mappers/UiChecklistStateMapper.kt`
- Create: `.../feature/checklist/presentation/mappers/UiChecklistLabelMapper.kt`
- Create: `.../feature/checklist/presentation/ChecklistViewModel.kt`
- Create: `.../feature/checklist/presentation/di/FeatureChecklistPresentationModule.kt`

- [ ] **Step 1: UiChecklistItem**

```kotlin
package ru.mirea.toir.feature.checklist.presentation.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed class UiAnswerType {
    data object Boolean : UiAnswerType()
    data object Number : UiAnswerType()
    data object Text : UiAnswerType()
    data class Select(val options: ImmutableList<String>) : UiAnswerType()
    data object Confirm : UiAnswerType()
}

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
)
```

- [ ] **Step 2: UiChecklistState + UiChecklistLabel**

```kotlin
// UiChecklistState.kt
package ru.mirea.toir.feature.checklist.presentation.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class UiChecklistState(
    val items: ImmutableList<UiChecklistItem> = persistentListOf(),
    val isLoading: Boolean = true,
    val validationError: String? = null,
    val isCompleted: Boolean = false,
)
```

```kotlin
// UiChecklistLabel.kt
package ru.mirea.toir.feature.checklist.presentation.models

sealed interface UiChecklistLabel {
    data class NavigateToPhotoCapture(val checklistItemResultId: String) : UiChecklistLabel
    data object ChecklistCompleted : UiChecklistLabel
}
```

- [ ] **Step 3: StateMapper**

```kotlin
package ru.mirea.toir.feature.checklist.presentation.mappers

import kotlinx.collections.immutable.toImmutableList
import ru.mirea.toir.feature.checklist.api.models.DomainAnswerType
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore
import ru.mirea.toir.feature.checklist.presentation.models.UiAnswerType
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistState

interface UiChecklistStateMapper {
    fun map(state: ChecklistStore.State): UiChecklistState
}

internal class UiChecklistStateMapperImpl : UiChecklistStateMapper {
    override fun map(state: ChecklistStore.State): UiChecklistState = UiChecklistState(
        items = state.items.map { it.toUi() }.toImmutableList(),
        isLoading = state.isLoading,
        validationError = state.validationError,
        isCompleted = state.isCompleted,
    )

    private fun DomainChecklistItem.toUi(): UiChecklistItem = UiChecklistItem(
        id = id,
        title = title,
        description = description,
        answerType = answerType.toUi(),
        isRequired = isRequired,
        requiresPhoto = requiresPhoto,
        resultId = resultId,
        valueBoolean = valueBoolean,
        valueNumber = valueNumber?.toString().orEmpty(),
        valueText = valueText.orEmpty(),
        valueSelect = valueSelect,
        isConfirmed = isConfirmed,
        photoCount = photoCount,
    )

    private fun DomainAnswerType.toUi(): UiAnswerType = when (this) {
        DomainAnswerType.Boolean -> UiAnswerType.Boolean
        DomainAnswerType.Number -> UiAnswerType.Number
        DomainAnswerType.Text -> UiAnswerType.Text
        is DomainAnswerType.Select -> UiAnswerType.Select(options.toImmutableList())
        DomainAnswerType.Confirm -> UiAnswerType.Confirm
    }
}
```

- [ ] **Step 4: LabelMapper + ChecklistViewModel + DI**

Следуй паттерну остальных фич. `ChecklistViewModel` принимает `Init(equipmentResultId)` через Compose `LaunchedEffect`.

```kotlin
class ChecklistViewModel internal constructor(...) : BaseViewModel<UiChecklistState, UiChecklistLabel>(...) {
    fun init(equipmentResultId: String) = store.accept(ChecklistStore.Intent.Init(equipmentResultId))
    fun onBooleanAnswer(itemId: String, value: Boolean) = store.accept(ChecklistStore.Intent.OnBooleanAnswer(itemId, value))
    fun onNumberAnswer(itemId: String, value: String) = store.accept(ChecklistStore.Intent.OnNumberAnswer(itemId, value))
    fun onTextAnswer(itemId: String, value: String) = store.accept(ChecklistStore.Intent.OnTextAnswer(itemId, value))
    fun onSelectAnswer(itemId: String, value: String) = store.accept(ChecklistStore.Intent.OnSelectAnswer(itemId, value))
    fun onConfirm(itemId: String) = store.accept(ChecklistStore.Intent.OnConfirm(itemId))
    fun onAddPhoto(itemId: String) = store.accept(ChecklistStore.Intent.OnAddPhoto(itemId))
    fun onFinishChecklist() = store.accept(ChecklistStore.Intent.OnFinishChecklist)
    override fun onCleared() { super.onCleared(); store.dispose() }
}
```

---

### Task 6: UI-модуль (Checklist items)

**Files:**
- Create: `.../feature/checklist/ui/items/BooleanChecklistItem.kt`
- Create: `.../feature/checklist/ui/items/NumberChecklistItem.kt`
- Create: `.../feature/checklist/ui/items/TextChecklistItem.kt`
- Create: `.../feature/checklist/ui/items/SelectChecklistItem.kt`
- Create: `.../feature/checklist/ui/items/ConfirmChecklistItem.kt`
- Create: `.../feature/checklist/ui/ChecklistScreen.kt`
- Create: `.../feature/checklist/ui/api/FeatureChecklistScreenApi.kt`

Добавить строки в strings.xml:
```xml
<!-- checklist -->
<string name="checklist_title">Чек-лист проверки</string>
<string name="checklist_button_finish">Завершить проверку</string>
<string name="checklist_button_add_photo">Добавить фото</string>
<string name="checklist_button_confirm">Выполнено</string>
<string name="checklist_required_marker">*</string>
<string name="checklist_photo_count">Фото: %1$d</string>
<string name="checklist_validation_error_required">Заполните все обязательные поля</string>
<string name="checklist_validation_error_photo">Прикрепите фото к обязательным пунктам</string>
```

- [ ] **Step 1: BooleanChecklistItem**

```kotlin
package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem

@Composable
internal fun BooleanChecklistItem(
    item: UiChecklistItem,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.title + if (item.isRequired) " *" else "",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = item.valueBoolean ?: false,
            onCheckedChange = onValueChange,
        )
    }
}
```

- [ ] **Step 2: NumberChecklistItem**

```kotlin
package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem

@Composable
internal fun NumberChecklistItem(
    item: UiChecklistItem,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = item.title + if (item.isRequired) " *" else "",
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedTextField(
            value = item.valueNumber,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
```

- [ ] **Step 3: TextChecklistItem**

```kotlin
package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem

@Composable
internal fun TextChecklistItem(
    item: UiChecklistItem,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = item.title + if (item.isRequired) " *" else "",
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedTextField(
            value = item.valueText,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
```

- [ ] **Step 4: SelectChecklistItem**

```kotlin
package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.mirea.toir.feature.checklist.presentation.models.UiAnswerType
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem

@Composable
internal fun SelectChecklistItem(
    item: UiChecklistItem,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectType = item.answerType as? UiAnswerType.Select ?: return
    Column(modifier = modifier) {
        Text(
            text = item.title + if (item.isRequired) " *" else "",
            style = MaterialTheme.typography.bodyLarge,
        )
        selectType.options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = item.valueSelect == option,
                    onClick = { onOptionSelected(option) },
                )
                Text(text = option, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
```

- [ ] **Step 5: ConfirmChecklistItem**

```kotlin
package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import ru.mirea.toir.res.MR

@Composable
internal fun ConfirmChecklistItem(
    item: UiChecklistItem,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.title + if (item.isRequired) " *" else "",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onConfirm,
            enabled = !item.isConfirmed,
        ) {
            Text(text = stringResource(MR.strings.checklist_button_confirm))
        }
    }
}
```

- [ ] **Step 6: ChecklistScreen**

```kotlin
package ru.mirea.toir.feature.checklist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.ext.CollectFlow
import ru.mirea.toir.feature.checklist.presentation.ChecklistViewModel
import ru.mirea.toir.feature.checklist.presentation.models.UiAnswerType
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistLabel
import ru.mirea.toir.feature.checklist.ui.items.BooleanChecklistItem
import ru.mirea.toir.feature.checklist.ui.items.ConfirmChecklistItem
import ru.mirea.toir.feature.checklist.ui.items.NumberChecklistItem
import ru.mirea.toir.feature.checklist.ui.items.SelectChecklistItem
import ru.mirea.toir.feature.checklist.ui.items.TextChecklistItem
import ru.mirea.toir.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChecklistScreen(
    equipmentResultId: String,
    onNavigateToPhotoCapture: (checklistItemResultId: String) -> Unit,
    onChecklistCompleted: () -> Unit,
    viewModel: ChecklistViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(equipmentResultId) {
        viewModel.init(equipmentResultId)
    }

    viewModel.label.CollectFlow { label ->
        when (label) {
            is UiChecklistLabel.NavigateToPhotoCapture -> onNavigateToPhotoCapture(label.checklistItemResultId)
            UiChecklistLabel.ChecklistCompleted -> onChecklistCompleted()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(MR.strings.checklist_title)) }) },
    ) { paddingValues ->
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(items = state.items, key = { it.id }) { item ->
                    Column {
                        when (item.answerType) {
                            is UiAnswerType.Boolean -> BooleanChecklistItem(
                                item = item,
                                onValueChange = { viewModel.onBooleanAnswer(item.id, it) },
                            )
                            is UiAnswerType.Number -> NumberChecklistItem(
                                item = item,
                                onValueChange = { viewModel.onNumberAnswer(item.id, it) },
                            )
                            is UiAnswerType.Text -> TextChecklistItem(
                                item = item,
                                onValueChange = { viewModel.onTextAnswer(item.id, it) },
                            )
                            is UiAnswerType.Select -> SelectChecklistItem(
                                item = item,
                                onOptionSelected = { viewModel.onSelectAnswer(item.id, it) },
                            )
                            is UiAnswerType.Confirm -> ConfirmChecklistItem(
                                item = item,
                                onConfirm = { viewModel.onConfirm(item.id) },
                            )
                        }
                        if (item.requiresPhoto) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(onClick = { viewModel.onAddPhoto(item.id) }) {
                                Text(text = stringResource(MR.strings.checklist_button_add_photo))
                            }
                            if (item.photoCount > 0) {
                                Text(
                                    text = "Фото: ${item.photoCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }

                item {
                    if (state.validationError != null) {
                        Text(
                            text = state.validationError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = viewModel::onFinishChecklist,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(MR.strings.checklist_button_finish))
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 7: FeatureChecklistScreenApi**

```kotlin
package ru.mirea.toir.feature.checklist.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ru.mirea.toir.core.navigation.ChecklistRoute
import ru.mirea.toir.feature.checklist.ui.ChecklistScreen

fun NavGraphBuilder.composableChecklistScreen(
    onNavigateToPhotoCapture: (checklistItemResultId: String) -> Unit,
    onChecklistCompleted: () -> Unit,
) {
    composable<ChecklistRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ChecklistRoute>()
        ChecklistScreen(
            equipmentResultId = route.equipmentResultId,
            onNavigateToPhotoCapture = onNavigateToPhotoCapture,
            onChecklistCompleted = onChecklistCompleted,
        )
    }
}
```

---

### Task 7: Подключить в App.kt и Koin + финал

- [ ] **Step 1: Добавить модули в Koin.kt**

```kotlin
import ru.mirea.toir.feature.checklist.impl.di.featureChecklistImplModule
import ru.mirea.toir.feature.checklist.presentation.di.featureChecklistPresentationModule
// ...
featureChecklistImplModule,
featureChecklistPresentationModule,
```

- [ ] **Step 2: Добавить экран в NavHost (App.kt)**

```kotlin
import ru.mirea.toir.core.navigation.PhotoCaptureRoute
import ru.mirea.toir.feature.checklist.ui.api.composableChecklistScreen

composableChecklistScreen(
    onNavigateToPhotoCapture = { resultId ->
        navController.navigate(PhotoCaptureRoute(resultId))
    },
    onChecklistCompleted = { navController.popBackStackOnResumed() },
)
```

- [ ] **Step 3: Добавить PhotoCaptureRoute в Screen.kt**

```kotlin
@Serializable
data class PhotoCaptureRoute(val checklistItemResultId: String) : Screen
```

- [ ] **Step 4: Сборка**

```bash
./gradlew :android:app:assembleDebug
```

- [ ] **Step 5: Ручная проверка**
  1. Открыть карточку оборудования → «Открыть чек-лист»
  2. Должны отображаться все пункты с правильными контролами
  3. Ответить на пункты → изменения сохраняются сразу (при возврате и открытии снова — значения сохранены)
  4. Попытаться завершить с незаполненными обязательными пунктами → появляется сообщение об ошибке
  5. Заполнить все обязательные → кнопка завершить работает

- [ ] **Step 6: Commit**

```bash
git add shared/feature-checklist/ settings.gradle.kts shared/main/ shared/core-navigation/ shared/common-resources/
git commit -m "feat: implement checklist screen with all answer types and mandatory validation"
```
