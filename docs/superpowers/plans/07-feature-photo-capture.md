# Waypoint 07 — Feature Photo Capture (Фотофиксация)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Реализовать экран фотофиксации — съёмка камерой или выбор из галереи, сохранение фото в локальной ФС + БД, привязка к `ChecklistItemResult`.

**Architecture:** Platform-specific реализация через `expect/actual` в `core-camera` (или напрямую в `feature-photo-capture`). Android: `ActivityResultContracts.TakePicture` + `MediaStore`. Фото сохраняется в `context.filesDir/photos/` (Android) или `NSDocumentDirectory` (iOS). `Photo` запись в SQLDelight, `sync_status = PENDING`.

**Tech Stack:** Compose Multiplatform, `expect/actual`, Koin, SQLDelight (PhotoDao), FileSystem.

---

## Файловая структура

```
shared/feature-photo-capture/
├── api/src/commonMain/.../feature/photo/capture/api/
│   └── store/PhotoCaptureStore.kt
├── impl/src/commonMain/.../feature/photo/capture/impl/
│   ├── di/FeaturePhotoCaptureImplModule.kt
│   ├── domain/repository/PhotoCaptureRepository.kt    ← интерфейс в domain
│   ├── data/repository/PhotoCaptureRepositoryImpl.kt
│   └── domain/PhotoCaptureStoreFactory.kt  (+ Executor + Reducer)
├── presentation/src/commonMain/.../feature/photo/capture/presentation/
│   ├── di/FeaturePhotoCapturePresentationModule.kt
│   ├── models/UiPhotoCaptureState.kt
│   ├── models/UiPhotoCaptureLabel.kt
│   ├── mappers/UiPhotoCaptureStateMapper.kt
│   ├── mappers/UiPhotoCaptureLabelMapper.kt
│   └── PhotoCaptureViewModel.kt
└── ui/src/commonMain/.../feature/photo/capture/ui/
    ├── api/FeaturePhotoCaptureScreenApi.kt
    └── PhotoCaptureScreen.kt
        (+ platform-specific composables via expect/actual for camera launcher)
```

---

### Task 1: Регистрация и build-файлы

- [ ] **Step 1: Зарегистрировать в settings.gradle.kts**

```kotlin
include(":shared:feature-photo-capture:api")
include(":shared:feature-photo-capture:impl")
include(":shared:feature-photo-capture:presentation")
include(":shared:feature-photo-capture:ui")
```

- [ ] **Step 2: Создать build.gradle.kts**

`api/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
plugins { alias(libs.plugins.conventionPlugin.kmpFeatureSetup) }
androidLibraryConfig { namespace = "ru.mirea.toir.feature.photo.capture.api" }
```

`impl/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations
plugins { alias(libs.plugins.conventionPlugin.kmpFeatureSetup) }
androidLibraryConfig { namespace = "ru.mirea.toir.feature.photo.capture.impl" }
commonMainDependencies {
    implementations(projects.shared.coreDatabase)
}
```

`presentation/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}
androidLibraryConfig { namespace = "ru.mirea.toir.feature.photo.capture.presentation" }
```

`ui/build.gradle.kts`:
```kotlin
import extensions.androidLibraryConfig
import extensions.androidMainDependencies
import extensions.implementations
plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}
androidLibraryConfig { namespace = "ru.mirea.toir.feature.photo.capture.ui" }
androidMainDependencies {
    implementations(libs.androidx.activity.compose)
}
```

---

### Task 2: Store (api)

**Files:**
- Create: `.../feature/photo/capture/api/store/PhotoCaptureStore.kt`

- [ ] **Step 1: Создать PhotoCaptureStore**

```kotlin
package ru.mirea.toir.feature.photo.capture.api.store

import com.arkivanov.mvikotlin.core.store.Store
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

interface PhotoCaptureStore : Store<PhotoCaptureStore.Intent, PhotoCaptureStore.State, PhotoCaptureStore.Label> {

    data class State(
        val checklistItemResultId: String = "",
        val photos: ImmutableList<String> = persistentListOf(),
        val isLoading: Boolean = false,
    )

    sealed interface Intent {
        data class Init(val checklistItemResultId: String) : Intent
        data class OnPhotoTaken(val fileUri: String) : Intent
        data object OnConfirm : Intent
    }

    sealed interface Label {
        data object PhotoConfirmed : Label
    }
}
```

---

### Task 3: Repository (impl)

**Files:**
- Create: `.../feature/photo/capture/impl/domain/repository/PhotoCaptureRepository.kt`
- Create: `.../feature/photo/capture/impl/data/repository/PhotoCaptureRepositoryImpl.kt`

- [ ] **Step 1: PhotoCaptureRepository**

```kotlin
package ru.mirea.toir.feature.photo.capture.impl.data.repository

internal interface PhotoCaptureRepository {
    suspend fun savePhoto(checklistItemResultId: String, fileUri: String): Result<Unit>
    suspend fun getPhotos(checklistItemResultId: String): Result<List<String>>
}
```

- [ ] **Step 2: PhotoCaptureRepositoryImpl**

```kotlin
package ru.mirea.toir.feature.photo.capture.impl.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.extensions.coRunCatching
import ru.mirea.toir.common.extensions.wrapResultFailure
import ru.mirea.toir.common.extensions.wrapResultSuccess
import ru.mirea.toir.core.database.dao.PhotoDao
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class PhotoCaptureRepositoryImpl(
    private val photoDao: PhotoDao,
    private val coroutineDispatchers: CoroutineDispatchers,
) : PhotoCaptureRepository {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun savePhoto(checklistItemResultId: String, fileUri: String): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    photoDao.insert(
                        id = Uuid.random().toString(),
                        checklistItemResultId = checklistItemResultId,
                        fileUri = fileUri,
                        takenAt = Clock.System.now().toString(),
                    )
                    Unit.wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "savePhoto failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }

    override suspend fun getPhotos(checklistItemResultId: String): Result<List<String>> =
        withContext(coroutineDispatchers.io) {
            coRunCatching(
                tryBlock = {
                    photoDao.selectByChecklistItemResultId(checklistItemResultId)
                        .map { it.file_uri }
                        .wrapResultSuccess()
                },
                catchBlock = { throwable ->
                    Napier.e(message = "getPhotos failed", throwable = throwable)
                    throwable.wrapResultFailure()
                },
            )
        }
}
```

---

### Task 4: Executor, Reducer, StoreFactory, DI (impl)

- [ ] **Step 1: Написать тест для PhotoCaptureReducer**

```kotlin
package ru.mirea.toir.feature.photo.capture.impl.domain

import kotlinx.collections.immutable.persistentListOf
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhotoCaptureReducerTest {
    private val reducer = PhotoCaptureReducer()
    private val initial = PhotoCaptureStore.State()

    @Test
    fun `AddPhoto appends to photos list`() {
        val result = with(reducer) {
            initial.reduce(PhotoCaptureStoreFactory.Message.AddPhoto("file:///test.jpg"))
        }
        assertEquals(1, result.photos.size)
        assertEquals("file:///test.jpg", result.photos.first())
    }

    @Test
    fun `SetLoading sets isLoading`() {
        val result = with(reducer) { initial.reduce(PhotoCaptureStoreFactory.Message.SetLoading(true)) }
        assertTrue(result.isLoading)
    }

    @Test
    fun `SetPhotos replaces photos list`() {
        val withPhotos = initial.copy(photos = persistentListOf("old.jpg"))
        val result = with(reducer) {
            withPhotos.reduce(PhotoCaptureStoreFactory.Message.SetPhotos(persistentListOf("new.jpg")))
        }
        assertEquals(1, result.photos.size)
        assertEquals("new.jpg", result.photos.first())
    }
}
```

- [ ] **Step 2: Запустить тест — убедиться, что падает**

```bash
./gradlew :shared:feature-photo-capture:impl:testDebugUnitTest 2>&1 | tail -5
```

- [ ] **Step 3: PhotoCaptureReducer**

```kotlin
package ru.mirea.toir.feature.photo.capture.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import kotlinx.collections.immutable.toImmutableList
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore

internal class PhotoCaptureReducer : Reducer<PhotoCaptureStore.State, PhotoCaptureStoreFactory.Message> {
    override fun PhotoCaptureStore.State.reduce(msg: PhotoCaptureStoreFactory.Message): PhotoCaptureStore.State = when (msg) {
        is PhotoCaptureStoreFactory.Message.SetLoading -> copy(isLoading = msg.value)
        is PhotoCaptureStoreFactory.Message.SetPhotos -> copy(photos = msg.photos)
        is PhotoCaptureStoreFactory.Message.AddPhoto -> copy(photos = (photos + msg.uri).toImmutableList())
        is PhotoCaptureStoreFactory.Message.SetResultId -> copy(checklistItemResultId = msg.id)
    }
}
```

- [ ] **Step 4: Запустить тест — убедиться, что проходит**

```bash
./gradlew :shared:feature-photo-capture:impl:testDebugUnitTest
```

- [ ] **Step 5: PhotoCaptureExecutor**

```kotlin
package ru.mirea.toir.feature.photo.capture.impl.domain

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.core.mvikotlin.BaseExecutor
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore
import ru.mirea.toir.feature.photo.capture.impl.data.repository.PhotoCaptureRepository

internal class PhotoCaptureExecutor(
    private val repository: PhotoCaptureRepository,
    mainDispatcher: CoroutineDispatcher,
) : BaseExecutor<PhotoCaptureStore.Intent, Nothing, PhotoCaptureStore.State, PhotoCaptureStoreFactory.Message, PhotoCaptureStore.Label>(
    mainContext = mainDispatcher,
) {
    override suspend fun suspendExecuteIntent(intent: PhotoCaptureStore.Intent) {
        when (intent) {
            is PhotoCaptureStore.Intent.Init -> {
                dispatch(PhotoCaptureStoreFactory.Message.SetResultId(intent.checklistItemResultId))
                loadPhotos(intent.checklistItemResultId)
            }
            is PhotoCaptureStore.Intent.OnPhotoTaken -> {
                val resultId = state().checklistItemResultId
                dispatch(PhotoCaptureStoreFactory.Message.SetLoading(true))
                repository.savePhoto(resultId, intent.fileUri).fold(
                    onSuccess = {
                        dispatch(PhotoCaptureStoreFactory.Message.AddPhoto(intent.fileUri))
                        dispatch(PhotoCaptureStoreFactory.Message.SetLoading(false))
                    },
                    onFailure = {
                        dispatch(PhotoCaptureStoreFactory.Message.SetLoading(false))
                    },
                )
            }
            PhotoCaptureStore.Intent.OnConfirm -> publish(PhotoCaptureStore.Label.PhotoConfirmed)
        }
    }

    private suspend fun loadPhotos(checklistItemResultId: String) {
        repository.getPhotos(checklistItemResultId).fold(
            onSuccess = { uris ->
                dispatch(PhotoCaptureStoreFactory.Message.SetPhotos(uris.toImmutableList()))
            },
            onFailure = { /* silent */ },
        )
    }
}
```

- [ ] **Step 6: PhotoCaptureStoreFactory**

```kotlin
package ru.mirea.toir.feature.photo.capture.impl.domain

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore
import ru.mirea.toir.feature.photo.capture.impl.data.repository.PhotoCaptureRepository

internal class PhotoCaptureStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: PhotoCaptureRepository,
    private val mainDispatcher: CoroutineDispatcher,
) {
    fun create(): PhotoCaptureStore =
        object :
            PhotoCaptureStore,
            Store<PhotoCaptureStore.Intent, PhotoCaptureStore.State, PhotoCaptureStore.Label> by storeFactory.create(
                name = PhotoCaptureStore::class.simpleName,
                initialState = PhotoCaptureStore.State(),
                bootstrapper = null,
                executorFactory = { PhotoCaptureExecutor(repository, mainDispatcher) },
                reducer = PhotoCaptureReducer(),
            ) {}

    internal sealed interface Message {
        data class SetLoading(val value: Boolean) : Message
        data class SetPhotos(val photos: ImmutableList<String>) : Message
        data class AddPhoto(val uri: String) : Message
        data class SetResultId(val id: String) : Message
    }
}
```

- [ ] **Step 7: DI-модуль impl**

```kotlin
package ru.mirea.toir.feature.photo.capture.impl.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.feature.photo.capture.impl.data.repository.PhotoCaptureRepository
import ru.mirea.toir.feature.photo.capture.impl.data.repository.PhotoCaptureRepositoryImpl
import ru.mirea.toir.feature.photo.capture.impl.domain.PhotoCaptureStoreFactory

val featurePhotoCaptureImplModule = module {
    factory<PhotoCaptureRepository> { new(::PhotoCaptureRepositoryImpl) }
    factory { new(::PhotoCaptureStoreFactory) }
}
```

---

### Task 5: Presentation-модуль

> По паттерну аналогичен остальным фичам.

- [ ] **Step 1: UiPhotoCaptureState + UiPhotoCaptureLabel**

```kotlin
// UiPhotoCaptureState.kt
package ru.mirea.toir.feature.photo.capture.presentation.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class UiPhotoCaptureState(
    val photos: ImmutableList<String> = persistentListOf(),
    val isLoading: Boolean = false,
)
```

```kotlin
// UiPhotoCaptureLabel.kt
package ru.mirea.toir.feature.photo.capture.presentation.models

sealed interface UiPhotoCaptureLabel {
    data object PhotoConfirmed : UiPhotoCaptureLabel
}
```

- [ ] **Step 2: StateMapper, LabelMapper, ViewModel, DI**

Следуй общему паттерну. `PhotoCaptureViewModel`:
```kotlin
class PhotoCaptureViewModel internal constructor(...) : BaseViewModel<UiPhotoCaptureState, UiPhotoCaptureLabel>(...) {
    fun init(checklistItemResultId: String) = store.accept(PhotoCaptureStore.Intent.Init(checklistItemResultId))
    fun onPhotoTaken(fileUri: String) = store.accept(PhotoCaptureStore.Intent.OnPhotoTaken(fileUri))
    fun onConfirm() = store.accept(PhotoCaptureStore.Intent.OnConfirm)
    override fun onCleared() { super.onCleared(); store.dispose() }
}
```

---

### Task 6: UI-модуль — платформозависимая камера

Ключевая сложность: камера работает по-разному на Android и iOS.

**Подход:** В `PhotoCaptureScreen.kt` создаём `expect fun rememberCameraLauncher(onPhotoTaken: (String) -> Unit): () -> Unit` в `commonMain` и реализуем `actual` в `androidMain` и `iosMain`.

**Files:**
- Create: `ui/src/commonMain/.../feature/photo/capture/ui/CameraLauncher.kt` (expect)
- Create: `ui/src/androidMain/.../feature/photo/capture/ui/CameraLauncher.android.kt` (actual)
- Create: `ui/src/iosMain/.../feature/photo/capture/ui/CameraLauncher.ios.kt` (actual)
- Create: `ui/src/commonMain/.../feature/photo/capture/ui/PhotoCaptureScreen.kt`
- Create: `ui/src/commonMain/.../feature/photo/capture/ui/api/FeaturePhotoCaptureScreenApi.kt`

Добавить строки в strings.xml:
```xml
<!-- photo_capture -->
<string name="photo_capture_title">Фотофиксация</string>
<string name="photo_capture_button_take">Сделать фото</string>
<string name="photo_capture_button_confirm">Готово</string>
```

- [ ] **Step 1: Создать expect CameraLauncher**

`commonMain/.../ui/CameraLauncher.kt`:
```kotlin
package ru.mirea.toir.feature.photo.capture.ui

import androidx.compose.runtime.Composable

@Composable
expect fun rememberCameraLauncher(onPhotoTaken: (uri: String) -> Unit): () -> Unit
```

- [ ] **Step 2: Android actual**

`androidMain/.../ui/CameraLauncher.android.kt`:
```kotlin
package ru.mirea.toir.feature.photo.capture.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

@Composable
actual fun rememberCameraLauncher(onPhotoTaken: (uri: String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnPhotoTaken by rememberUpdatedState(onPhotoTaken)
    val photoUri = remember { createPhotoUri(context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            currentOnPhotoTaken(photoUri.toString())
        }
    }

    return remember(launcher) { { launcher.launch(photoUri) } }
}

private fun createPhotoUri(context: Context): Uri {
    val photosDir = File(context.filesDir, "photos").also { it.mkdirs() }
    val file = File(photosDir, "${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}
```

> ⚠️ Для `FileProvider` нужно добавить в `AndroidManifest.xml` (`android/app`):
> ```xml
> <provider
>     android:name="androidx.core.content.FileProvider"
>     android:authorities="${applicationId}.fileprovider"
>     android:exported="false"
>     android:grantUriPermissions="true">
>     <meta-data
>         android:name="android.support.FILE_PROVIDER_PATHS"
>         android:resource="@xml/file_paths" />
> </provider>
> ```
> И создать `android/app/src/main/res/xml/file_paths.xml`:
> ```xml
> <?xml version="1.0" encoding="utf-8"?>
> <paths>
>     <files-path name="photos" path="photos/" />
> </paths>
> ```
> И добавить разрешение в Manifest: `<uses-permission android:name="android.permission.CAMERA" />`

- [ ] **Step 3: iOS actual (stub — полная реализация через UIImagePickerController)**

`iosMain/.../ui/CameraLauncher.ios.kt`:
```kotlin
package ru.mirea.toir.feature.photo.capture.ui

import androidx.compose.runtime.Composable

@Composable
actual fun rememberCameraLauncher(onPhotoTaken: (uri: String) -> Unit): () -> Unit {
    // TODO: implement using UIImagePickerController via interop
    // For MVP: return no-op and implement in Waypoint post-MVP for iOS
    return {}
}
```

- [ ] **Step 4: PhotoCaptureScreen**

```kotlin
package ru.mirea.toir.feature.photo.capture.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.ext.CollectFlow
import ru.mirea.toir.feature.photo.capture.presentation.PhotoCaptureViewModel
import ru.mirea.toir.feature.photo.capture.presentation.models.UiPhotoCaptureLabel
import ru.mirea.toir.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoCaptureScreen(
    checklistItemResultId: String,
    onPhotoConfirmed: () -> Unit,
    viewModel: PhotoCaptureViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(checklistItemResultId) {
        viewModel.init(checklistItemResultId)
    }

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiPhotoCaptureLabel.PhotoConfirmed -> onPhotoConfirmed()
        }
    }

    val cameraLauncher = rememberCameraLauncher(onPhotoTaken = viewModel::onPhotoTaken)

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(MR.strings.photo_capture_title)) }) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.photos.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    items(items = state.photos, key = { it }) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                        )
                    }
                }
            }

            Button(
                onClick = cameraLauncher,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(MR.strings.photo_capture_button_take))
            }

            Button(
                onClick = viewModel::onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.photos.isNotEmpty(),
            ) {
                Text(text = stringResource(MR.strings.photo_capture_button_confirm))
            }
        }
    }
}
```

> ⚠️ `AsyncImage` из Coil — проверь, что Coil 3.x для Compose Multiplatform добавлен в `common-ui` или `feature-photo-capture/ui`. Если нет — добавь в `libs.versions.toml`:
> ```toml
> coil = "3.0.4"
> coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
> ```
> Альтернативно используй `Image(painter = rememberAsyncImagePainter(uri))` или просто `Image` с `BitmapPainter` из File.

- [ ] **Step 5: FeaturePhotoCaptureScreenApi**

```kotlin
package ru.mirea.toir.feature.photo.capture.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ru.mirea.toir.core.navigation.PhotoCaptureRoute
import ru.mirea.toir.feature.photo.capture.ui.PhotoCaptureScreen

fun NavGraphBuilder.composablePhotoCaptureScreen(
    onPhotoConfirmed: () -> Unit,
) {
    composable<PhotoCaptureRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<PhotoCaptureRoute>()
        PhotoCaptureScreen(
            checklistItemResultId = route.checklistItemResultId,
            onPhotoConfirmed = onPhotoConfirmed,
        )
    }
}
```

---

### Task 7: Подключить в App.kt и Koin + финальная проверка

- [ ] **Step 1: Добавить модули в Koin.kt**

```kotlin
import ru.mirea.toir.feature.photo.capture.impl.di.featurePhotoCaptureImplModule
import ru.mirea.toir.feature.photo.capture.presentation.di.featurePhotoCapturePresentationModule
// ...
featurePhotoCaptureImplModule,
featurePhotoCapturePresentationModule,
```

- [ ] **Step 2: Добавить экран в NavHost (App.kt)**

```kotlin
import ru.mirea.toir.feature.photo.capture.ui.api.composablePhotoCaptureScreen

composablePhotoCaptureScreen(
    onPhotoConfirmed = { navController.popBackStackOnResumed() },
)
```

- [ ] **Step 3: Добавить FileProvider в AndroidManifest.xml**

В `android/app/src/main/AndroidManifest.xml` в `<application>`:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
<uses-permission android:name="android.permission.CAMERA" />
```

Создать `android/app/src/main/res/xml/file_paths.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path name="photos" path="photos/" />
</paths>
```

- [ ] **Step 4: Сборка**

```bash
./gradlew :android:app:assembleDebug
```
Ожидаемый результат: `BUILD SUCCESSFUL`

- [ ] **Step 5: Ручная проверка**
  1. В чек-листе нажать «Добавить фото» → переход на экран фотофиксации
  2. Кнопка «Сделать фото» открывает системную камеру
  3. Сделанное фото появляется в превью
  4. Кнопка «Готово» становится активной
  5. Нажатие «Готово» → возврат в чек-лист, счётчик фото обновился

- [ ] **Step 6: Commit**

```bash
git add shared/feature-photo-capture/ android/app/src/main/ settings.gradle.kts shared/main/ shared/common-resources/ gradle/libs.versions.toml
git commit -m "feat: implement photo capture screen with camera support and local storage"
```
