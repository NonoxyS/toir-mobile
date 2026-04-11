# Design Spec: ToirTheme Full DS Tokens + LoginScreen Redesign

**Date:** 2026-04-11  
**Scope:** `shared/common-ui` (theme), `shared/feature-auth/ui` (LoginScreen)

---

## 1. Мотивация

`ToirColorScheme` содержит только `white`/`black`. `ToirTypography` — 4 токена с именами, не совпадающими с ДС. `ToirShapes` — числовые имена с неправильными значениями. `LoginScreen` использует `MaterialTheme` напрямую вместо `ToirTheme`. Цель — привести тему в полное соответствие с `docs/design-system/MASTER.md` и переписать `LoginScreen` по `docs/design-system/pages/auth.md`.

---

## 2. Изменения в теме (`shared/common-ui`)

### 2.1 Fonts.kt

Добавить `fontInter` рядом с `fontRoboto`. Веса из `MR.fonts`:
- `inter_regular` → `FontWeight.Normal`
- `inter_medium` → `FontWeight.Medium`
- `inter_semibold` → `FontWeight.SemiBold`
- `inter_bold` → `FontWeight.Bold`
- `inter_thin` → `FontWeight.Thin`

### 2.2 Colors.kt

Заменить `ToirColorScheme(white, black)` на полный набор DS-токенов. Все цвета — константы в Kotlin (не через `MR.colors`), как показано в MASTER.md §14. Удалить `white`/`black` из `MR.colors` не нужно — оставляем ресурсы, просто перестаём их использовать в теме.

```
ToirColorScheme:
  // Background
  background: #1A1D22
  surface: #242830
  surface2: #2D3240
  surfacePressed: #313744
  // Borders
  border: #3D4455
  borderSubtle: #2A2F3C
  // Text
  textPrimary: #E8EAF0
  textSecondary: #9499A8
  textDisabled: #55596A
  textOnAccent: #1A1D22
  // CTA
  ctaPrimary: #D8DBE6
  ctaSecondary: #2D3240
  // Semantic
  success: #3D9E72
  successSubtle: #1F3D2D
  warning: #C4872A
  warningSubtle: #3A2B10
  error: #B84040
  errorSubtle: #3A1A1A
  sync: #B07830
  syncSubtle: #362410
  destructive: #B84040
  // States
  focusBorder: #8A90A0
  pressedOverlay: rgba(255,255,255,0.06) — применяется как Color(0x0FFFFFFF)
  selectedBackground: rgba(216,219,230,0.10) — применяется как Color(0x1AD8DBE6)
```

`getLightColorScheme()` и `getDarkColorScheme()` возвращают одинаковое (только тёмная тема по ДС). Light-вариант оставить как stub для будущего.

### 2.3 Typography.kt

Удалить 4 старых токена, добавить 7 DS-токенов. Шрифт — `fontInter`.

| Токен | Размер | LineHeight | Weight |
|-------|--------|------------|--------|
| `displayLarge` | 24sp | 32sp | SemiBold (600) |
| `displayMedium` | 20sp | 27sp | SemiBold (600) |
| `headline` | 17sp | 24sp | SemiBold (600) |
| `bodyLarge` | 16sp | 24sp | Normal (400) |
| `bodyMedium` | 14sp | 21sp | Normal (400) |
| `label` | 13sp | 17sp | Medium (500) |
| `caption` | 12sp | 16sp | Normal (400) |

### 2.4 Shapes.kt

Заменить числовые имена на DS-имена, исправить значения:

| Токен | Значение |
|-------|----------|
| `xs` | 4dp |
| `sm` | 6dp |
| `md` | 10dp |
| `lg` | 14dp |
| `pill` | 999dp |

### 2.5 Theme.kt

`ProvideTextStyle` обновить: `textMD` → `bodyMedium`, `colors.black` → `colors.textPrimary`.

### 2.6 Spacers.kt (новый файл)

Расположение: `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/utils/Spacers.kt`

`RowScope` — `Modifier.width(N.dp)`, `ColumnScope` — `Modifier.height(N.dp)`:

```kotlin
// Sizes: 4, 8, 12, 16, 24, 32, 48
@Composable fun RowScope.Spacer4(modifier: Modifier = Modifier)
@Composable fun RowScope.Spacer8(modifier: Modifier = Modifier)
// ...и т.д.
@Composable fun ColumnScope.Spacer4(modifier: Modifier = Modifier)
// ...и т.д.
```

---

## 3. Обновление demo-экранов

| Файл | Старый токен | Новый токен |
|------|-------------|-------------|
| `DemoFirstScreen.kt` | `typography.headlineMD` | `typography.displayLarge` |
| `DemoSecondScreen.kt` | `typography.headlineMD` | `typography.displayLarge` |
| `DemoSecondScreen.kt` | `typography.bodyLG` | `typography.bodyLarge` |

---

## 4. LoginScreen — полный редизайн

**Источник:** `docs/design-system/pages/auth.md`

### 4.1 Layout

```
Box(fillMaxSize, contentAlignment = Center) {
  Column(
    verticalArrangement = spacedBy(0.dp), // отступы через Spacer
    horizontalAlignment = CenterHorizontally,
    horizontalPadding = 16.dp  // ← ДС: экранный padding 16dp (не 32dp как сейчас)
  ) {
    Text "TOIR"         // displayLarge, textPrimary
    Spacer8
    Text "Система технического обхода"  // bodyMedium, textSecondary
    Spacer32
    ToirTextField login  // label над полем, стандарт из MASTER.md
    Spacer12
    ToirTextField password + eye icon toggle
    Spacer12
    [ErrorText если errorMessage != null]  // caption, error
    Spacer24
    Button "Войти"  // max 280dp, center, height 48dp
    Spacer16
    Text "Забыли пароль?"  // caption, textSecondary, TextDecoration.Underline
  }
}
```

### 4.2 TextField-стиль

Использовать `OutlinedTextField` с кастомными цветами через `OutlinedTextFieldDefaults.colors(...)`:
- `containerColor` = `colors.surface2`
- `unfocusedBorderColor` = `colors.border`
- `focusedBorderColor` = `colors.focusBorder`
- `errorBorderColor` = `colors.error`
- `unfocusedLabelColor` = `colors.textSecondary`
- `focusedLabelColor` = `colors.textSecondary`
- `cursorColor` = `colors.textPrimary`
- `unfocusedTextColor` / `focusedTextColor` = `colors.textPrimary`

Параметр `isError = state.errorMessage != null` — оба поля получают красную рамку при ошибке.

`shape = ToirTheme.shapes.sm` (6dp)

### 4.3 Поле пароля

Добавить `trailingIcon` — иконка `Icons.Outlined.Visibility` / `VisibilityOff`, `color.textSecondary`. `visualTransformation` переключается по локальному `var passwordVisible by remember`.

### 4.4 Кнопка

```kotlin
Button(
  modifier = Modifier.widthIn(max = 280.dp).height(48.dp),
  shape = ToirTheme.shapes.sm,
  colors = ButtonDefaults.buttonColors(
    containerColor = colors.ctaPrimary,
    contentColor = colors.textOnAccent,
    disabledContainerColor = colors.ctaPrimary.copy(alpha = 0.5f),
    disabledContentColor = colors.textOnAccent.copy(alpha = 0.5f),
  )
)
```

Loading: `CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colors.textOnAccent, strokeWidth = 2.dp)`

### 4.5 Scaffold

Убрать `Scaffold` — экран авторизации без `TopAppBar`, фон `colors.background` задаётся через `Box` с `backgroundColor`.

### 4.6 Ограничения текущей реализации

`UiAuthState.errorMessage: String?` не различает тип ошибки (credentials / network / server). Все ошибки показываются одинаково: `caption`, `color.error`, оба поля с красной рамкой. Дифференциация по типу ошибки — отдельный scope (изменение state-модели + executor).

---

## 5. Что НЕ входит в scope

- Изменение `UiAuthState` / executor для типизированных ошибок
- Экран "Забыли пароль?" (ссылка добавляется как UI-элемент без обработчика навигации — TODO в комментарии)
- Spacing tokens в `ToirTheme`
- Реализация light-темы (stub остаётся)
