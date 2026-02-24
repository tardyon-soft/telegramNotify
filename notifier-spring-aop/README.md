# notifier-spring-aop

Модуль интеграции с Spring AOP.

Содержит:
- `TelegramNotifyAspect`

Pointcut:

```java
@Around("@annotation(ann)")
```

Что делает аспект:
- корректно резолвит `Method` (если сигнатура взята с интерфейса, пробует метод target-класса)
- поддерживает все режимы `NotifyWhen`:
  - `BEFORE`
  - `AFTER_SUCCESS`
  - `AFTER_FAILURE`
  - `AFTER_FINALLY`
- формирует `MethodInvocationContext` и передает его в `TelegramNotificationDispatcher`

Модуль не зависит от Spring Boot и может использоваться в чистом Spring-контексте.
