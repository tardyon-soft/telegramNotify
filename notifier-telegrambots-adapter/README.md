# notifier-telegrambots-adapter

Telegram outbound adapter:

- `DefaultTelegramSender implements TelegramSender`
- creates `DefaultAbsSender` from `NotifierConfig.botToken()`
- maps parse modes:
  - `PLAIN` -> none
  - `HTML` -> `HTML`
  - `MARKDOWN` -> `Markdown`
  - `MARKDOWN_V2` -> `MarkdownV2`
- applies `disableWebPagePreview`
- wraps `TelegramApiException` into `TelegramSendException`
- `sendMany()` iterates over chat IDs

Testability:

- second constructor accepts injected sender executor, so tests can run without real Telegram network calls.
