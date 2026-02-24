# notifier-telegrambots-adapter

Адаптер отправки сообщений в Telegram через библиотеку `telegrambots`.

Основной класс:
- `DefaultTelegramSender implements TelegramSender`

Поведение:
- создает `DefaultAbsSender` на основе `NotifierConfig.botToken()`
- формирует `SendMessage` и отправляет только исходящие сообщения (outbound-only)
- применяет `parseMode`:
  - `PLAIN` -> без parse mode
  - `HTML` -> `HTML`
  - `MARKDOWN` -> `Markdown`
  - `MARKDOWN_V2` -> `MarkdownV2`
- применяет `disableWebPagePreview`
- оборачивает `TelegramApiException` в `TelegramSendException`
- `sendMany()` отправляет в цикле по всем chat id

Для тестов:
- есть дополнительный конструктор с подменяемым `DefaultAbsSender`, поэтому можно тестировать без сети и без реального Telegram API.
