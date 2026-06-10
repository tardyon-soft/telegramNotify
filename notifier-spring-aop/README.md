# notifier-spring-aop

`notifier-spring-aop` - интеграция `telegram-notifier` со Spring AOP.

Основной класс:
- `TelegramNotifyAspect`

Pointcut:

```java
@Around("@annotation(ann)")
```

## Что делает аспект

- находит `@TelegramNotify` на методе
- корректно резолвит `Method`, если прокси работает через интерфейс
- формирует `MethodInvocationContext`
- вызывает `TelegramNotificationDispatcher`
- поддерживает:
  - `NotifyWhen.BEFORE`
  - `NotifyWhen.AFTER_SUCCESS`
  - `NotifyWhen.AFTER_FAILURE`
  - `NotifyWhen.AFTER_FINALLY`

Модуль не зависит от Spring Boot и может использоваться в обычном Spring-контексте с `@EnableAspectJAutoProxy`.
