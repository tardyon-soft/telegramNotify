# notifier-boot3-starter

Публикуемый starter для Spring Boot `3.4.x` и Java `21`.

Артефакт:
- `ru.tardyon.maven:telegram-notifier-boot3-starter`

## Особенности

- использует `@AutoConfiguration`
- подключается через `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## Что поднимает автоконфигурация

- `TelegramNotifierProperties`
- `Boot3NotifierConfigAdapter`
- `TelegramNotifierEnabledCondition`
- `TemplateEngine`
- `TaskExecutor` (`telegramNotifierExecutor`)
- `TelegramSender`
- `TelegramNotificationDispatcher`
- `TelegramNotifyAspect`

## Условия активации

- `telegram.notifier.enabled=true` или свойство отсутствует
- заполнены `token`, `username`, `chat-ids`

## Конфигурация

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
    proxy:
      enabled: false
      type: SOCKS5
      host: 127.0.0.1
      port: 1080
```
