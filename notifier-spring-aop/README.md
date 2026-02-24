# notifier-spring-aop

Provides `TelegramNotifyAspect` for methods annotated with `@TelegramNotify`.

Pointcut:

```java
@Around("@annotation(ann)")
```

Behavior:

- resolves interface method to target class method when needed
- supports `NotifyWhen`:
  - `BEFORE`
  - `AFTER_SUCCESS`
  - `AFTER_FAILURE`
  - `AFTER_FINALLY`
- builds `MethodInvocationContext` and delegates to `TelegramNotificationDispatcher`
