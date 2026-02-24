# notifier-template-spel

Модуль шаблонизатора на Spring SpEL.

Реализация:
- `DefaultSpelTemplateEngine implements TemplateEngine`

Возможности:
- кеш `Expression` по строке выражения
- переменные в контексте:
  - `#args`, `#p0..`, `#a0..`
  - `#result`, `#ex`
  - `#methodName`, `#className`
  - именованные параметры метода через `DefaultParameterNameDiscoverer`
- пустой `condition` трактуется как `true`
- пустой `message` трактуется как `""`

Пример:

```java
String message = engine.render("'Заказ=' + #orderId + ', result=' + #result", ctx);
boolean shouldSend = engine.evaluateCondition("#result != null", ctx);
```
