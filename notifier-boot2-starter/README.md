# notifier-boot2-starter

Стартер автоконфигурации для Spring Boot `2.3.x` (Java `11`).

Публикуемый артефакт:
- `ru.tardyon.maven:telegram-notifier-boot2-starter`

Что поднимает автоконфигурация:
- `TelegramNotifierProperties` (`telegram.notifier.*`)
- `Boot2NotifierConfigAdapter`
- `TelegramNotifierEnabledCondition`
- бины (`@ConditionalOnMissingBean`):
  - `NotifierConfig`
  - `TemplateEngine` (SpEL)
  - `TaskExecutor` (`telegramNotifierExecutor`)
  - `TelegramSender`
  - `TelegramNotificationDispatcher`
  - `TelegramNotifyAspect`

Условия активации:
- `telegram.notifier.enabled=true` (или свойство отсутствует)
- заполнены `token`, `username`, `chat-ids`

Поддерживаемые форматы конфигурации:
- плоский (`token`, `username`, `chat-ids`, `async-enabled`)
- вложенный (`bot.token`, `bot.username`, `targets.chat-ids`, `async.enabled`)

Рекомендуемый пример:

```yaml
telegram:
  notifier:
    enabled: true
    bot:
      token: ${TELEGRAM_BOT_TOKEN:}
      username: ${TELEGRAM_BOT_USERNAME:}
    targets:
      chat-ids:
        - ${TELEGRAM_CHAT_ID:}
    parse-mode: HTML
    disable-web-page-preview: true
    async:
      enabled: true
    executor-core-pool-size: 2
    executor-max-pool-size: 4
    executor-queue-capacity: 500
```
