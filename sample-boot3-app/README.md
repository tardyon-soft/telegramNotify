# sample-boot3-app

Пример приложения для Spring Boot `3.4.1-SNAPSHOT` (Java `21`).

Назначение:
- показать тот же API, что и в Boot2-стартере
- продемонстрировать режимы `NotifyWhen` и расширенные настройки (`parse-mode`, `error-policy`, override `chatIds`)
- проверить включение/выключение auto-configuration по condition

Переменные окружения:
- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_BOT_USERNAME`
- `TELEGRAM_CHAT_ID`

Запуск:

```bash
export TELEGRAM_BOT_TOKEN=...
export TELEGRAM_BOT_USERNAME=...
export TELEGRAM_CHAT_ID=-1001234567890

./gradlew :sample-boot3-app:bootRun
```

В `DemoService`:
- `beforeNotification(String,long)` - `NotifyWhen.BEFORE`
- `successNotification(String)` - `NotifyWhen.AFTER_SUCCESS` + `ParseMode.HTML`
- `failureNotification(String)` - `NotifyWhen.AFTER_FAILURE` + `ErrorPolicy.LOG_ONLY`
- `finallyNotification(String,boolean)` - `NotifyWhen.AFTER_FINALLY` + override chat id
