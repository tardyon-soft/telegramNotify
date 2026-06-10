# notifier-core

`notifier-core` - базовый модуль библиотеки `telegram-notifier`.

Содержит:
- `@TelegramNotify`, `NotifyWhen`
- `MethodInvocationContext`
- `NotifierConfig`
- `NotificationRequest`, `NotificationOptions`
- `ParseMode`, `ErrorPolicy`
- `TelegramNotificationDispatcher`
- `TelegramSender`, `TelegramSendException`
- `TemplateEngine`
- `ProxyType`

## Поведение dispatcher

`TelegramNotificationDispatcher`:
- вычисляет `condition` и `message` через `TemplateEngine`
- не отправляет сообщение, если `condition=false`
- не отправляет сообщение, если итоговый `message` пустой
- использует `chatIds` из аннотации, иначе берет `defaultChatIds()` из `NotifierConfig`
- поддерживает async-режим, если `asyncEnabled()` возвращает `true` и задан `Executor`
- применяет `ErrorPolicy.LOG_ONLY` или `ErrorPolicy.THROW`

## Минимальный пример

```java
NotificationRequest request = NotificationRequest.of(
    "'method=' + #methodName + ', result=' + #result",
    "#result != null",
    null,
    ParseMode.HTML,
    ErrorPolicy.LOG_ONLY,
    new MethodInvocationContext(target, method, args, result, null)
);

dispatcher.dispatch(request);
```
