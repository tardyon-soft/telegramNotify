# sample-boot3-app

Demo application for Spring Boot `3.4.1-SNAPSHOT`.

Run:

```bash
export TELEGRAM_BOT_TOKEN=...
export TELEGRAM_BOT_USERNAME=...
export TELEGRAM_CHAT_ID=123456789
./gradlew :sample-boot3-app:bootRun
```

Demo service:

- `DemoService#beforeNotification(String,long)` -> `NotifyWhen.BEFORE` + condition
- `DemoService#successNotification(String)` -> `NotifyWhen.AFTER_SUCCESS` + HTML parse mode
- `DemoService#failureNotification(String)` -> `NotifyWhen.AFTER_FAILURE` + `ErrorPolicy.LOG_ONLY`
- `DemoService#finallyNotification(String,boolean)` -> `NotifyWhen.AFTER_FINALLY` + per-method `chatIds` override

Runner executes all scenarios and includes forced failure paths for demonstration.
