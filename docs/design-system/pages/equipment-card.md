# Page Override: Карточка оборудования

> Переопределяет MASTER.md для экрана `EquipmentCardScreen` — карточки точки маршрута,
> показанной перед запуском чек-листа.

## Специфика экрана

Карточка оборудования между списком точек (`RoutePointsScreen`) и чек-листом
(`ChecklistScreen`). Это **«единственный шанс верификации перед запуском работы»**:
техник видит характеристики оборудования и (опционально) подтверждает QR-кодом, что
он действительно у нужного агрегата.

## Layout

```
┌─ Top App Bar ────────────────────────────────┐
│ ← Компрессор К-1                            │
└──────────────────────────────────────────────┘
  ┌─ Карточка оборудования (elevation.1) ───┐
  ║ Код                                      │  ← color.warning 4dp accent
  ║ PT-047                                   │     (если QR не подтверждён)
  ║                                          │
  ║ Название                                 │
  ║ Компрессор К-1                           │
  ║                                          │
  ║ Тип                                      │
  ║ Винтовой компрессор                      │
  ║                                          │
  ║ Локация                                  │
  ║ Зона А · Этаж 1                          │
  ║                                          │
  ║ Статус                                   │
  ║ [В работе ▶]                             │  ← статус-бейдж (§8.4)
  └──────────────────────────────────────────┘

  ┌─ Info banner (если QR не подтверждён) ──┐
  │ ⓘ  Перед запуском проверки              │
  │    отсканируйте QR-код на оборудовании  │
  └─────────────────────────────────────────┘

  ✓ Подтверждено по QR                        ← chip (если подтверждено)

┌─ Sticky Footer ──────────────────────────────┐
│  [ Сканировать QR ]   ← Secondary           │
│  [ Открыть чек-лист ]   ← Primary           │
└──────────────────────────────────────────────┘
```

## Top App Bar

- Стандартный (§8.7), фон `color.background`.
- Заголовок — название оборудования (`type.displayMedium`, `color.textPrimary`).
- Иконка «назад» слева (vector, 24dp). Без действий справа.

## Карточка оборудования

- Внешний padding экрана: `spacing.md` (16dp) горизонтально, `spacing.md` сверху.
- Карточка: фон `color.surface`, `radius.md`, padding 16dp внутри.
- **Левый Status Accent Bar (§8.10)**, толщина 4dp:
  - `color.warning` — QR не подтверждён, требуется верификация.
  - `color.success` — QR подтверждён **или** оборудование не требует QR.
  - `color.textDisabled` — точка ещё не начата (опционально, если статус «не начат»).
- При наличии accent bar левый padding контента увеличивается до 20dp (16 + 4).

### Поля (Equipment Card Field, §8.13)

- Все поля рендерятся одинаково: label + value, gap между полями `spacing.sm` (12dp).
- Label: `type.bodyMedium`, `color.textSecondary`.
- Value: `type.bodyLarge`, `color.textPrimary`.
- **Без рамок, без фонов** — только текст с вертикальным ритмом (вся группа полей
  лежит внутри одной общей карточки `color.surface`, отдельные «карточки на поле»
  не используются).
- Поле **«Статус»** — value не текст, а **статус-бейдж** (§8.4): иконка + текст,
  фон `*Subtle`, `radius.pill`, padding 4dp 10dp.
- Порядок полей фиксирован и соответствует `DomainEquipmentCard`:
  **Код → Название → Тип → Локация → Статус**.
  Дополнительные поля (например, серийный номер, дата ввода в эксплуатацию) —
  идут после Локации, до Статуса.

### Условные поля и пустые значения

- **Локация (`locationName`)** — поле **скрывается полностью**, если значение пустое
  (текущее поведение `EquipmentCardContent.kt`).
- **Прочие текстовые поля** (`code`, `name`, `type`) — value никогда не должно быть
  пустым с product-точки зрения, но как defensive fallback показывается **«—»**
  (em-dash, U+2014) тем же стилем, что и обычное value.
- Поле «Статус» всегда присутствует (default = `NOT_STARTED`).

## Информационный баннер «Сканировать QR»

Показывается **только** если `equipment.requiresQr == true && !qrConfirmed`.

- Под карточкой, отступ сверху `spacing.md` (16dp).
- Фон `color.surface2`, `radius.md`, padding 12dp.
- Layout: Row, vertical center.
  - Иконка `info` (vector, 20dp), `color.textSecondary`.
  - gap 8dp.
  - Текст в две строки: «Перед запуском проверки отсканируйте QR-код на оборудовании»,
    `type.caption`, `color.textSecondary`.

## Состояние «QR подтверждён»

- Accent bar карточки → `color.success`.
- Info banner **скрыт**.
- Между карточкой и footer — **chip** «✓ Подтверждено по QR»:
  - Фон `color.successSubtle`, текст `color.success`.
  - `radius.pill`, padding 4dp 10dp.
  - Иконка `check_circle` (vector, 16dp) + текст (`type.caption`).
  - Выравнивание: центр по горизонтали, отступ сверху `spacing.sm`.

## Sticky Footer (`bottomBar` Scaffold)

- Фон `color.background`, border-top 1dp `color.borderSubtle` когда есть scroll.
- Padding: `horizontal = 16.dp`, **`vertical = 16.dp`** поверх bottom safe area
  (итог = 16dp + safe area inset).
- Кнопки в Column, gap `spacing.xs` (8dp).

### Кнопка «Сканировать QR»

- Variant: **Secondary**, full-width, height 48dp.
- Иконка `qr_code_scanner` (vector через `MR.images.ic_qr_code_scanner`), 20dp,
  `color.textPrimary`. **Никаких эмодзи.**
- gap между иконкой и текстом 8dp.
- **Скрывается полностью** если `equipment.requiresQr == false` или `qrConfirmed == true`.

### Кнопка «Открыть чек-лист»

- Variant: **Primary**, full-width, height 48dp.
- **Disabled** если `equipment.requiresQr == true && !qrConfirmed`.
  Под кнопкой — caption «Сначала отсканируйте QR» (`type.caption`, `color.warning`,
  центрировано, gap 4dp).
- **Enabled** в остальных случаях.
- Тап → переход в `ChecklistScreen` для этой точки.

## QR-сканирование

- Тап «Сканировать QR» → платформенный сканер:
  - Android: bottom-sheet с CameraX + ML Kit Barcode Scanner.
  - iOS: full-screen `AVCaptureSession`.
- На время сканирования — `loadingState = scanning`, обе footer-кнопки disabled.
- **Успех** (распарсен правильный `equipmentCode`):
  - `qrConfirmed = true`, UI обновляется: accent bar → green, banner → chip.
  - SnackBar (опционально) «QR-код подтверждён» (`color.success`, auto-dismiss 2s).
- **Неверный QR** (код не соответствует `equipment.code`):
  - SnackBar «Этот QR-код не соответствует оборудованию» (`color.error`,
    border-left `color.error`, без auto-dismiss).
- **Отказ в permissions**:
  - SnackBar «Нет доступа к камере. Откройте настройки.» + action «Настройки».
- **Отмена сканирования** пользователем — UI возвращается в исходное состояние.

> **Состояние реализации:** модуль QR пока не интегрирован. До интеграции
> на product-уровне `equipment.requiresQr = false` для всех точек, и UI работает
> без QR-секции. Спека описывает целевой UX.

## Состояние Loading (загрузка карточки)

- На месте карточки — `CircularProgressIndicator` 28dp, центрирован.
- Footer кнопки disabled.
- Заголовок App Bar — пустой или «Загрузка...» (если название точки ещё не пришло
  через args).

## Состояние Error (карточка не загрузилась)

- На месте карточки и баннеров:
  - Иконка `error_outline` (vector, 48dp), `color.error`, центр.
  - Заголовок «Не удалось загрузить карточку» — `type.bodyLarge`, `color.textPrimary`.
  - Описание ошибки — `type.bodyMedium`, `color.textSecondary` (опционально).
  - Кнопка «Повторить» — Secondary, max-width 200dp, gap `spacing.md`.
- Footer **скрыт** в этом состоянии.

## Доступность

- Status accent bar дублируется status-бейджем и chip — соблюдается правило
  «не только цвет» (§13).
- Заголовки полей читаются screen reader'ом до значений
  (label first → value second).
- При QR-успехе — `accessibilityLiveRegion = polite` для chip «Подтверждено по QR».
- Footer кнопки — стандартный `accessibilityRole = button`.

## Навигация

- «Назад» в App Bar / system back → возврат на `RoutePointsScreen` без сохранения
  `qrConfirmed` (нужно подтверждать заново при следующем входе).
- «Открыть чек-лист» → forward navigation на `ChecklistScreen` (не replace —
  пользователь может вернуться к карточке через back).
