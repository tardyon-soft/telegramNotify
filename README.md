# telegram-notifier

`telegram-notifier` - библиотека для отправки Telegram-уведомлений через аннотации в Spring-приложениях.

Публикуемые артефакты:
- `ru.tardyon.maven:telegram-notifier-boot2-starter` - Spring Boot `2.3.x`, Java `11`
- `ru.tardyon.maven:telegram-notifier-boot3-starter` - Spring Boot `3.4.x`, Java `21`

В Maven-публикацию уходят только два starter-артефакта. Классы внутренних модулей встраиваются внутрь starter JAR.

## Состав репозитория

- `notifier-core` - аннотации, dispatcher, контракты конфигурации и отправки
- `notifier-template-spel` - `TemplateEngine` на Spring SpEL
- `notifier-spring-aop` - аспект `@TelegramNotify`
- `notifier-telegrambots-adapter` - outbound-адаптер на `org.telegram:telegrambots`
- `notifier-boot2-starter` - автоконфигурация для Spring Boot 2
- `notifier-boot3-starter` - автоконфигурация для Spring Boot 3
- `sample-boot2-app` - пример для Boot 2
- `sample-boot3-app` - пример для Boot 3

## Возможности

- единый API для Boot 2 и Boot 3
- уведомления до вызова метода, после успеха, после ошибки и в `finally`
- SpEL-шаблоны для `message` и `condition`
- override `chatIds` на уровне метода
- `parseMode`: `PLAIN`, `HTML`, `MARKDOWN`, `MARKDOWN_V2`
- `errorPolicy`: `LOG_ONLY`, `THROW`
- async dispatch через `Executor`
- proxy-конфиг для `telegrambots`: `HTTP`, `SOCKS4`, `SOCKS5`

## Подключение

### Spring Boot 2

```groovy
implementation "ru.tardyon.maven:telegram-notifier-boot2-starter:<version>"
```

### Spring Boot 3

```groovy
implementation "ru.tardyon.maven:telegram-notifier-boot3-starter:<version>"
```

## Конфигурация

Рекомендуемый формат:

```yaml
telegram:
  notifier:
    enabled: true
    bot:
      token: ${TELEGRAM_BOT_TOKEN:}
      username: ${TELEGRAM_BOT_USERNAME:}
    targets:
      chat-ids: ${TELEGRAM_CHAT_ID:}
    parse-mode: HTML
    disable-web-page-preview: true
    async:
      enabled: true
    error-policy: LOG_ONLY
    proxy:
      enabled: false
      type: SOCKS5
      host: 127.0.0.1
      port: 1080
      username: ${TELEGRAM_PROXY_USERNAME:}
      password: ${TELEGRAM_PROXY_PASSWORD:}
```

Поддерживается и плоский формат:

```yaml
telegram:
  notifier:
    enabled: true
    token: ${TELEGRAM_BOT_TOKEN:}
    username: ${TELEGRAM_BOT_USERNAME:}
    chat-ids: ${TELEGRAM_CHAT_ID:}
    async-enabled: true
```

Для активации автоконфигурации должны быть заполнены:
- `token`
- `username`
- `chat-ids`

Если хотя бы одно из этих значений пустое, starter не создает notifier-бины.

## Пример использования

```java
import org.springframework.stereotype.Service;
import ru.tardyon.maven.telegram.notifier.core.annotation.NotifyWhen;
import ru.tardyon.maven.telegram.notifier.core.annotation.TelegramNotify;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ErrorPolicy;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ParseMode;

@Service
public class PaymentService {
  @TelegramNotify(
      message = "'[BEFORE] payment=' + #paymentId + ', amount=' + #amount",
      condition = "#amount > 0",
      when = NotifyWhen.BEFORE
  )
  public String start(String paymentId, long amount) {
    return "accepted-" + paymentId;
  }

  @TelegramNotify(
      message = "'<b>[SUCCESS]</b> payment=' + #paymentId + ', result=' + #result",
      condition = "#result.startsWith('ok')",
      when = NotifyWhen.AFTER_SUCCESS,
      parseMode = ParseMode.HTML
  )
  public String success(String paymentId) {
    return "ok-" + paymentId;
  }

  @TelegramNotify(
      message = "'[FAILURE] payment=' + #paymentId + ', ex=' + #ex.message",
      when = NotifyWhen.AFTER_FAILURE,
      errorPolicy = ErrorPolicy.LOG_ONLY,
      chatIds = {-1001234567890L}
  )
  public void fail(String paymentId) {
    throw new IllegalStateException("payment failed for " + paymentId);
  }
}
```

## Доступные SpEL-переменные

В `message` и `condition` доступны:
- `#args`
- `#p0`, `#p1`, ...
- `#a0`, `#a1`, ...
- именованные параметры метода при компиляции с `-parameters`
- `#result`
- `#ex`
- `#methodName`
- `#className`

Примеры:

```java
@TelegramNotify(message = "'order=' + #orderId + ', result=' + #result")
public String handle(String orderId) { ... }

@TelegramNotify(
    message = "'failure=' + #ex.message",
    condition = "#ex != null",
    when = NotifyWhen.AFTER_FAILURE
)
public void process(String orderId) { ... }
```

## Proxy в `telegrambots`

Библиотека `org.telegram:telegrambots` уже поддерживает proxy через `DefaultBotOptions`.
В `telegram-notifier` эта поддержка пробрасывается через `telegram.notifier.proxy.*`.

Поддерживаемые поля:
- `enabled`
- `type`: `HTTP`, `SOCKS4`, `SOCKS5`
- `host`
- `port`
- `username`
- `password`

Ограничение: `telegrambots` нативно надежно покрывает `host/port/type`. Если потребуется отдельная реализация сложной proxy-аутентификации на уровне транспорта, ее нужно добавлять поверх стандартного `DefaultBotOptions`.

## Sample-приложения

Оба sample используют namespace `ru.tardyon.maven.telegram.notifier.sample.*` и показывают:
- `NotifyWhen.BEFORE`
- `NotifyWhen.AFTER_SUCCESS`
- `NotifyWhen.AFTER_FAILURE`
- `NotifyWhen.AFTER_FINALLY`

Запуск:

```bash
export TELEGRAM_BOT_TOKEN=...
export TELEGRAM_BOT_USERNAME=...
export TELEGRAM_CHAT_ID=-1001234567890

./gradlew :sample-boot2-app:bootRun
./gradlew :sample-boot3-app:bootRun
```

Если env-переменные не заданы, приложения стартуют, но notifier-бины не поднимаются по condition.

## Сборка и тесты

```bash
./gradlew clean test
```

Проверка структуры проекта:

```bash
./gradlew projects
```

Локальная публикация starter-артефактов:

```bash
./gradlew :notifier-boot2-starter:publishToMavenLocal
./gradlew :notifier-boot3-starter:publishToMavenLocal
```

## GitLab CI/CD

Файл пайплайна: `/Users/sergej/Documents/telegramNotify/.gitlab-ci.yml`

Текущие стадии:
- `build` - `./gradlew --no-daemon --stacktrace clean assemble`
- `test` - `./gradlew --no-daemon --stacktrace test`
- `publish` - публикация релиза по тегу `vX.Y.Z`

Для publish job используются переменные окружения:
- `JRELEASER_MAVENCENTRAL_USERNAME`
- `JRELEASER_MAVENCENTRAL_PASSWORD`
- `JRELEASER_GPG_PUBLIC_KEY`
- `JRELEASER_GPG_SECRET_KEY`
- `JRELEASER_GPG_PASSPHRASE`

## Лицензирование и публикация

`groupId` проекта: `ru.tardyon.maven`

Публикуются только:
- `ru.tardyon.maven:telegram-notifier-boot2-starter`
- `ru.tardyon.maven:telegram-notifier-boot3-starter`
