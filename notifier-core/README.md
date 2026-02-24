# notifier-core

Базовый модуль библиотеки с API и доменной логикой уведомлений.

Содержит:
- аннотации `@TelegramNotify`, `NotifyWhen`
- контекст вызова `MethodInvocationContext`
- модель отправки `NotificationRequest`, `NotificationOptions`
- enums `ParseMode`, `ErrorPolicy`
- диспетчер `TelegramNotificationDispatcher`
- контракты `NotifierConfig`, `TelegramSender`, `TemplateEngine`

Поведение `TelegramNotificationDispatcher`:
- вычисляет `condition` и `message` через `TemplateEngine`
- не отправляет сообщение, если `condition=false` или текст пустой
- берет `chatIds` из аннотации, иначе из `NotifierConfig`
- поддерживает async при `asyncEnabled=true` и наличии `Executor`
- обрабатывает ошибки по `ErrorPolicy` (`LOG_ONLY` или `THROW`)

Пример:

```java
NotificationRequest request = NotificationRequest.of(
        "'Операция ' + #methodName + ', result=' + #result",
        "#result != null",
        null,
        ParseMode.HTML,
        ErrorPolicy.LOG_ONLY,
        new MethodInvocationContext(target, method, args, result, null)
);
dispatcher.dispatch(request);
```
