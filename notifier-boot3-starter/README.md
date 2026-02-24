# notifier-boot3-starter

Auto-configuration starter for Spring Boot `3.4.x` (Java 21).

Published artifact:

- `ru.tardyon:telegram-notifier-boot3-starter`

Uses `@AutoConfiguration` and resource:

- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

Includes same runtime components as Boot2 starter:

- properties, condition, config adapter
- SpEL template engine
- telegram sender and dispatcher
- AOP aspect

Activation conditions are the same (`enabled + token + username + chat-ids`).

Example config:

```yaml
telegram:
  notifier:
    enabled: true
    token: ${TELEGRAM_BOT_TOKEN:}
    username: ${TELEGRAM_BOT_USERNAME:}
    chat-ids: ${TELEGRAM_CHAT_ID:}
    parse-mode: MARKDOWN_V2
    error-policy: LOG_ONLY
    async-enabled: true
```

Typical annotation usage:

```java
@TelegramNotify(
    message = "'Job done: ' + #methodName + ', result=' + #result",
    condition = "#result != null",
    when = NotifyWhen.AFTER_SUCCESS
)
public String runJob() {
    return "OK";
}
```
