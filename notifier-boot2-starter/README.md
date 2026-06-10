# notifier-boot2-starter

Публикуемый starter для Spring Boot `2.3.x` и Java `11`.

Артефакт:
- `ru.tardyon.maven:telegram-notifier-boot2-starter`

## Что поднимает автоконфигурация

- `TelegramNotifierProperties`
- `Boot2NotifierConfigAdapter`
- `TelegramNotifierEnabledCondition`
- `TemplateEngine`
- `TaskExecutor` (`telegramNotifierExecutor`)
- `TelegramSender`
- `TelegramNotificationDispatcher`
- `TelegramNotifyAspect`

Все бины создаются через `@ConditionalOnMissingBean`.

## Условия активации

- `telegram.notifier.enabled=true` или свойство отсутствует
- заполнены `token`, `username`, `chat-ids`

## Конфигурация

Поддерживаются:
- плоский формат
- вложенный формат

Рекомендуемый пример:

```yaml
telegram:
  notifier:
    enabled: true
    bot:
      token: ${TELEGRAM_BOT_TOKEN:}
      username: ${TELEGRAM_BOT_USERNAME:}
    targets:
      chat-ids: ${TELEGRAM_CHAT_ID:}
    parse-mode: HTML
    disable-web-page-preview: true
    async:
      enabled: true
    error-policy: LOG_ONLY
    executor-core-pool-size: 2
    executor-max-pool-size: 4
    executor-queue-capacity: 500
    proxy:
      enabled: false
      type: SOCKS5
      host: 127.0.0.1
      port: 1080
```
