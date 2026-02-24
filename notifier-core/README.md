# notifier-core

Core contracts:

- annotations: `@TelegramNotify`, `NotifyWhen`
- invocation context: `MethodInvocationContext`
- dispatch model: `NotificationRequest`, `NotificationOptions`, `ParseMode`, `ErrorPolicy`
- dispatcher: `TelegramNotificationDispatcher`
- sender abstraction: `TelegramSender`
- templating abstraction: `TemplateEngine`

Example dispatcher usage:

```java
NotificationRequest request = NotificationRequest.of(
    "'Hello ' + #p0",
    "true",
    null,
    ParseMode.HTML,
    ErrorPolicy.LOG_ONLY,
    new MethodInvocationContext(target, method, args, result, null)
);
dispatcher.dispatch(request);
```
