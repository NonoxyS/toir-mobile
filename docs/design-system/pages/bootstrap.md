# Page Override: Загрузка / Bootstrap

> Переопределяет MASTER.md для экрана авто-входа и загрузки конфигурации.

## Специфика экрана

Auth-gate + загрузка стартовой конфигурации. Появляется при запуске приложения,
если у пользователя есть валидный токен (иначе — Auth). Также показывается после
успешного входа, пока тянется `/api/v1/mobile/bootstrap`.

«Прихожая» приложения. Нет Bottom Navigation, нет «назад», нет ничего лишнего.

## Layout

```
┌─────────────────────────────────────────────────┐
│                                                 │
│                                                 │
│                  TOIR                           │  ← displayLarge, textPrimary
│       Система технического обхода               │  ← bodyMedium, textSecondary
│                                                 │
│                                                 │  ← spacing.xxl до состояния
│                                                 │
│                  ⟳                              │  ← CircularProgressIndicator 28dp
│             Подготовка...                       │  ← caption, textSecondary
│                                                 │
│                                                 │
└─────────────────────────────────────────────────┘
```

## Структура

- `Box(fillMaxSize)` + `background = ToirTheme.colors.background`.
- `Column(verticalArrangement = SpaceBetween)` внутри:
  - **Шапка** (top третья) — тот же блок, что в `pages/auth.md`:
    - Текст «TOIR» (`type.displayLarge`, `color.textPrimary`).
    - Подзаголовок «Система технического обхода» (`type.bodyMedium`, `color.textSecondary`).
    - Отступ сверху: `spacing.xxl` (48dp) от safe area.
  - **Центр** — слот для текущего состояния (Loading / Error). Высота автоматическая.
  - **Низ** — пустой spacer для визуального баланса.

## Состояние Loading

- `CircularProgressIndicator` 28dp, `color.textSecondary`.
- Под индикатором — caption «Подготовка...», `color.textSecondary`.
- gap между ними: `spacing.sm` (12dp).

## Состояние Error

```
                    [⊘]
            Не удалось загрузить
          Проверьте подключение и
                попробуйте снова

            [    Повторить    ]   ← Primary
```

- Иконка `cloud_off` (Material Symbols, vector через `MR.images.ic_cloud_off`),
  размер 48dp, `color.error`.
- Заголовок: `type.bodyLarge`, `color.textPrimary`, центр — «Не удалось загрузить».
- Подзаголовок (две строки, центр): `type.bodyMedium`, `color.textSecondary`.
- gap `spacing.lg` (24dp) между группой текста и кнопками.
- Кнопка **«Повторить»** — Primary, max-width 280dp, центрирована.
  Тап → повторный вызов `loadBootstrap()`.

> **Кнопки выхода/смены пользователя на этом экране нет.** Сценарии, для которых
> она могла бы понадобиться, обрабатываются автоматически (см. ниже).

## Обработка 401 / тухлого токена

`BootstrapExecutor` обязан различать тип ошибки:

- **HTTP 401** от `/api/v1/mobile/bootstrap` → токен невалиден или протух.
  Executor вызывает `authRepository.logout()` + публикует `Label.NavigateToLogin`.
  Error-state с кнопкой «Повторить» **не показывается** — ретрай бесполезен.
- **Network / timeout / 5xx** → отображается Error-state с кнопкой «Повторить».
- **Прочие 4xx (403, 422, …)** → Error-state с кнопкой «Повторить» (повтор может
  помочь, если ошибка временная серверная).

Это исключает залипание пользователя на Bootstrap при невалидном токене и убирает
необходимость в UI-кнопке «Сменить пользователя» / «Выйти». Canonical-место для
ручного логаута — будущий экран Profile/Settings.

## Состояние Success

- UI **не рендерится** — экран сразу публикует `Label.NavigateToRoutesList`.
- Никаких snackbar, no «Готово».

## Анимации

- Title-блок появляется без анимации (мгновенно при первом composition).
- Смена `Loading → Error` или `Loading → Success`: `Crossfade(targetState = uiState, animationSpec = tween(200))`.
- Без декоративных бесконечных анимаций — только spinner CircularProgressIndicator.
- Уважает `isReduceMotionEnabled`: Crossfade заменяется мгновенной сменой.

## Безопасные зоны

- Top safe area (status bar / notch / Dynamic Island) учитывается.
- Bottom safe area не критична — низ экрана пустой.
- Без `imePadding` — клавиатура не появляется.

## Доступность

- При появлении Error-состояния фокус screen-reader'а перемещается на заголовок
  «Не удалось загрузить».
- Кнопка «Повторить» — `accessibilityRole = button`, label «Повторить попытку загрузки».
- CircularProgressIndicator — `accessibilityLabel = "Загрузка"`, `accessibilityState = busy`.

## Навигация

- Никакого «назад». Системная back-кнопка / swipe-back на этом экране **не реагирует**
  (или приводит к выходу из приложения через `finish()` — стандартное поведение
  start destination'а).
- Из Error → «Сменить пользователя» — единственный способ покинуть экран без retry.
