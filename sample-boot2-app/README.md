# sample-boot2-app

Demo application for Spring Boot `2.3.0.RELEASE`.

Run:

```bash
export TELEGRAM_BOT_TOKEN=...
export TELEGRAM_BOT_USERNAME=...
export TELEGRAM_CHAT_ID=123456789
./gradlew :sample-boot2-app:bootRun
```

Demo service:

- `DemoService#beforeNotification(String,long)` -> `NotifyWhen.BEFORE` + condition
- `DemoService#successNotification(String)` -> `NotifyWhen.AFTER_SUCCESS` + `#result`
- `DemoService#failureNotification(String)` -> `NotifyWhen.AFTER_FAILURE` + `#ex.message`
- `DemoService#finallyNotification(String,boolean)` -> `NotifyWhen.AFTER_FINALLY`

Runner calls all scenarios and intentionally triggers failures to demonstrate failure/finally notifications.
