# telegram-notifier

Telegram notification library for Spring with two published starter artifacts:

- `ru.tardyon:telegram-notifier-boot2-starter` (Spring Boot `2.3.x`, Java `11`)
- `ru.tardyon:telegram-notifier-boot3-starter` (Spring Boot `3.4.x`, Java `21`)

Internal modules (`core`, `spel`, `spring-aop`, `telegrambots-adapter`) are bundled inside starter JARs.

## Module Structure

- `notifier-core` - API contracts, annotations, dispatcher, config interfaces.
- `notifier-template-spel` - `TemplateEngine` implementation based on Spring SpEL.
- `notifier-spring-aop` - `@TelegramNotify` aspect integration.
- `notifier-telegrambots-adapter` - outbound Telegram sender adapter.
- `notifier-boot2-starter` - Spring Boot 2 auto-configuration.
- `notifier-boot3-starter` - Spring Boot 3 auto-configuration.
- `sample-boot2-app` - runnable sample for Boot 2.
- `sample-boot3-app` - runnable sample for Boot 3.

## Quick Start

### 1) Add dependency

Boot 2:

```groovy
implementation "ru.tardyon:telegram-notifier-boot2-starter:<version>"
```

Boot 3:

```groovy
implementation "ru.tardyon:telegram-notifier-boot3-starter:<version>"
```

### 2) Configure application

```yaml
telegram:
  notifier:
    enabled: true
    token: ${TELEGRAM_BOT_TOKEN:}
    username: ${TELEGRAM_BOT_USERNAME:}
    chat-ids: ${TELEGRAM_CHAT_ID:}
    parse-mode: HTML
    disable-web-page-preview: true
```

Required env vars:

- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_BOT_USERNAME`
- `TELEGRAM_CHAT_ID`

If `token`, `username`, or `chat-ids` are empty, auto-configuration is disabled by condition.

### 3) Annotate service methods

```java
@Service
public class OrderService {
    @TelegramNotify(
        message = "'Order ' + #orderId + ' processed, result=' + #result",
        condition = "#result != null",
        when = NotifyWhen.AFTER_SUCCESS
    )
    public String processOrder(String orderId) {
        return "OK";
    }
}
```

Supported `NotifyWhen` modes:

- `BEFORE`
- `AFTER_SUCCESS`
- `AFTER_FAILURE`
- `AFTER_FINALLY`

## Build And Test

```bash
./gradlew clean test
```

## Run Samples

```bash
export TELEGRAM_BOT_TOKEN=...
export TELEGRAM_BOT_USERNAME=...
export TELEGRAM_CHAT_ID=123456789
./gradlew :sample-boot2-app:bootRun
./gradlew :sample-boot3-app:bootRun
```

Without token/username/chat-id samples still start, notifier beans stay disabled by condition.

## Advanced Examples

### Success + Failure Notifications For One Operation

```java
@Service
public class PaymentService {
    @TelegramNotify(
        message = "'Payment started: id=' + #paymentId + ', amount=' + #amount",
        when = NotifyWhen.BEFORE,
        condition = "#amount > 0"
    )
    @TelegramNotify(
        message = "'Payment SUCCESS: id=' + #paymentId + ', result=' + #result",
        when = NotifyWhen.AFTER_SUCCESS,
        parseMode = ParseMode.HTML
    )
    @TelegramNotify(
        message = "'Payment FAILED: id=' + #paymentId + ', ex=' + #ex.message",
        when = NotifyWhen.AFTER_FAILURE,
        errorPolicy = ErrorPolicy.LOG_ONLY
    )
    public String pay(String paymentId, long amount) {
        return "OK";
    }
}
```

### Notify Only On Business Condition

```java
@TelegramNotify(
    message = "'High latency detected: ' + #result + ' ms'",
    condition = "#result != null && #result > 500",
    when = NotifyWhen.AFTER_SUCCESS
)
public Long measureLatency() {
    return externalCallLatency();
}
```

### Use Named Parameters In SpEL

`-parameters` is enabled in this project, so method argument names are available:

```java
@TelegramNotify(
    message = "'Order=' + #orderId + ', user=' + #userId + ', result=' + #result",
    condition = "#orderId != null && #result != null"
)
public String process(String orderId, Long userId) {
    return "DONE";
}
```

### Override Chat IDs Per Method

```java
@TelegramNotify(
    message = "'Critical alert: ' + #p0",
    chatIds = {123456789L, 987654321L},
    when = NotifyWhen.AFTER_FAILURE
)
public void critical(String msg) {
    throw new IllegalStateException(msg);
}
```

### Custom Template Engine Bean

```java
@Configuration
public class NotifierCustomization {
    @Bean
    public TemplateEngine templateEngine() {
        return (template, ctx) -> "[env=prod] " + template;
    }
}
```

`@ConditionalOnMissingBean` in starter will keep your custom bean.

### Custom Sender Bean (Mock / Queue / Proxy)

```java
@Configuration
public class NotifierCustomization {
    @Bean
    public TelegramSender telegramSender() {
        return (chatId, message, parseMode) -> {
            // route to queue, audit, or stub in tests
        };
    }
}
```

### Async Delivery Tuning

```yaml
telegram:
  notifier:
    async-enabled: true
    executor-core-pool-size: 2
    executor-max-pool-size: 8
    executor-queue-capacity: 1000
```

When `async-enabled=true`, dispatcher uses `telegramNotifierExecutor`.

## Publishing

GitLab CI has:

- `build` stage: `./gradlew clean test`
- `publish` stage: runs only on tag `vX.Y.Z` and publishes only two starter artifacts via JReleaser + Maven Central Portal user token.
