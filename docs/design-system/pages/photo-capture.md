# Page Override: Фотофиксация

> Переопределяет MASTER.md для экрана `PhotoCaptureScreen` — захвата фото к пункту
> чек-листа (например, фотофиксация дефекта).

## Специфика экрана

Полноэкранный flow для съёмки 1..N фотографий, прикрепляемых к одному пункту
чек-листа. Открывается из чек-листа по тапу «Добавить фото», возвращает массив
URI после «Подтвердить».

## Layout

```
┌─ Top App Bar ────────────────────────────────┐
│ ← Фотофиксация · 2 / 5                       │  ← счётчик прогресса
└──────────────────────────────────────────────┘

  ┌─ Лента фото (LazyRow, если photos.isNotEmpty) ┐
  │  ┌────┐  ┌────┐                                │
  │  │    │  │    │                                │  ← 120×120dp тайлы
  │  │ ▦  │  │ ▦  │                                │     radius.md, gap 8dp
  │  └────┘  └────┘                                │
  └────────────────────────────────────────────────┘

  ─── Empty state (если photos.isEmpty) ─────────────
        [иконка camera_alt 48dp, textDisabled]
              Нет фотографий
        Снимите первое фото для подтверждения

┌─ Sticky Footer ──────────────────────────────┐
│  [   Снять фото   ]   ← Secondary, 48dp     │
│  [  Подтвердить   ]   ← Primary, 48dp       │
└──────────────────────────────────────────────┘
```

## Top App Bar

- Стандартный (§8.7), фон `color.background`.
- Заголовок: «Фотофиксация · X / N» (X — текущее количество, N — `maxPhotos`).
  `type.displayMedium`, `color.textPrimary`.
- Если лимит не задан — заголовок «Фотофиксация» без счётчика.
- Иконка «назад» слева (vector, 24dp).
  - Если `photos.isEmpty()` → обычный pop.
  - Если `photos.isNotEmpty()` → `AlertDialog` (см. «Подтверждение выхода»).
- Иконок справа нет.

## Лента фото (PhotoCaptureGrid, §8.14)

- Реализация: `LazyRow`, `horizontalArrangement = Arrangement.spacedBy(8.dp)`,
  `contentPadding = PaddingValues(horizontal = 16.dp)`.
- Высота фиксированная **128dp** (120dp тайл + 4dp вертикальный воздух сверху/снизу).
- Отступ сверху от App Bar: `spacing.md` (16dp).

### Тайл фото

- Размер: **120×120dp**, `clip(ToirTheme.shapes.md)` (радиус 10dp).
- `AsyncImage` (Coil3 / `coil-compose`), `ContentScale.Crop`.
- Placeholder при загрузке: фон `color.surface2` + `CircularProgressIndicator` 20dp в центре.
- Error state (битая ссылка): фон `color.surface2` + иконка `broken_image` 24dp `color.textDisabled`.
- Border: 1dp `color.borderSubtle` (тонкий, чтобы тайл не сливался с background при
  тёмном фото).

## Empty state (`photos.isEmpty()`)

- Заменяет ленту (выводится **вместо** неё, не поверх).
- Column, `verticalArrangement = Arrangement.Center`, центр доступного пространства.
- Иконка `camera_alt` (vector, 48dp), `color.textDisabled`.
- gap `spacing.sm` (12dp).
- Заголовок «Нет фотографий» — `type.bodyLarge`, `color.textPrimary`.
- gap 4dp.
- Подзаголовок «Снимите первое фото для подтверждения» — `type.bodyMedium`,
  `color.textSecondary`.

## Взаимодействие с тайлом

### Tap → full-screen preview

- Открывает full-screen Composable (внутри того же NavGraph через
  `Dialog(properties = DialogProperties(usePlatformDefaultWidth = false))` или
  отдельный route).
- Фон: `color.background` (полная непрозрачность, не scrim — фото техник должен
  изучать без отвлекающего фона).
- Top App Bar внутри preview:
  - Фон `color.background`.
  - Иконка `close` (vector, 24dp) слева → закрывает preview.
  - Заголовок «Фото X из N» (`type.displayMedium`).
- Контент: `AsyncImage`, `ContentScale.Fit`, max-width = ширина экрана,
  max-height = высота экрана минус App Bar.
- Tap по фото — без действия (резерв для будущих UI-элементов).
- Системный back / swipe-back — закрывает preview.

#### Pinch-to-zoom

- `Modifier.transformable(state = rememberTransformableState { zoomChange, panChange, _ -> })`.
- Диапазон zoom: **1.0× → 4.0×** (clamped).
- Pan активен только при `zoom > 1.0×`, ограничен границами увеличенного фото
  (фото не уезжает за пределы экрана).
- **Double-tap** — toggle между 1.0× (fit) и 2.5×, анимировано через
  `animateFloatAsState`, 200ms `FastOutSlowInEasing`.
- При закрытии preview zoom сбрасывается на 1.0×.
- Уважает `isReduceMotionEnabled`: double-tap zoom без анимации (мгновенный snap).

#### Shared element transition

- Тайл в ленте → full-screen preview через `SharedTransitionLayout` +
  `Modifier.sharedElement(state = rememberSharedContentState(key = photoUri), ...)`
  (Compose Multiplatform 1.7+).
- `key` для shared element — **URI фото** (стабильный идентификатор, чтобы Compose
  правильно сматчил элементы между screens).
- Параметры: длительность **250ms**, easing `FastOutSlowInEasing`.
- При открытии: тайл «вырастает» из своей позиции в ленте до full-screen, фон
  preview одновременно fade-in от прозрачного к `color.background`.
- При закрытии: обратный transition, фон fade-out.
- Если preview был открыт со скроллом ленты (тайл скрыт за горизонтальным
  scroll-offset) — лента скроллит к нужному тайлу **до** старта закрывающего
  transition, иначе элемент схлопывается «в никуда».
- **Уважает `isReduceMotionEnabled`**: shared transition заменяется обычным
  fade-in/out 200ms.

### Long-press → удаление

- Системный `AlertDialog`:
  - Заголовок: «Удалить фото?»
  - Описание: «Это действие нельзя отменить.» (`type.caption`, `color.textSecondary`).
- Кнопки:
  - «Отмена» — Ghost, слева.
  - «Удалить» — **Destructive** (фон `color.errorSubtle`, текст `color.error`,
    border 1dp `color.error`), справа.
- При подтверждении — фото удаляется из state, лента обновляется анимированно
  (`AnimatedVisibility(exit = fadeOut() + shrinkHorizontally())`, 200ms).
- Фокус screen reader'а после удаления возвращается на следующий тайл, или, если
  был последним, на «Снять фото».

## Sticky Footer (`bottomBar` Scaffold)

- Фон `color.background`, border-top 1dp `color.borderSubtle` (опционально, scroll
  здесь нет — можно опустить).
- Padding: `horizontal = 16.dp`, **`vertical = 16.dp`** поверх bottom safe area.
- Кнопки в Column, gap `spacing.xs` (8dp).

### Кнопка «Снять фото»

- Variant: **Secondary**, full-width, height 48dp.
- Иконка `camera_alt` (vector через `MR.images.ic_camera_alt`), 20dp,
  `color.textPrimary`. **Никаких эмодзи.**
- gap между иконкой и текстом 8dp.
- **Disabled** если `isCapturing == true` (камера сейчас открыта).
- **Disabled** если `photos.size >= maxPhotos`. Под кнопкой — caption
  «Достигнут лимит фотографий» (`type.caption`, `color.textSecondary`,
  центрировано, gap 4dp).

### Кнопка «Подтвердить»

- Variant: **Primary**, full-width, height 48dp.
- **Disabled** если `photos.isEmpty()`. Под кнопкой — caption
  «Снимите хотя бы одно фото» (`type.caption`, `color.textSecondary`).
- Тап → возврат на чек-лист с массивом URI (через NavController result или
  shared store).

## Лимиты

- `maxPhotos` приходит из конфига чек-лист-айтема (поле `photoMaxCount`).
- Дефолт: **5**.
- Если поле не задано (`null`) — лимит не показывается в заголовке, кнопка
  «Снять фото» не disabled-ится по лимиту.

## Запуск камеры

- Тап «Снять фото» → `CameraLauncher` (платформенно):
  - Android: `ActivityResultContracts.TakePicture` + `FileProvider` URI.
  - iOS: `UIImagePickerController` или `AVFoundation` (через `expect/actual`).
- При запуске — `isCapturing = true`, обе footer-кнопки disabled.
- Возврат:
  - **Успех** → URI добавляется в state, тайл появляется в ленте с fade-in (200ms).
  - **Отмена** пользователем (back из камеры) → ничего не происходит,
    `isCapturing = false`.
  - **Ошибка** (нет permissions) → SnackBar «Нет доступа к камере. Откройте
    настройки.» (`color.error`) + action «Настройки» (открывает системные
    настройки приложения).
  - **Ошибка** (нет места) → SnackBar «Недостаточно места на устройстве»
    (`color.error`).

## Подтверждение выхода с несохранёнными фото

При тапе «назад» с `photos.isNotEmpty()`:

- `AlertDialog`:
  - Заголовок: «Отменить фотофиксацию?»
  - Описание: «Снятые фото будут удалены и не привяжутся к пункту чек-листа.»
- Кнопки:
  - «Продолжить съёмку» — Ghost, слева. Закрывает диалог, остаётся на экране.
  - «Удалить» — **Destructive**, справа. Очищает state, выходит на чек-лист
    без передачи URI.

## Состояния

- **Loading state** не нужен — экран открывается мгновенно (state приходит из
  параметров навигации: `maxPhotos`, начальный список фото).
- **Error state** на уровне экрана — нет (ошибки handled на уровне отдельных
  операций через SnackBar).

## Доступность

- Тайлы фото:
  - `accessibilityLabel = "Фото X из N. Двойной тап для просмотра, длинный тап для удаления."`
  - `accessibilityRole = image`.
- Empty state — заголовок «Нет фотографий» прочитывается фокусом первым.
- Footer кнопки — стандартный `accessibilityRole = button`, label из текста.
- При удалении — фокус возвращается логично (см. секцию Long-press).
- Preview pinch-to-zoom — есть текстовая альтернатива? Нет, для visual-only задачи
  это допустимо. Screen reader пользователи могут использовать системные жесты
  системного увеличения.

## Навигация

- Открытие из чек-листа: forward navigation, передаются `itemId`, `existingPhotos`,
  `maxPhotos`.
- «Подтвердить» → возврат с результатом (массив URI), state в чек-листе обновляется.
- «Назад» (с пустыми фото) → обычный pop без диалога.
- Preview — child route (через `dialogRoute` или вложенный composable),
  закрытие через свой close-action.
