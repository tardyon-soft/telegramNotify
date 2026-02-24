# telegram-notifier

Библиотека для отправки уведомлений в Telegram через аннотации в Spring.

Публикуемые артефакты:
- `ru.wildred:telegram-notifier-boot2-starter` (Spring Boot `2.3.x`, Java `11`)
- `ru.wildred:telegram-notifier-boot3-starter` (Spring Boot `3.4.x`, Java `21`)

Внутренние модули (`core`, `spel`, `spring-aop`, `telegrambots-adapter`) встраиваются внутрь starter JAR.

## Структура

- `notifier-core` - API, аннотации, dispatcher, контракты конфигурации.
- `notifier-template-spel` - шаблонизатор на Spring SpEL.
- `notifier-spring-aop` - аспект `@TelegramNotify`.
- `notifier-telegrambots-adapter` - адаптер отправки через `telegrambots`.
- `notifier-boot2-starter` - автоконфигурация для Boot 2.
- `notifier-boot3-starter` - автоконфигурация для Boot 3.
- `sample-boot2-app` - пример приложения Boot 2.
- `sample-boot3-app` - пример приложения Boot 3.

## Быстрый старт

### 1. Подключить зависимость

Boot 2:

```groovy
implementation "ru.wildred:telegram-notifier-boot2-starter:<version>"
```

Boot 3:

```groovy
implementation "ru.wildred:telegram-notifier-boot3-starter:<version>"
```

### 2. Настроить `application.yml`

Поддерживаются оба формата конфигурации.

Рекомендуемый вложенный формат:

```yaml
telegram:
  notifier:
    enabled: true
    bot:
      token: ${TELEGRAM_BOT_TOKEN:}
      username: ${TELEGRAM_BOT_USERNAME:}
    targets:
      chat-ids:
        - ${TELEGRAM_CHAT_ID:}
    parse-mode: HTML
    disable-web-page-preview: true
    async:
      enabled: true
    error-policy: LOG_ONLY
```

Также поддерживается плоский формат:

```yaml
telegram:
  notifier:
    enabled: true
    token: ${TELEGRAM_BOT_TOKEN:}
    username: ${TELEGRAM_BOT_USERNAME:}
    chat-ids: ${TELEGRAM_CHAT_ID:}
    async-enabled: true
```

Обязательные параметры для активации авто-конфигурации:
- `token`
- `username`
- `chat-ids`

Если хотя бы один из них пустой, notifier отключается condition-ом.

### 3. Поставить аннотацию на метод

```java
@Service
public class OrderService {
    @TelegramNotify(
            message = "'Заказ ' + #orderId + ' обработан, result=' + #result",
            condition = "#result != null",
            when = NotifyWhen.AFTER_SUCCESS
    )
    public String processOrder(String orderId) {
        return "OK";
    }
}
```

Режимы `NotifyWhen`:
- `BEFORE`
- `AFTER_SUCCESS`
- `AFTER_FAILURE`
- `AFTER_FINALLY`

## SpEL-переменные

В шаблонах `message` и `condition` доступны:
- `#args`, `#p0..`, `#a0..`
- `#result`
- `#ex`
- `#methodName`
- `#className`
- именованные параметры метода (при `-parameters`)

## Примеры использования

Уведомление до, после успеха и при ошибке:

```java
@Service
public class PaymentService {
    @TelegramNotify(
            message = "'Старт платежа id=' + #paymentId + ', amount=' + #amount",
            when = NotifyWhen.BEFORE
    )
    @TelegramNotify(
            message = "'Платеж OK id=' + #paymentId + ', result=' + #result",
            when = NotifyWhen.AFTER_SUCCESS,
            parseMode = ParseMode.HTML
    )
    @TelegramNotify(
            message = "'Платеж FAIL id=' + #paymentId + ', ex=' + #ex.message",
            when = NotifyWhen.AFTER_FAILURE,
            errorPolicy = ErrorPolicy.LOG_ONLY
    )
    public String pay(String paymentId, long amount) {
        return "OK";
    }
}
```

Переопределение chat id на уровне метода:

```java
@TelegramNotify(
        message = "'Критическая ошибка: ' + #p0",
        chatIds = {-1001234567890L, -1009876543210L},
        when = NotifyWhen.AFTER_FAILURE
)
public void critical(String msg) {
    throw new IllegalStateException(msg);
}
```

## Локальный запуск и тесты

Проверка проекта:

```bash
./gradlew clean test
```

Запуск sample-приложений:

```bash
export TELEGRAM_BOT_TOKEN=...
export TELEGRAM_BOT_USERNAME=...
export TELEGRAM_CHAT_ID=-1001234567890

./gradlew :sample-boot2-app:bootRun
./gradlew :sample-boot3-app:bootRun
```

Без обязательных параметров приложения стартуют, но notifier-бин не создается.

## CI/CD (GitLab)

Файл: `.gitlab-ci.yml`

- `build` stage: `./gradlew --no-daemon clean test`
- `publish` stage: только по тегу `vX.Y.Z`, публикация двух starter-артефактов в приватный Maven

Переменные для публикации:
- `MAVEN_REPO_URL`
- `MAVEN_REPO_USERNAME`
- `MAVEN_REPO_PASSWORD`
- `MAVEN_REPO_ALLOW_INSECURE` (`true/false`, опционально)
