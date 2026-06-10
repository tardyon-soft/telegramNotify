# notifier-template-spel

`notifier-template-spel` - реализация `TemplateEngine` на Spring SpEL.

Основной класс:
- `DefaultSpelTemplateEngine`

## Возможности

- кеширует `Expression` по строке
- поддерживает `message` и `condition`
- пустой `condition` трактует как `true`
- пустой `message` трактует как пустую строку
- использует `DefaultParameterNameDiscoverer` для имен параметров

## Доступные переменные

- `#args`
- `#p0`, `#p1`, ...
- `#a0`, `#a1`, ...
- `#result`
- `#ex`
- `#methodName`
- `#className`
- именованные параметры метода при `-parameters`

## Пример

```java
String message = engine.render("'order=' + #orderId + ', result=' + #result", ctx);
boolean matched = engine.evaluateCondition("#result != null", ctx);
```
