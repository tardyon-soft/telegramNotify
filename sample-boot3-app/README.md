# sample-boot3-app

Пример приложения для Spring Boot `3.4.1-SNAPSHOT` и Java `21`.

Namespace:
- `ru.tardyon.maven.telegram.notifier.sample.boot3`

## Что показывает пример

`DemoService` содержит 4 сценария:
- `beforeNotification(String accountId)` - `NotifyWhen.BEFORE`
- `successNotification(String accountId)` - `NotifyWhen.AFTER_SUCCESS`
- `failureNotification(String accountId)` - `NotifyWhen.AFTER_FAILURE`
- `finallyNotification(String accountId, boolean fail)` - `NotifyWhen.AFTER_FINALLY`

В примерах используются:
- `#methodName`
- именованный параметр `#accountId`
- `#result`
- `#ex`
- `ParseMode.HTML`
- `ErrorPolicy.LOG_ONLY`

## Конфигурация

`/Users/sergej/Documents/telegramNotify/sample-boot3-app/src/main/resources/application.yml`

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

./gradlew :sample-boot3-app:bootRun
```

Если обязательные параметры пустые, приложение стартует без notifier-бинов.
