# sample-boot2-app

Пример приложения для Spring Boot `2.3.0.RELEASE` (Java `11`).

Назначение:
- показать работу `@TelegramNotify` в режимах `BEFORE/AFTER_SUCCESS/AFTER_FAILURE/AFTER_FINALLY`
- показать использование SpEL-переменных `#p0`, `#result`, `#ex`
- проверить, что при пустых обязательных настройках авто-конфигурация корректно отключается

Переменные окружения:
- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_BOT_USERNAME`
- `TELEGRAM_CHAT_ID`

Запуск:

```bash
export TELEGRAM_BOT_TOKEN=...
export TELEGRAM_BOT_USERNAME=...
export TELEGRAM_CHAT_ID=-1001234567890

./gradlew :sample-boot2-app:bootRun
```

В `DemoService`:
- `beforeNotification(String,long)` - уведомление до выполнения
- `successNotification(String)` - уведомление после успешного результата
- `failureNotification(String)` - уведомление при исключении
- `finallyNotification(String,boolean)` - уведомление в `finally`
