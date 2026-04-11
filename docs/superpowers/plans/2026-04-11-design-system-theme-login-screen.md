# Design System Theme + LoginScreen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Привести `ToirTheme` к полному соответствию с `docs/design-system/MASTER.md` и переписать `LoginScreen` по `docs/design-system/pages/auth.md`.

**Architecture:** Все изменения в `shared/common-ui` (тема) изолированы — меняем только `Colors.kt`, `Typography.kt`, `Shapes.kt`, `Fonts.kt`, `Theme.kt` и добавляем `Spacers.kt`. Затем обновляем два demo-экрана (сломаются при переименовании токенов), добавляем иконки, строки и переписываем `LoginScreen`.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform 1.10.1, Material3 1.9.0, moko-resources.

---

## Файлы

| Действие | Путь |
|----------|------|
| Modify | `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Fonts.kt` |
| Modify | `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Colors.kt` |
| Modify | `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Theme.kt` |
| Modify | `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Typography.kt` |
| Modify | `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Shapes.kt` |
| Create | `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/utils/Spacers.kt` |
| Modify | `shared/feature-demo-first/ui/src/commonMain/kotlin/ru/mirea/toir/feature/demo/first/ui/DemoFirstScreen.kt` |
| Modify | `shared/feature-demo-second/ui/src/commonMain/kotlin/ru/mirea/toir/feature/demo/second/ui/DemoSecondScreen.kt` |
| Modify | `gradle/libs.versions.toml` |
| Modify | `shared/feature-auth/ui/build.gradle.kts` |
| Modify | `shared/common-resources/src/commonMain/moko-resources/base/strings.xml` |
| Modify | `shared/feature-auth/ui/src/commonMain/kotlin/ru/mirea/toir/feature/auth/ui/LoginScreen.kt` |

---

## Task 1: fontInter в Fonts.kt

**Files:**
- Modify: `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Fonts.kt`

- [ ] **Step 1: Добавить fontInter**

Заменить содержимое файла:

```kotlin
package ru.mirea.toir.common.ui.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dev.icerock.moko.resources.compose.asFont
import ru.mirea.toir.res.MR

val fontInter: FontFamily
    @Composable get() = FontFamily(
        MR.fonts.Inter_Regular.asFont(weight = FontWeight.Normal).requireNotNull(),
        MR.fonts.Inter_Medium.asFont(weight = FontWeight.Medium).requireNotNull(),
        MR.fonts.Inter_SemiBold.asFont(weight = FontWeight.SemiBold).requireNotNull(),
        MR.fonts.Inter_Bold.asFont(weight = FontWeight.Bold).requireNotNull(),
        MR.fonts.Inter_Thin.asFont(weight = FontWeight.Thin).requireNotNull(),
    )

val fontRoboto: FontFamily
    @Composable get() = FontFamily(
        MR.fonts.roboto_regular.asFont(weight = FontWeight.Normal).requireNotNull(),
        MR.fonts.roboto_medium.asFont(weight = FontWeight.Medium).requireNotNull(),
        MR.fonts.roboto_thin.asFont(weight = FontWeight.Thin).requireNotNull(),
        MR.fonts.roboto_bold.asFont(weight = FontWeight.Bold).requireNotNull(),
    )

private fun Font?.requireNotNull(): Font = requireNotNull(this)
```

> **Примечание:** moko-resources генерирует имена ресурсов из имён файлов. Файл `Inter-Regular.otf` → `MR.fonts.Inter_Regular`. Если после сборки компилятор не находит `Inter_Regular`, проверить реально сгенерированное имя в `build/generated/moko-resources/`.

- [ ] **Step 2: Проверить сборку**

```bash
./gradlew :shared:common-ui:compileKotlinAndroid
```

Ожидание: BUILD SUCCESSFUL

- [ ] **Step 3: Коммит**

```bash
git add shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Fonts.kt
git commit -m "feat(theme): add Inter font family"
```

---

## Task 2: Colors.kt — полный набор DS-токенов + Theme.kt (цвет)

**Files:**
- Modify: `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Colors.kt`
- Modify: `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Theme.kt`

- [ ] **Step 1: Заменить Colors.kt**

```kotlin
package ru.mirea.toir.common.ui.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Suppress("CompositionLocalAllowlist")
internal val LocalToirThemeColors = staticCompositionLocalOf<ToirColorScheme> {
    error("CompositionLocal LocalToirThemeColors was not provided")
}

@Immutable
data class ToirColorScheme(
    // Background
    val background: Color,
    val surface: Color,
    val surface2: Color,
    val surfacePressed: Color,
    // Borders
    val border: Color,
    val borderSubtle: Color,
    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val textOnAccent: Color,
    // CTA
    val ctaPrimary: Color,
    val ctaSecondary: Color,
    // Semantic
    val success: Color,
    val successSubtle: Color,
    val warning: Color,
    val warningSubtle: Color,
    val error: Color,
    val errorSubtle: Color,
    val sync: Color,
    val syncSubtle: Color,
    val destructive: Color,
    // States
    val focusBorder: Color,
    val pressedOverlay: Color,
    val selectedBackground: Color,
)

private val darkColorScheme = ToirColorScheme(
    background = Color(0xFF1A1D22),
    surface = Color(0xFF242830),
    surface2 = Color(0xFF2D3240),
    surfacePressed = Color(0xFF313744),
    border = Color(0xFF3D4455),
    borderSubtle = Color(0xFF2A2F3C),
    textPrimary = Color(0xFFE8EAF0),
    textSecondary = Color(0xFF9499A8),
    textDisabled = Color(0xFF55596A),
    textOnAccent = Color(0xFF1A1D22),
    ctaPrimary = Color(0xFFD8DBE6),
    ctaSecondary = Color(0xFF2D3240),
    success = Color(0xFF3D9E72),
    successSubtle = Color(0xFF1F3D2D),
    warning = Color(0xFFC4872A),
    warningSubtle = Color(0xFF3A2B10),
    error = Color(0xFFB84040),
    errorSubtle = Color(0xFF3A1A1A),
    sync = Color(0xFFB07830),
    syncSubtle = Color(0xFF362410),
    destructive = Color(0xFFB84040),
    focusBorder = Color(0xFF8A90A0),
    pressedOverlay = Color(0x0FFFFFFF),
    selectedBackground = Color(0x1AD8DBE6),
)

// Light theme is a stub — only dark theme is currently designed per DS MASTER.md
internal fun getLightColorScheme(): ToirColorScheme = darkColorScheme

internal fun getDarkColorScheme(): ToirColorScheme = darkColorScheme
```

- [ ] **Step 2: Обновить Theme.kt — ссылку на цвет**

Найти строку в `Theme.kt`:
```kotlin
value = ToirTheme.typography.textMD.copy(color = ToirTheme.colors.black),
```
Заменить на:
```kotlin
value = ToirTheme.typography.textMD.copy(color = ToirTheme.colors.textPrimary),
```

- [ ] **Step 3: Проверить сборку**

```bash
./gradlew :shared:common-ui:compileKotlinAndroid
```

Ожидание: BUILD SUCCESSFUL

- [ ] **Step 4: Коммит**

```bash
git add shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Colors.kt \
        shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Theme.kt
git commit -m "feat(theme): replace color tokens with full DS palette"
```

---

## Task 3: Typography.kt — 7 DS-токенов + Inter + Theme.kt + Demo-экраны

**Files:**
- Modify: `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Typography.kt`
- Modify: `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Theme.kt`
- Modify: `shared/feature-demo-first/ui/src/commonMain/kotlin/ru/mirea/toir/feature/demo/first/ui/DemoFirstScreen.kt`
- Modify: `shared/feature-demo-second/ui/src/commonMain/kotlin/ru/mirea/toir/feature/demo/second/ui/DemoSecondScreen.kt`

Все 4 файла меняются в одном шаге — они взаимозависимы при компиляции.

- [ ] **Step 1: Заменить Typography.kt**

```kotlin
package ru.mirea.toir.common.ui.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Suppress("CompositionLocalAllowlist")
internal val LocalToirThemeTypography = staticCompositionLocalOf<ToirTypography> {
    error("CompositionLocal LocalToirThemeTypography was not provided")
}

@Immutable
class ToirTypography internal constructor(
    val displayLarge: TextStyle,   // 24sp 600 — заголовок экрана
    val displayMedium: TextStyle,  // 20sp 600 — заголовок карточки
    val headline: TextStyle,       // 17sp 600 — подзаголовок секции
    val bodyLarge: TextStyle,      // 16sp 400 — основной текст, поля ввода
    val bodyMedium: TextStyle,     // 14sp 400 — вторичный текст
    val label: TextStyle,          // 13sp 500 — лейблы полей, кнопки
    val caption: TextStyle,        // 12sp 400 — метки, мета
)

@Composable
internal fun defaultTypography(): ToirTypography {
    return ToirTypography(
        displayLarge = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
        ),
        displayMedium = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 27.sp,
        ),
        headline = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = 24.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 21.sp,
        ),
        label = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 17.sp,
        ),
        caption = TextStyle(
            fontFamily = fontInter,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),
    )
}
```

- [ ] **Step 2: Обновить Theme.kt — ссылку на типографику**

Найти строку:
```kotlin
value = ToirTheme.typography.textMD.copy(color = ToirTheme.colors.textPrimary),
```
Заменить на:
```kotlin
value = ToirTheme.typography.bodyMedium.copy(color = ToirTheme.colors.textPrimary),
```

- [ ] **Step 3: Обновить DemoFirstScreen.kt**

Найти:
```kotlin
style = ToirTheme.typography.headlineMD,
```
Заменить на:
```kotlin
style = ToirTheme.typography.displayLarge,
```

- [ ] **Step 4: Обновить DemoSecondScreen.kt**

Найти и заменить все вхождения:
```kotlin
style = ToirTheme.typography.headlineMD,
```
→
```kotlin
style = ToirTheme.typography.displayLarge,
```

Найти и заменить все вхождения:
```kotlin
style = ToirTheme.typography.bodyLG,
```
→
```kotlin
style = ToirTheme.typography.bodyLarge,
```

- [ ] **Step 5: Проверить сборку**

```bash
./gradlew :shared:common-ui:compileKotlinAndroid \
          :shared:feature-demo-first:ui:compileKotlinAndroid \
          :shared:feature-demo-second:ui:compileKotlinAndroid
```

Ожидание: BUILD SUCCESSFUL для всех трёх модулей

- [ ] **Step 6: Коммит**

```bash
git add shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Typography.kt \
        shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Theme.kt \
        shared/feature-demo-first/ui/src/commonMain/kotlin/ru/mirea/toir/feature/demo/first/ui/DemoFirstScreen.kt \
        shared/feature-demo-second/ui/src/commonMain/kotlin/ru/mirea/toir/feature/demo/second/ui/DemoSecondScreen.kt
git commit -m "feat(theme): replace typography tokens with DS scale, switch to Inter"
```

---

## Task 4: Shapes.kt — DS-имена и значения

**Files:**
- Modify: `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Shapes.kt`

- [ ] **Step 1: Заменить Shapes.kt**

```kotlin
package ru.mirea.toir.common.ui.compose.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

@Suppress("CompositionLocalAllowlist")
internal val LocalToirThemeShapes = staticCompositionLocalOf<ToirShapes> {
    error("CompositionLocal LocalToirThemeShapes was not provided")
}

@Immutable
class ToirShapes internal constructor(
    val xs: CornerBasedShape = RoundedCornerShape(4.dp),    // чекбоксы, бейджи
    val sm: CornerBasedShape = RoundedCornerShape(6.dp),    // поля ввода, кнопки
    val md: CornerBasedShape = RoundedCornerShape(10.dp),   // карточки, панели
    val lg: CornerBasedShape = RoundedCornerShape(14.dp),   // модальные окна, bottomsheet
    val pill: CornerBasedShape = RoundedCornerShape(999.dp),// статус-бейджи, переключатели
)
```

- [ ] **Step 2: Проверить сборку**

```bash
./gradlew :shared:common-ui:compileKotlinAndroid
```

Ожидание: BUILD SUCCESSFUL

- [ ] **Step 3: Коммит**

```bash
git add shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Shapes.kt
git commit -m "feat(theme): rename shape tokens to DS names (xs/sm/md/lg/pill)"
```

---

## Task 5: Spacers.kt — новый файл с расширениями RowScope / ColumnScope

**Files:**
- Create: `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/utils/Spacers.kt`

- [ ] **Step 1: Создать файл**

```kotlin
package ru.mirea.toir.common.ui.compose.utils

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ── Row spacers (horizontal) ──────────────────────────────────────────────────

@Composable
fun RowScope.Spacer4(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(4.dp).then(modifier))

@Composable
fun RowScope.Spacer8(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(8.dp).then(modifier))

@Composable
fun RowScope.Spacer12(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(12.dp).then(modifier))

@Composable
fun RowScope.Spacer16(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(16.dp).then(modifier))

@Composable
fun RowScope.Spacer24(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(24.dp).then(modifier))

@Composable
fun RowScope.Spacer32(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(32.dp).then(modifier))

@Composable
fun RowScope.Spacer48(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(48.dp).then(modifier))

// ── Column spacers (vertical) ─────────────────────────────────────────────────

@Composable
fun ColumnScope.Spacer4(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(4.dp).then(modifier))

@Composable
fun ColumnScope.Spacer8(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(8.dp).then(modifier))

@Composable
fun ColumnScope.Spacer12(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(12.dp).then(modifier))

@Composable
fun ColumnScope.Spacer16(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(16.dp).then(modifier))

@Composable
fun ColumnScope.Spacer24(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(24.dp).then(modifier))

@Composable
fun ColumnScope.Spacer32(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(32.dp).then(modifier))

@Composable
fun ColumnScope.Spacer48(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(48.dp).then(modifier))
```

- [ ] **Step 2: Проверить сборку**

```bash
./gradlew :shared:common-ui:compileKotlinAndroid
```

Ожидание: BUILD SUCCESSFUL

- [ ] **Step 3: Коммит**

```bash
git add shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/utils/Spacers.kt
git commit -m "feat(common-ui): add RowScope/ColumnScope Spacer extensions"
```

---

## Task 6: Material Icons Extended + строки для LoginScreen

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `shared/feature-auth/ui/build.gradle.kts`
- Modify: `shared/common-resources/src/commonMain/moko-resources/base/strings.xml`

- [ ] **Step 1: Добавить зависимость в libs.versions.toml**

В блок `[libraries]` добавить после `compose-multiplatform-material`:
```toml
compose-multiplatform-material-icons-extended = { group = "org.jetbrains.compose.material", name = "material-icons-extended", version.ref = "compose-multiplatform" }
```

- [ ] **Step 2: Добавить зависимость в feature-auth/ui/build.gradle.kts**

Заменить:
```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.auth.ui"
}
```

На:
```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "ru.mirea.toir.feature.auth.ui"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.multiplatform.material.icons.extended)
        }
    }
}
```

- [ ] **Step 3: Обновить strings.xml**

Заменить содержимое файла `shared/common-resources/src/commonMain/moko-resources/base/strings.xml`:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<resources>
    <string name="demo_first_title">Demo Feature 1</string>
    <string name="demo_first_number_label">Enter a number</string>
    <string name="demo_first_next_button">Next</string>

    <string name="demo_second_title">Demo Feature 2</string>
    <string name="demo_second_error">Failed to load joke. Try again.</string>
    <string name="demo_second_refresh_button">Refresh</string>
    <string name="demo_second_back_button">Back</string>

    <!-- auth -->
    <string name="auth_title">TOIR</string>
    <string name="auth_subtitle">Система технического обхода</string>
    <string name="auth_login_hint">Логин</string>
    <string name="auth_password_hint">Пароль</string>
    <string name="auth_button_login">Войти</string>
    <string name="auth_forgot_password">Забыли пароль?</string>
    <string name="auth_error_invalid_credentials">Неверный логин или пароль</string>

</resources>
```

- [ ] **Step 4: Проверить сборку**

```bash
./gradlew :shared:feature-auth:ui:compileKotlinAndroid
```

Ожидание: BUILD SUCCESSFUL

- [ ] **Step 5: Коммит**

```bash
git add gradle/libs.versions.toml \
        shared/feature-auth/ui/build.gradle.kts \
        shared/common-resources/src/commonMain/moko-resources/base/strings.xml
git commit -m "feat(auth-ui): add material-icons-extended dep, add auth strings"
```

---

## Task 7: LoginScreen.kt — полный редизайн по auth.md

**Files:**
- Modify: `shared/feature-auth/ui/src/commonMain/kotlin/ru/mirea/toir/feature/auth/ui/LoginScreen.kt`

- [ ] **Step 1: Заменить LoginScreen.kt**

```kotlin
package ru.mirea.toir.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.mirea.toir.common.ui.compose.theme.ToirColorScheme
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.CollectFlow
import ru.mirea.toir.common.ui.compose.utils.Spacer12
import ru.mirea.toir.common.ui.compose.utils.Spacer16
import ru.mirea.toir.common.ui.compose.utils.Spacer24
import ru.mirea.toir.common.ui.compose.utils.Spacer32
import ru.mirea.toir.common.ui.compose.utils.Spacer8
import ru.mirea.toir.feature.auth.presentation.AuthViewModel
import ru.mirea.toir.feature.auth.presentation.models.UiAuthLabel
import ru.mirea.toir.res.MR

@Composable
internal fun LoginScreen(
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    val colors = ToirTheme.colors
    val typography = ToirTheme.typography

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiAuthLabel.NavigateToMain -> onNavigateToMain()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(MR.strings.auth_title),
                style = typography.displayLarge,
                color = colors.textPrimary,
            )
            Spacer8()
            Text(
                text = stringResource(MR.strings.auth_subtitle),
                style = typography.bodyMedium,
                color = colors.textSecondary,
            )
            Spacer32()
            OutlinedTextField(
                value = state.login,
                onValueChange = viewModel::onLoginChange,
                label = { Text(text = stringResource(MR.strings.auth_login_hint)) },
                singleLine = true,
                isError = state.errorMessage != null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                shape = ToirTheme.shapes.sm,
                colors = authTextFieldColors(colors),
            )
            Spacer12()
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(text = stringResource(MR.strings.auth_password_hint)) },
                singleLine = true,
                isError = state.errorMessage != null,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { viewModel.onLoginClick() }),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                shape = ToirTheme.shapes.sm,
                colors = authTextFieldColors(colors),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Outlined.Visibility
                            } else {
                                Icons.Outlined.VisibilityOff
                            },
                            contentDescription = null,
                            tint = colors.textSecondary,
                        )
                    }
                },
            )
            if (state.errorMessage != null) {
                Spacer12()
                Text(
                    text = state.errorMessage.orEmpty(),
                    style = typography.caption,
                    color = colors.error,
                )
            }
            Spacer24()
            Button(
                onClick = viewModel::onLoginClick,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .height(48.dp),
                enabled = !state.isLoading && state.login.isNotBlank() && state.password.isNotBlank(),
                shape = ToirTheme.shapes.sm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.ctaPrimary,
                    contentColor = colors.textOnAccent,
                    disabledContainerColor = colors.ctaPrimary.copy(alpha = 0.5f),
                    disabledContentColor = colors.textOnAccent.copy(alpha = 0.5f),
                ),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.textOnAccent,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(MR.strings.auth_button_login),
                        style = typography.label,
                    )
                }
            }
            Spacer16()
            // TODO: navigate to password recovery when backend API is ready
            Text(
                text = stringResource(MR.strings.auth_forgot_password),
                style = typography.caption,
                color = colors.textSecondary,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

@Composable
private fun authTextFieldColors(colors: ToirColorScheme) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = colors.surface2,
    unfocusedContainerColor = colors.surface2,
    disabledContainerColor = colors.surface2,
    errorContainerColor = colors.surface2,
    focusedBorderColor = colors.focusBorder,
    unfocusedBorderColor = colors.border,
    errorBorderColor = colors.error,
    disabledBorderColor = colors.border.copy(alpha = 0.5f),
    focusedTextColor = colors.textPrimary,
    unfocusedTextColor = colors.textPrimary,
    disabledTextColor = colors.textDisabled,
    errorTextColor = colors.textPrimary,
    focusedLabelColor = colors.textSecondary,
    unfocusedLabelColor = colors.textSecondary,
    disabledLabelColor = colors.textDisabled,
    errorLabelColor = colors.error,
    cursorColor = colors.textPrimary,
    errorCursorColor = colors.error,
)
```

- [ ] **Step 2: Проверить сборку всего проекта**

```bash
./gradlew :android:app:assembleDebug
```

Ожидание: BUILD SUCCESSFUL

- [ ] **Step 3: Коммит**

```bash
git add shared/feature-auth/ui/src/commonMain/kotlin/ru/mirea/toir/feature/auth/ui/LoginScreen.kt
git commit -m "feat(auth-ui): rewrite LoginScreen per DS auth.md spec"
```

---

## Финальная проверка

- [ ] Запустить detekt:

```bash
./gradlew detekt
```

Ожидание: 0 новых нарушений (только ранее существующие предупреждения, если они были).

- [ ] Проверить, что приложение запускается на Android и LoginScreen отображается корректно.
