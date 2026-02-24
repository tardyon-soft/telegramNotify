# notifier-boot2-starter

Auto-configuration starter for Spring Boot `2.3.x` (Java 11).

Published artifact:

- `ru.tardyon:telegram-notifier-boot2-starter`

Auto-configuration includes:

- `TelegramNotifierProperties` (`telegram.notifier.*`)
- `Boot2NotifierConfigAdapter`
- `TelegramNotifierEnabledCondition`
- beans (all `@ConditionalOnMissingBean`):
  - `NotifierConfig`
  - `TemplateEngine` (SpEL)
  - `TaskExecutor` (`telegramNotifierExecutor`)
  - `TelegramSender`
  - `TelegramNotificationDispatcher`
  - `TelegramNotifyAspect`

Activation conditions:

- `telegram.notifier.enabled=true` (or missing)
- `token` present
- `username` present
- `chat-ids` not empty

Example config:

```yaml
telegram:
  notifier:
    enabled: true
    token: ${TELEGRAM_BOT_TOKEN:}
    username: ${TELEGRAM_BOT_USERNAME:}
    chat-ids: ${TELEGRAM_CHAT_ID:}
    parse-mode: HTML
    disable-web-page-preview: true
    async-enabled: true
    executor-core-pool-size: 2
    executor-max-pool-size: 4
    executor-queue-capacity: 500
```

Override default beans:

```java
@Configuration
public class NotifierOverrides {
    @Bean
    public TelegramSender telegramSender() {
        return (chatId, message, parseMode) -> {};
    }
}
```
