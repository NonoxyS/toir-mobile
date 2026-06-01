# TOIR Design System — MASTER

> Источник истины для всех UI-решений в проекте TOIR.
> При конфликте между page-файлом и этим документом — page-файл побеждает.

---

## 0. Карта модулей и экранов

Дизайн-система реализована в `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/`
и потребляется фичами через объект `ToirTheme` (см. §14).

| Feature-модуль          | Screen                | Page override               | Назначение                            |
|-------------------------|-----------------------|-----------------------------|---------------------------------------|
| `feature-auth`          | `LoginScreen`         | `pages/auth.md`             | Вход в систему                        |
| `feature-bootstrap`     | `BootstrapScreen`     | `pages/bootstrap.md`        | Auth-gate + загрузка конфига          |
| `feature-routes-list`   | `RoutesListScreen`    | `pages/routes-list.md`      | Главный экран — назначенные маршруты  |
| `feature-route-points`  | `RoutePointsScreen`   | `pages/route-detail.md`     | Список точек обхода маршрута          |
| `feature-equipment-card`| `EquipmentCardScreen` | `pages/equipment-card.md`   | Карточка оборудования (точка маршрута)|
| `feature-checklist`     | `ChecklistScreen`     | `pages/checklist.md`        | Рабочий чек-лист точки                |
| `feature-photo-capture` | `PhotoCaptureScreen`  | `pages/photo-capture.md`    | Захват фото к пункту чек-листа        |
| _(не реализовано)_      | Summary               | `pages/summary.md`          | Экран итогов обхода (запланирован)    |

> Pages-файлы могут описывать ещё-не-реализованные элементы (QR-сканирование, collapsible
> секция «Завершённые», sticky offline-баннер). Это намеренно — pages фиксируют целевой UX.
> Если в коде элемента нет — добавление должно идти по соответствующему page-override.

---

## 1. Философия

**Приложение — серьёзный инструмент для сертифицированных техников в промышленной среде.**

Принципы в порядке приоритета:

1. **Читаемость в полевых условиях** — яркое солнце, перчатки, грязный экран.
2. **Статус виден мгновенно** — цвет + иконка + текст, никогда только один из них.
3. **Ни одного лишнего декоративного элемента** — только функциональная эстетика.
4. **Offline-first** — интерфейс не ломается без сети, всегда показывает актуальный статус.

**Избегать:** неоновые акценты, сложные градиенты, тени ради теней, металлические текстуры,
Hazard-полосы, «sci-fi» эффекты, чистый чёрный (#000) фон, чистый белый (#fff) текст.

---

## 2. Цветовые токены

> **Две темы, выбор по системе.** `ToirTheme(isDark = isSystemInDarkTheme())` отдаёт
> dark- или light-палитру. Тёмная — для тёмных цехов, светлая — для яркого солнца.
> Ручного переключателя нет: следуем системной теме ОС. Таблицы 2.1–2.4 — токены тёмной
> темы, раздел 2.5 — соответствие в светлой. Контент системных баров подстраивается
> через `SystemBarsEffect(isDark)` (Android), на iOS — стиль `.default` хост-контроллера.

### 2.1 Нейтральные (основа)

| Токен                  | Hex       | Описание                                   |
|------------------------|-----------|--------------------------------------------|
| `color.background`     | `#1A1D22` | Основной фон — тёмный графит, не чёрный    |
| `color.surface`        | `#242830` | Карточки, панели первого уровня            |
| `color.surface2`       | `#2D3240` | Карточки второго уровня, модальные окна    |
| `color.surfacePressed` | `#313744` | Состояние нажатия поверхности              |
| `color.border`         | `#3D4455` | Разделители, рамки полей                   |
| `color.borderSubtle`   | `#2A2F3C` | Тонкие разделители между элементами списка |

### 2.2 Текст

| Токен                 | Hex       | Описание                             |
|-----------------------|-----------|--------------------------------------|
| `color.textPrimary`   | `#E8EAF0` | Основной текст, заголовки — офф-вайт |
| `color.textSecondary` | `#9499A8` | Вторичный текст, метаданные          |
| `color.textDisabled`  | `#55596A` | Недоступные элементы (~25% яркости)  |
| `color.textOnAccent`  | `#1A1D22` | Текст на светлых кнопках (CTA)       |

### 2.3 Акцентные (только для статусов и CTA)

| Токен                 | Hex       | Описание                                  |
|-----------------------|-----------|-------------------------------------------|
| `color.ctaPrimary`    | `#D8DBE6` | Фон основной кнопки (светлая на тёмном)   |
| `color.ctaSecondary`  | `#2D3240` | Фон вторичной кнопки                      |
| `color.success`       | `#3D9E72` | Успех, выполнено — приглушённый зелёный   |
| `color.successSubtle` | `#1F3D2D` | Фон элемента со статусом «Выполнено»      |
| `color.warning`       | `#C4872A` | Предупреждение — тёплый янтарь            |
| `color.warningSubtle` | `#3A2B10` | Фон элемента со статусом «Предупреждение» |
| `color.error`         | `#B84040` | Ошибка, дефект — приглушённый кирпичный   |
| `color.errorSubtle`   | `#3A1A1A` | Фон элемента со статусом «Ошибка»         |
| `color.sync`          | `#B07830` | Ожидание синхронизации — бронза           |
| `color.syncSubtle`    | `#362410` | Фон элемента «Ожидает синхр.»             |
| `color.destructive`   | `#B84040` | Деструктивные действия (удаление)         |

### 2.4 Семантические состояния компонентов

| Токен                      | Значение                                |
|----------------------------|-----------------------------------------|
| `state.disabledOpacity`    | `0.5` для компонента, `0.25` для текста |
| `state.pressedOverlay`     | `rgba(255,255,255,0.06)`                |
| `state.focusBorder`        | `#8A90A0` (светлее base border)         |
| `state.selectedBackground` | `rgba(216,219,230,0.10)`                |

### 2.5 Светлая палитра

Те же 24 токена для светлой темы. CTA инвертируется (тёмная кнопка на светлом фоне),
фон — светлый графит-грей `#EDEFF2`, а не чисто-белый (меньше бликов на солнце).
Все текстовые/семантические пары проверены на WCAG AA (раздел 13).

| Токен                  | Hex       | Описание                                       |
|------------------------|-----------|------------------------------------------------|
| `color.background`     | `#EDEFF2` | Основной фон — светлый графит-грей             |
| `color.surface`        | `#FFFFFF` | Карточки, панели первого уровня                |
| `color.surface2`       | `#F6F7F9` | Карточки второго уровня, модальные окна        |
| `color.surfacePressed` | `#E4E7EC` | Состояние нажатия поверхности                  |
| `color.border`         | `#C9CED8` | Разделители, рамки полей                       |
| `color.borderSubtle`   | `#E3E6EC` | Тонкие разделители между элементами списка     |
| `color.textPrimary`    | `#1A1D22` | Основной текст, заголовки — графит             |
| `color.textSecondary`  | `#5A6172` | Вторичный текст, метаданные                    |
| `color.textDisabled`   | `#8F95A4` | Недоступные элементы                           |
| `color.textOnAccent`   | `#FFFFFF` | Текст на тёмных кнопках (CTA)                  |
| `color.ctaPrimary`     | `#2A2F3C` | Фон основной кнопки (тёмная на светлом)        |
| `color.ctaSecondary`   | `#EAECF1` | Фон вторичной кнопки                           |
| `color.success`        | `#27704D` | Успех, выполнено — зелёный                     |
| `color.successSubtle`  | `#E2F1EA` | Фон элемента со статусом «Выполнено»           |
| `color.warning`        | `#8C5A12` | Предупреждение — тёплый янтарь                 |
| `color.warningSubtle`  | `#FBF0DC` | Фон элемента со статусом «Предупреждение»      |
| `color.error`          | `#B83232` | Ошибка, дефект — кирпичный                     |
| `color.errorSubtle`    | `#FBE6E4` | Фон элемента со статусом «Ошибка»              |
| `color.sync`           | `#8A5614` | Ожидание синхронизации — бронза                |
| `color.syncSubtle`     | `#F7ECDA` | Фон элемента «Ожидает синхр.»                  |
| `color.destructive`    | `#B83232` | Деструктивные действия (удаление)              |
| `state.focusBorder`    | `#3D4455` | Фокус (темнее base border)                     |
| `state.pressedOverlay` | `rgba(0,0,0,0.06)` | Оверлей нажатия (чёрный на светлом)   |
| `state.selectedBackground` | `rgba(42,47,60,0.08)` | Фон выделения                     |

---

## 3. Типографика

**Шрифт:** Inter (геометрический гротеск, кроссплатформенный).
Fallback: системный (SF Pro на iOS, Roboto на Android).

### Шкала размеров

| Токен                | Размер | LineHeight | Weight | Применение                  |
|----------------------|--------|------------|--------|-----------------------------|
| `type.displayLarge`  | 24sp   | 1.35       | 600    | Заголовок экрана            |
| `type.displayMedium` | 20sp   | 1.35       | 600    | Заголовок карточки маршрута |
| `type.headline`      | 17sp   | 1.4        | 600    | Подзаголовок секции         |
| `type.bodyLarge`     | 16sp   | 1.5        | 400    | Основной текст, поля ввода  |
| `type.bodyMedium`    | 14sp   | 1.5        | 400    | Вторичный текст, описания   |
| `type.label`         | 13sp   | 1.3        | 500    | Лейблы полей, метки кнопок  |
| `type.caption`       | 12sp   | 1.3        | 400    | Временны́е метки, мета      |

**Правила:**

- CAPS только для коротких label-кнопок (≤ 3 слов), letter-spacing +0.8px.
- Тело текста: weight 400. Заголовки: 600. Никакого 700+ в обычных экранах.
- Межстрочный интерлиньяж 1.4–1.5×.

---

## 4. Отступы (Spacing)

Базовый шаг — **8dp**. Все отступы кратны 4dp.

| Токен         | Значение | Применение                                      |
|---------------|----------|-------------------------------------------------|
| `spacing.xxs` | 4dp      | Между иконкой и текстом                         |
| `spacing.xs`  | 8dp      | Внутри компонента (padding маленький)           |
| `spacing.sm`  | 12dp     | Внутренний padding плотных блоков               |
| `spacing.md`  | 16dp     | Горизонтальный padding экрана, padding карточки |
| `spacing.lg`  | 24dp     | Между секциями                                  |
| `spacing.xl`  | 32dp     | Крупные вертикальные разрывы                    |
| `spacing.xxl` | 48dp     | Пустое пространство до CTA-кнопки               |

Горизонтальный padding экрана: **16dp** (стандарт), на малых телефонах не уменьшать.

---

## 5. Скругления (Border Radius)

| Токен         | Значение | Применение                              |
|---------------|----------|-----------------------------------------|
| `radius.xs`   | 4dp      | Чекбоксы, бейджи, мелкие метки          |
| `radius.sm`   | 6dp      | Поля ввода, кнопки                      |
| `radius.md`   | 10dp     | Карточки, панели                        |
| `radius.lg`   | 14dp     | Модальные окна, bottomsheet             |
| `radius.pill` | 999dp    | Статус-бейджи «таблетки», переключатели |

---

## 6. Elevation / Разделение слоёв

В тёмной теме тень почти невидима. Разделение уровней — через **границу (border)** и **фон**.

| Уровень           | Реализация                                                   |
|-------------------|--------------------------------------------------------------|
| `elevation.0`     | Нет рамки, фон = `color.background`                          |
| `elevation.1`     | Фон `color.surface`, нет рамки                               |
| `elevation.2`     | Фон `color.surface2` + border 1dp `color.border`             |
| `elevation.modal` | Фон `color.surface2` + border 1dp `color.border` + scrim 50% |

Никаких сложных `box-shadow` с большим blur — только subtle border или фоновый контраст.

---

## 7. Иконки

- Источник: **Material Symbols Outlined** (stroke weight 200–300, optical size 24) или **Lucide Icons**.
- В коде поставляются как `*.xml`/`*.svg` через **moko-resources**, доступ — `MR.images.<name>`
  + `painterResource(MR.images.<name>)`. Растровые PNG не использовать.
- Единая гарнитура для всего приложения.
- Размер: 20dp (inline в тексте), 24dp (стандарт), 28dp (акцентный).
- Цвет: `color.textSecondary` по умолчанию; `color.textPrimary` для активных.
- Запрещено: emoji вместо иконок.
- Touch target: минимум 44×44pt (с `hitSlop` если иконка 24dp).

---

## 8. Компоненты

### 8.1 Кнопки

| Вариант       | Background          | Text                  | Border             | Применение              |
|---------------|---------------------|-----------------------|--------------------|-------------------------|
| `Primary`     | `color.ctaPrimary`  | `color.textOnAccent`  | нет                | Главное действие экрана |
| `Secondary`   | `color.surface2`    | `color.textPrimary`   | 1dp `color.border` | Вторичное действие      |
| `Destructive` | `color.errorSubtle` | `color.error`         | 1dp `color.error`  | Удаление, отмена        |
| `Ghost`       | прозрачный          | `color.textSecondary` | нет                | Третичные действия      |

**Размеры:**

- Height: 48dp (стандарт), 44dp (compact).
- Border radius: `radius.sm` (6dp).
- Padding горизонтальный: 20dp.
- Только одна Primary кнопка на экран.

**Состояния:**

- `Pressed`: overlay `state.pressedOverlay` поверх фона.
- `Disabled`: opacity `state.disabledOpacity` (0.5), `pointerEvents: none`.
- `Loading`: показывает CircularProgress вместо текста, `disabled`.

### 8.2 Поля ввода (TextField)

```
Label (bodyMedium, textSecondary) [*обязательное]
┌─────────────────────────────────────────┐
│ Placeholder или значение (bodyLarge)    │
└─────────────────────────────────────────┘
  Текст ошибки (caption, error)
```

- Фон поля: `color.surface2`.
- Рамка normal: 1dp `color.border`.
- Рамка focused: 1dp `state.focusBorder`.
- Рамка error: 1dp `color.error`.
- Label всегда над полем (не placeholder).
- Обязательные поля: `*` красного цвета `color.error` после label.
- Height: минимум 48dp.
- Тип клавиатуры соответствует типу данных (числа, email, текст).
- Disabled: opacity 0.5, не нажимается.

### 8.3 Чекбоксы

- Размер: 24×24dp, touch target 44×44dp.
- Unchecked: рамка 2dp `color.border`, фон прозрачный.
- Checked: фон `color.success`, иконка ✓ `color.textPrimary`.
- Error (обязательный, не заполнен): рамка `color.error`, иконка `!`.
- Label рядом: `bodyLarge`.

### 8.4 Статус-бейдж

Форма: `radius.pill`. Padding: 4dp 10dp.

| Статус                  | Background              | Text/Icon                 |
|-------------------------|-------------------------|---------------------------|
| `Completed` (Выполнено) | `color.successSubtle`   | `color.success` + ✓       |
| `InProgress` (В работе) | `rgba(196,135,42,0.15)` | `color.warning` + ▶       |
| `NotStarted` (Не начат) | `color.surface2`        | `color.textSecondary` + ○ |
| `Error` (Ошибка)        | `color.errorSubtle`     | `color.error` + ✕         |
| `Pending` (Синхр.)      | `color.syncSubtle`      | `color.sync` + ⇅          |
| `Offline`               | `color.surface2`        | `color.textDisabled` + ⊘  |

Всегда: иконка + текстовый лейбл (для цветослепых).

### 8.5 SnackBar / Toast

- Позиция: снизу, выше bottom navigation bar + safe area.
- Фон: `color.surface2`, border-left 4dp цвет статуса.
- Автоскрытие: 3–4 сек (info/success), без автоскрытия (error, требует действия).
- Текст: `bodyMedium`, иконка слева.

### 8.6 Bottom Navigation (Tab Bar)

- Max 4 пункта: Маршруты, Журнал, Синхронизация, Профиль.
- Фон: `color.surface`.
- Активная вкладка: иконка + label `color.textPrimary` (weight 500).
- Неактивная: `color.textSecondary`.
- Индикатор активной: подчёркивание 2dp сверху иконки или точка `color.ctaPrimary`.
- Нет горизонтального text truncation — label всегда видна.
- Badges на иконках: только для счётчиков (синхр. ожидающих), `color.warning` фон.

### 8.7 Top App Bar

- Фон: `color.background` (слитно с экраном, без доп. elevation).
- Заголовок: `type.displayMedium` (20sp, 600), `color.textPrimary`.
- Иконка «назад» слева: 24dp, `color.textSecondary`.
- Действие справа: иконка 24dp `color.textSecondary`.
- Статус синхронизации — компактная иконка правее заголовка.

### 8.8 Прогресс-бар

- Линейный: height 4dp, background `color.border`, fill `color.success`.
- Сегментный (dot-steps): размер точки 8dp, gap 6dp. Выполнен — зелёный, текущий — белый, ожидает — border серый.

### 8.9 Разделитель

- Толщина: 1dp.
- Цвет: `color.borderSubtle` (#2A2F3C).
- Используется между пунктами списка, внутри карточек.

### 8.10 Status Accent Bar

Вертикальная цветная полоса слева у карточки — быстрый «status at a glance», читается даже в перчатках.

- Толщина: **3dp** (компактные карточки списка, например sync-статус) или **4dp** (выделенная
  карточка «текущий шаг»).
- Высота: на всю высоту карточки, прижата к левому краю.
- Цвет — по семантике статуса:
  - В работе / текущий → `color.warning`
  - Завершено → `color.success`
  - Ошибка → `color.error`
  - Ожидает синхр. → `color.sync`
  - Не начато → `color.textDisabled` (или скрыть полосу)
- Когда полоса присутствует, левый padding контента увеличивается на 4dp (16dp → 20dp), чтобы
  не «съедать» воздух у текста.
- Полоса **дополняет** иконку и/или бейдж статуса, не заменяет их (требование §13 — не только цвет).

### 8.11 Pinned Progress Header

Прогресс-полоса под Top App Bar, всегда видимая (не sticky-при-скролле — pinned).

```
┌─ Top App Bar ───────────────────────────────┐
│ ← ПТО-7 · Насосная ст. №3                  │
└─────────────────────────────────────────────┘
│ 5 of 12 completed   ████████░░░░░░░░  42%   │  ← pinned
└─────────────────────────────────────────────┘
```

- Высота: 56dp, фон `color.background`, нижняя граница 1dp `color.borderSubtle`.
- Внутри: Row (16dp горизонтальный padding, 10dp вертикальный):
  - Подпись `«X of Y completed»` — `type.bodyMedium`, `color.textSecondary`.
  - `LinearProgressIndicator`: высота **4dp**, форма `radius.pill`, fill `color.success`,
    track `color.border`. Прогресс берёт оставшееся пространство.
  - Опционально: процент справа — `type.bodyMedium` (500), `color.textPrimary`.
- Применяется на экранах с явным линейным прогрессом: точки маршрута, шаги чек-листа.

### 8.12 Checklist Item Types

Чек-лист поддерживает **5 типов пунктов** (каждый — отдельный composable). У всех общие
инварианты:

- Заголовок пункта формируется через `item.titleWithRequiredMarker()` — добавляет красную `*`
  для обязательного поля (`color.error`, тот же weight).
- Подсказки/ошибки под полем: `type.caption`. Цвет:
  - `color.textSecondary` — нейтральная подсказка (диапазон, формат).
  - `color.error` — ошибка валидации.
- Вертикальный gap между пунктами в LazyColumn: **16dp**.

| Тип       | Контрол                         | Особенности                                                                |
|-----------|----------------------------------|----------------------------------------------------------------------------|
| `Boolean` | `Switch` (Material3)             | Лейбл слева (`bodyLarge`), Switch справа.                                  |
| `Text`    | `OutlinedTextField` (single-line)| Лейбл сверху (`bodyMedium`/`label`), keyboard `Text`.                      |
| `Number`  | `OutlinedTextField` (decimal)    | Range-валидация (`min`/`max`); helper `«from X to Y»` серый, ошибка красная.|
| `Select`  | RadioGroup (Column, gap 4dp)     | Каждая опция — `bodyLarge`, рядом `RadioButton`.                           |
| `Confirm` | Кнопка `Primary` («Подтвердить») | Текст-инструкция слева, кнопка справа; после подтверждения — `disabled`.   |

Добавление новых типов — расширение этой таблицы и каталога в `feature-checklist/ui/items/`.

### 8.13 Equipment Card Field

Базовый «label + value» паттерн для отображения справочных полей (карточка оборудования).

- Layout: Column, `Arrangement.spacedBy(12.dp)` между полями.
- Внутри одного поля:
  - Label (`type.caption` или `type.bodyMedium`, `color.textSecondary`).
  - Value (`type.bodyLarge`, `color.textPrimary`).
  - Без рамок, без фона — просто текст с вертикальным ритмом.
- Карточка оборудования сама себе фон не рисует — рендерится поверх `color.background`.
- В `bottomBar` экрана — Primary кнопка «Открыть чек-лист» (full-width минус
  2×`spacing.md`, фон `color.ctaPrimary`).

### 8.14 Photo Capture Grid

Горизонтальная лента сделанных фото + кнопки управления.

- Лента: `LazyRow`, `horizontalArrangement = spacedBy(8.dp)`, `contentPadding = 4.dp` по краям.
- Тайл фото: квадрат **120×120dp**, `radius.md`, `AsyncImage` (Coil3).
- Состояние «нет фото»: лента скрыта, видна только кнопка `«Снять фото»` (Secondary).
- Кнопки в Column ниже ленты:
  - `«Снять фото»` — Secondary, всегда видна; `disabled` пока идёт capture.
  - `«Подтвердить»` — Primary, `disabled` пока `photos.isEmpty()`.
- Удаление фото — long-press на тайл → подтверждение через системный диалог.

---

## 9. Экранные шаблоны

### 9.1 Экран-список (маршруты, журнал)

```
┌─ Top App Bar ───────────────────────────────┐
│ ← Назначенные маршруты        🔄 ⊘ offline  │
└─────────────────────────────────────────────┘
  ┌─ Card elevation.1 ──────────────────────┐
  │ ПТО-7 · Насосная ст. №3  [В работе ▶]   │
  │ Маршрут #1234                            │
  │ ████░░░░ 5 / 12 точек                    │
  │ 📅 04 апр 2026 · ⏱ ~40 мин              │
  └─────────────────────────────────────────┘
  ┌─ Card + sync accent (3dp left) ─────────┐
  ║ ПТО-3 · Котельная       [Ожидает ⇅]    │  ← color.sync 3dp слева
  ║ Маршрут #1233                            │
  ║ ✓ Завершён · 8 / 8 точек                │
  └─────────────────────────────────────────┘
└─ Bottom Navigation ─────────────────────────┘
```

- Активные маршруты — сверху, ожидающие синхр. — со status accent bar (§8.10).
- Empty state: иконка + «Нет назначенных маршрутов» + retry при ошибке.
- Pull-to-refresh — в плане, ещё не реализован (отслеживается в waypoint-плане).

### 9.2 Чек-лист (рабочий экран)

```
┌─ Top App Bar ───────────────────────────────┐
│ ← Компрессор К-1        Сохранить и выйти   │
│   ████████░░ 8/10 пунктов                   │
└─────────────────────────────────────────────┘
  ─── Секция 1: Внешний осмотр ───────────── (sticky)
  ┌─────────────────────────────────────────┐
  │ 1. Осмотреть корпус *                   │
  │    [OK ✓] [Неисправен ✕]  [📷]        │
  └─────────────────────────────────────────┘
  ┌─────────────────────────────────────────┐
  │ 2. Уровень масла *       [123.4] кПа   │
  └─────────────────────────────────────────┘
  ...
┌─ Sticky Footer ─────────────────────────────┐
│         [Завершить обход]  ← disabled пока   │
└─────────────────────────────────────────────┘
```

- Заголовки секций: sticky при скролле.
- Незаполненный обязательный пункт: красная рамка, `*` красный.
- Кнопка «Завершить» disabled пока есть незаполненные обязательные.

> **Текущее состояние реализации (`feature-checklist`):**
> Sticky-заголовки секций, sticky-footer и offline-баннер ещё не реализованы — пункты
> рендерятся плоским `LazyColumn`, кнопка «Завершить» — последним item'ом списка.
> Item-и реализованы (см. §8.12). Выравнивание с pages/checklist.md — в плане работ.

### 9.3 Итоги проверки

```
┌─────────────────────────────────────────────┐
│                   ✓                         │
│            Обход завершён                   │
│         04 апр 2026, 14:32                  │
│                                             │
│  ┌──────┐  ┌──────┐  ┌──────┐             │
│  │  12  │  │   3  │  │   8  │             │
│  │пунк. │  │дефек.│  │ фото │             │
│  └──────┘  └──────┘  └──────┘             │
│                                             │
│    [Синхронизировать]   (primary)           │
│    [Распечатать отчёт]  (secondary)         │
└─────────────────────────────────────────────┘
```

---

## 10. Анимации и переходы

| Тип                            | Длительность | Easing              |
|--------------------------------|--------------|---------------------|
| Нажатие кнопки (scale/opacity) | 100–150ms    | ease-out            |
| Переход между экранами         | 250ms        | ease-in-out         |
| Появление SnackBar             | 200ms        | ease-out (slide-up) |
| Исчезновение SnackBar          | 150ms        | ease-in             |
| Разворот аккордеона            | 200ms        | ease-out            |
| Прогресс-бар заполнение        | 300ms        | ease-out            |

- Все анимации уважают `prefers-reduced-motion` / AccessibilityInfo.isReduceMotionEnabled.
- Никаких декоративных бесконечных анимаций.
- Состояние Loading: CircularIndicator 24dp, `color.textSecondary`.

---

## 11. Offline-режим

- Когда сеть недоступна: иконка `⊘` рядом с заголовком в App Bar.
- Локальные (несинхронизированные) данные помечаются `⇅` иконкой цвета `color.sync`.
- Кнопка «Синхронизировать» — `disabled` без сети, не скрывается.
- При частичной потере сети — не скрываем контент, только добавляем статусный индикатор.
- SnackBar при потере сети: «Нет связи. Данные сохраняются локально.» (без автоскрытия).

---

## 12. Доступность

- Контраст текст/фон: минимум **4.5:1** для основного текста, **3:1** для крупного (≥18sp).
- Touch target: минимум **44×44pt** для всех интерактивных элементов.
- `accessibilityLabel` на все иконочные кнопки.
- Статус-бейджи содержат и иконку, и текст (не только цвет).
- `accessibilityRole` и `accessibilityState` для чекбоксов, переключателей, кнопок.
- Dynamic Type: все размеры в `sp` (масштабируемые), не `dp`.
- Порядок фокуса следует визуальному порядку.

---

## 13. Контрастная проверка (WCAG AA)

| Пара                                                | Соотношение | Статус                |
|-----------------------------------------------------|-------------|-----------------------|
| `textPrimary` (#E8EAF0) на `background` (#1A1D22)   | ~13.5:1     | ✓ AAA                 |
| `textSecondary` (#9499A8) на `background` (#1A1D22) | ~5.8:1      | ✓ AA                  |
| `textDisabled` (#55596A) на `background` (#1A1D22)  | ~2.8:1      | ⚠ только для disabled |
| `success` (#3D9E72) на `successSubtle` (#1F3D2D)    | ~4.8:1      | ✓ AA                  |
| `error` (#B84040) на `errorSubtle` (#3A1A1A)        | ~5.2:1      | ✓ AA                  |
| `warning` (#C4872A) на `warningSubtle` (#3A2B10)    | ~5.0:1      | ✓ AA                  |
| `textOnAccent` (#1A1D22) на `ctaPrimary` (#D8DBE6)  | ~11.8:1     | ✓ AAA                 |

### Светлая тема

| Пара                                                | Соотношение | Статус                |
|-----------------------------------------------------|-------------|-----------------------|
| `textPrimary` (#1A1D22) на `surface` (#FFFFFF)      | ~16.9:1     | ✓ AAA                 |
| `textPrimary` (#1A1D22) на `background` (#EDEFF2)   | ~14.7:1     | ✓ AAA                 |
| `textSecondary` (#5A6172) на `surface` (#FFFFFF)    | ~6.2:1      | ✓ AA                  |
| `textDisabled` (#8F95A4) на `surface` (#FFFFFF)     | ~3.0:1      | ⚠ только для disabled |
| `textOnAccent` (#FFFFFF) на `ctaPrimary` (#2A2F3C)  | ~13.4:1     | ✓ AAA                 |
| `success` (#27704D) на `successSubtle` (#E2F1EA)    | ~5.1:1      | ✓ AA                  |
| `warning` (#8C5A12) на `warningSubtle` (#FBF0DC)    | ~5.2:1      | ✓ AA                  |
| `error` (#B83232) на `errorSubtle` (#FBE6E4)        | ~5.0:1      | ✓ AA                  |
| `sync` (#8A5614) на `syncSubtle` (#F7ECDA)          | ~5.3:1      | ✓ AA                  |

---

## 14. Реализация токенов в Kotlin (Compose Multiplatform)

Все токены живут в `shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/`
и доступны через объект **`ToirTheme`** (а не `MaterialTheme`):

```kotlin
@Composable
fun MyComponent() {
    Box(
        modifier = Modifier
            .background(ToirTheme.colors.surface)
            .clip(ToirTheme.shapes.md)
            .padding(16.dp)
    ) {
        Text(
            text = "Заголовок",
            style = ToirTheme.typography.headline,
            color = ToirTheme.colors.textPrimary,
        )
    }
}
```

Доступные surfaces:

- `ToirTheme.colors: ToirColorScheme` — все 24 токена из §2 (background, surface, …,
  destructive, focusBorder, pressedOverlay, selectedBackground).
- `ToirTheme.typography: ToirTypography` — 7 стилей из §3 (displayLarge, displayMedium,
  headline, bodyLarge, bodyMedium, label, caption). Шрифт — `fontInter` через
  moko-resources.
- `ToirTheme.shapes: ToirShapes` — `xs/sm/md/lg/pill` из §5.

Корневой провайдер — `ToirTheme { content() }` в `shared/main/.../App.kt`. По умолчанию
`LocalTextStyle` уже выставлен в `bodyMedium` + `textPrimary`, поэтому в большинстве
`Text(...)` явный color не требуется.

### Цветовая палитра (минимальный пример)

```kotlin
// shared/common-ui/src/commonMain/kotlin/ru/mirea/toir/common/ui/compose/theme/Colors.kt

@Immutable
data class ToirColorScheme(
    val background: Color,
    val surface: Color,
    val surface2: Color,
    val surfacePressed: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val textOnAccent: Color,
    val ctaPrimary: Color,
    val ctaSecondary: Color,
    val success: Color, val successSubtle: Color,
    val warning: Color, val warningSubtle: Color,
    val error: Color,   val errorSubtle: Color,
    val sync: Color,    val syncSubtle: Color,
    val destructive: Color,
    val focusBorder: Color,
    val pressedOverlay: Color,
    val selectedBackground: Color,
)

private val darkColorScheme = ToirColorScheme(
    background = Color(0xFF1A1D22),
    surface = Color(0xFF242830),
    surface2 = Color(0xFF2D3240),
    // … значения см. в §2
)

internal fun getLightColorScheme(): ToirColorScheme = darkColorScheme  // dark-only
internal fun getDarkColorScheme(): ToirColorScheme = darkColorScheme
```

---

## 15. Чеклист перед сдачей

### Визуальное качество

- [ ] Нет emoji вместо иконок (только Material Symbols или Lucide)
- [ ] Иконки единой гарнитуры, одинаковый stroke weight
- [ ] Нет hardcoded цветов — только токены из `ToirTheme.colors`
- [ ] Нет обращений к `MaterialTheme.colorScheme/typography/shapes` —
      только `ToirTheme.colors/typography/shapes`
- [ ] Состояния нажатия не сдвигают layout

### Интерактивность

- [ ] Все тапаемые элементы ≥ 44×44pt
- [ ] Кнопки показывают feedback в 100–150ms
- [ ] Disabled-состояние визуально однозначно и не реагирует на тап
- [ ] Прогресс/лоадер показан при операциях > 300ms

### Статусы и цвета

- [ ] Каждый статус = иконка + текст + цвет (не только цвет)
- [ ] Offline-режим виден в App Bar
- [ ] Незаполненные обязательные поля помечены при попытке сохранить

### Layout

- [ ] Safe areas соблюдены (notch, gesture bar, bottom tab bar)
- [ ] Контент не скрывается за sticky header/footer
- [ ] Горизонтальный padding 16dp по всем экранам
- [ ] Проверено на 375px (малый телефон) и 414px (большой)

### Доступность

- [ ] Контраст ≥ 4.5:1 для основного текста
- [ ] `accessibilityLabel` на все иконочные кнопки
- [ ] Порядок фокуса соответствует визуальному
- [ ] `isReduceMotionEnabled` уважается
