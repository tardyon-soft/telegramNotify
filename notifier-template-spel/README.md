# notifier-template-spel

SpEL-based `TemplateEngine` implementation: `DefaultSpelTemplateEngine`.

Features:

- expression cache by expression string
- variables:
  - `#args`, `#p0..`, `#a0..`
  - `#result`, `#ex`
  - `#methodName`, `#className`
  - named parameters via `DefaultParameterNameDiscoverer`
- empty condition -> `true`
- empty message -> `""`

Example:

```java
String text = engine.render("'Order=' + #orderId + ', result=' + #result", ctx);
boolean enabled = engine.evaluateCondition("#result != null", ctx);
```
