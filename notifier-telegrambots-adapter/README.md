# notifier-telegrambots-adapter

`notifier-telegrambots-adapter` - outbound-адаптер отправки сообщений через `org.telegram:telegrambots`.

Основной класс:
- `DefaultTelegramSender`

## Что делает

- создает `DefaultAbsSender` по `NotifierConfig.botToken()`
- формирует `SendMessage`
- применяет `parseMode`
- применяет `disableWebPagePreview`
- оборачивает `TelegramApiException` в `TelegramSendException`
- отправляет несколько сообщений через `sendMany()`

## Proxy

Адаптер использует нативные возможности `telegrambots` через `DefaultBotOptions`:
- `HTTP`
- `SOCKS4`
- `SOCKS5`

Поля для конфигурации:
- `proxyEnabled()`
- `proxyType()`
- `proxyHost()`
- `proxyPort()`
- `proxyUsername()`
- `proxyPassword()`

На текущем этапе гарантированно используется proxy mapping `type/host/port`.

## Тестирование

Для unit-тестов есть конструктор с подменяемым `DefaultAbsSender` и вариант с `TelegramRequestExecutor`, поэтому адаптер можно проверять без реального доступа к Telegram API.
