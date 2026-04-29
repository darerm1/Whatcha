# Статический BDUI-контракт

В этой папке лежат статические файлы для лабораторной по BDUI:

- `manifest.json`
- `detail-fragment.json`
- `publish-bdui.ps1`

Они описывают один server-driven экран: `DetailFragment`.

## Структура экрана

Файл `detail-fragment.json` состоит из следующих частей:

- `screenId / kind / version` — метаинформация экрана
- `screenStates` — поддерживаемые состояния экрана
- `defaults` — значения по умолчанию для элементов
- `theme` — токены темы, типографика и общие стилевые объекты
- `items` — список компонентов экрана

## Контракт состояния

Объект состояния не дублируется внутри самого JSON-экрана, а описывается этой документацией. Он должен прийти вместе со схемой экрана или быть подготовлен клиентом до рендера.

Обязательные поля:

- `state`: `loading | content | error | empty`
- `toolbarTitle`
- `showRatingsSection`
- `favoriteSelected`
- `ratingEnabled`
- `personalRatingValue`
- `personalRatingText`
- `saveRatingEnabled`
- `statusButtonTitle`

Опциональные поля:

- `errorMessage`
- `posterUrl`
- `movieTitle`
- `movieMeta`
- `movieDescription`
- `ratingsPayload`

`ratingsPayload` имеет форму:

```json
{
  "kp": 8.4,
  "imdb": 8.7,
  "filmCritics": 7.9,
  "russianFilmCritics": 60.0
}
```

## Состояния экрана

Экран поддерживает четыре состояния:

- `loading` — данные ещё загружаются, контент не показывается
- `content` — показывается основной экран фильма
- `error` — показывается `error_view`
- `empty` — данных для показа нет

Источником истины для базовой видимости является поле `state`.

- если у элемента нет `visibleStates`, используется `defaults.visibleStates`
- `visibleStates` на компоненте переопределяет это правило
- `showRatingsSection` остаётся отдельным бизнес-флагом, потому что это не состояние всего экрана, а условие показа конкретного блока

## Пример payload

```json
{
  "state": "content",
  "toolbarTitle": "Рождественский роман на родео",
  "showRatingsSection": true,
  "posterUrl": "https://example.com/poster.jpg",
  "movieTitle": "Рождественский роман на родео",
  "movieMeta": "2025 • драма • 0 мин",
  "movieDescription": "Наездница родео Эмма возвращается домой и пытается начать жизнь заново.",
  "favoriteSelected": false,
  "ratingsPayload": {
    "imdb": 6.0
  },
  "ratingEnabled": true,
  "personalRatingValue": 0,
  "personalRatingText": "Оценка не задана",
  "saveRatingEnabled": false,
  "statusButtonTitle": "Статус: Не установлен"
}
```

## Поддерживаемые компоненты

Клиент должен уметь рендерить следующие типы:

- `toolbar`
  Обязательные поля: `elementId`, `textBinding` или `text`, `action`
- `error_view`
  Обязательные поля: `elementId`, `textBinding` или `text`
  Опциональные поля: `action`, `data.showRetry`, `appearance.buttonText`
- `loading_view`
  Обязательные поля: `elementId`
- `empty_view`
  Обязательные поля: `elementId`, `textBinding` или `text`
- `poster_image`
  Обязательные поля: `elementId`, `data.urlBinding` или `data.url`
- `text`
  Обязательные поля: `elementId`, `textBinding` или `text`
- `icon_toggle`
  Обязательные поля: `elementId`, `data.icon`, `data.selectedIcon`
- `ratings_chart`
  Обязательные поля: `elementId`, `data.ratingsBinding` или `data.ratings`
- `rating_bar`
  Обязательные поля: `elementId`, `data.ratingBinding` или `data.rating`
- `primary_button`
  Обязательные поля: `elementId`, `textBinding` или `text`

## Layout-правила

В контракте используются простые layout-хинты:

- если `layout` не задан, используется `defaults.layout`
- `layout.mode = "block"` — элемент занимает отдельную строку
- `layout.mode = "inline"` — элемент рендерится в общей строке
- `rowId` — связывает элементы одной строки
- `weight` — задаёт растягивание элемента внутри строки
- `align = "end"` — выравнивает inline-элемент к концу строки

Более сложный layout engine в этом контракте не используется.

## Семантика action

Клиент должен трактовать `action.type` так:

- `navigate_back` — закрыть текущий экран
- `retry_load_detail` — повторно запросить данные detail-экрана без optimistic update
- `open_poster_preview` — открыть постер в preview-режиме
- `toggle_favorite` — переключить состояние избранного; наружу уходит событие с `elementId` и текущим `movieId` контекста; optimistic update не обязателен
- `change_rating` — обновить локально выбранное значение рейтинга; наружу уходит новое число рейтинга
- `one_click` — выполнить список `effects` по порядку
- `open_status_menu` — открыть меню выбора статуса; наружу уходит выбранное значение статуса после подтверждения

Поддерживаемые `effects`:

- `save_rating` — сохранить текущую оценку
- `show_feedback` — показать сообщение пользователю

## Темизация и ресурсы

В `theme` используются не жёсткие hex-цвета, а токены текущей Android-темы:

- `colorBackground`
- `colorSurface`
- `colorPrimary`
- `colorOnPrimary`
- `colorOnSurface`
- `colorOnSurfaceVariant`
- `colorError`
- `colorOutline`

Это позволяет одному и тому же JSON корректно работать в светлой и тёмной теме.

Иконки и placeholders должны существовать на клиенте как локальные ресурсы:

- `ic_arrow_back`
- `ic_favorite_border`
- `ic_favorite_filled`
- `poster_placeholder_branded`

## Публикация

Для загрузки статических файлов в Echo API используется:

- `publish-bdui.ps1`
