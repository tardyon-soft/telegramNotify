# sample-boot2-app

Пример приложения для Spring Boot `2.3.0.RELEASE` и Java `11`.

Namespace:
- `ru.tardyon.maven.telegram.notifier.sample.boot2`

## Что показывает пример

`DemoService` содержит 4 сценария:
- `beforeNotification(String orderId)` - `NotifyWhen.BEFORE`
- `successNotification(String orderId)` - `NotifyWhen.AFTER_SUCCESS`
- `failureNotification(String orderId)` - `NotifyWhen.AFTER_FAILURE`
- `finallyNotification(String orderId, boolean fail)` - `NotifyWhen.AFTER_FINALLY`

В примерах используются:
- `#methodName`
- именованный параметр `#orderId`
- `#result`
- `#ex`
- `ParseMode.MARKDOWN`
- `ParseMode.HTML`

## Конфигурация

`/Users/sergej/Documents/telegramNotify/sample-boot2-app/src/main/resources/application.yml`

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
```

## Запуск

```bash
export TELEGRAM_BOT_TOKEN=...
export TELEGRAM_BOT_USERNAME=...
export TELEGRAM_CHAT_ID=-1001234567890

./gradlew :sample-boot2-app:bootRun
```

Если обязательные параметры пустые, приложение стартует без notifier-бинов.
