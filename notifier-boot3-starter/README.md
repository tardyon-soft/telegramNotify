# notifier-boot3-starter

Стартер автоконфигурации для Spring Boot `3.4.x` (Java `21`).

Публикуемый артефакт:
- `ru.wildred:telegram-notifier-boot3-starter`

Особенности Boot 3:
- использует `@AutoConfiguration`
- регистрация через ресурс  
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

Поднимаемые компоненты:
- `TelegramNotifierProperties`, `TelegramNotifierEnabledCondition`, `Boot3NotifierConfigAdapter`
- `TemplateEngine` (SpEL)
- `TelegramSender`, `TelegramNotificationDispatcher`
- `TelegramNotifyAspect`

Условия активации:
- `telegram.notifier.enabled=true` (или отсутствует)
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
    error-policy: LOG_ONLY
    async:
      enabled: true
```
