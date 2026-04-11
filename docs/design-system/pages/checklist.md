# Page Override: Экран чек-листа

> Переопределяет MASTER.md для экрана рабочего чек-листа.

## Специфика экрана

Это основной рабочий экран. Техник работает в нём большую часть времени, часто в неудобных
условиях (перчатки, яркий свет). Приоритет: скорость ввода и минимум ошибок.

## Отличия от MASTER

### Плотность контента

Пункты чек-листа используют **compact padding**:
- Внутренний padding пункта: 12dp (spacing.sm) вместо 16dp
- Gap между пунктами: 2dp (разделитель borderSubtle)
- Высота строки пункта: minimum 56dp (для перчаток, выше стандарта)

### Sticky заголовки секций

Секции (группы пунктов) имеют sticky header при скролле:
- Background: `color.background` (не surface)
- Текст: `type.label` (13sp, 500, CAPS, letter-spacing +0.8px)
- Цвет: `color.textSecondary`
- Separator под sticky header: 1dp `color.border`

### Элемент чек-листа

```
┌─────────────────────────────────────────────────┐
│ 1. Осмотреть корпус компрессора *               │ ← bodyLarge, textPrimary
│                                    [OK] [Неиспр.]│
│                                           [📷 0] │ ← фото: caption + иконка
└─────────────────────────────────────────────────┘
```

- Нумерация: textSecondary, bodyMedium
- Текст пункта: textPrimary, bodyLarge
- `*` для обязательных: отдельный span color.error, bodyLarge
- Кнопки [OK] / [Неисправен]: `Secondary` вариант, height 40dp, compact
- [OK] нажатый: фон `successSubtle`, текст `success`, border `success`
- [Неисправен] нажатый: фон `errorSubtle`, текст `error`, border `error`
- Иконка фото: `color.textSecondary`, при наличии фото — `color.success` + счётчик

### Поле числового ввода

```
┌─────────────────────────────────────┐
│ 123.4                          кПа │
└─────────────────────────────────────┘
```

- Ширина: auto, min 120dp, max 50% экрана
- Единицы измерения: textSecondary, bodyMedium, внутри поля справа
- Клавиатура: numeric/decimal

### Sticky footer (кнопка завершения)

- Height: 56dp + bottom safe area
- Background: `color.background` с border-top 1dp `color.border`
- Кнопка «Завершить»: Primary, full-width минус 2×spacing.md
- Disabled пока есть незаполненные обязательные (`*`) пункты
- Disabled: opacity 0.5 + текст «Заполните обязательные поля» над кнопкой (caption, warning)

### Offline-баннер

При offline: slim-баннер (height 32dp) между App Bar и контентом:
- Background: `color.syncSubtle`
- Текст: «Офлайн-режим · данные сохраняются локально» — caption, `color.sync`
- Иконка ⊘ слева

## Навигация

- Нет bottom navigation bar на этом экране (скрыт для фокуса).
- «Назад» в App Bar: подтверждение «Прогресс будет сохранён. Выйти?».
