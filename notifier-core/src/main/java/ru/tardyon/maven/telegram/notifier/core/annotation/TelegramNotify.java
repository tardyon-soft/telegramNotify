package ru.tardyon.maven.telegram.notifier.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ErrorPolicy;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ParseMode;

/**
 * Аннотация для отправки уведомлений в Telegram при выполнении методов.
 *
 * <p>Позволяет настроить автоматическую отправку уведомлений в Telegram-чаты
 * в различные моменты выполнения аннотированного метода:
 * <ul>
 *   <li>перед выполнением ({@link NotifyWhen#BEFORE})</li>
 *   <li>после успешного выполнения ({@link NotifyWhen#AFTER_SUCCESS})</li>
 *   <li>при возникновении исключения ({@link NotifyWhen#AFTER_FAILURE})</li>
 *   <li>в любом случае после выполнения ({@link NotifyWhen#AFTER_FINALLY})</li>
 * </ul>
 *
 * <p>Сообщение может содержать SpEL-выражения для доступа к параметрам метода,
 * результату выполнения и информации об исключении.
 *
 * <p>Пример использования:
 * <pre>{@code
 * @TelegramNotify(
 *     message = "'Заказ ' + #orderId + ' обработан с результатом: ' + #result",
 *     condition = "#result.startsWith('ok')",
 *     when = NotifyWhen.AFTER_SUCCESS,
 *     parseMode = ParseMode.HTML
 * )
 * public String processOrder(String orderId) {
 *     // логика обработки
 *     return "ok-" + orderId;
 * }
 * }</pre>
 *
 * @see NotifyWhen
 * @see ParseMode
 * @see ErrorPolicy
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TelegramNotify {
  /**
   * Текст уведомления.
   *
   * <p>Может содержать SpEL-выражения для динамического формирования сообщения.
   * Доступные переменные в контексте:
   * <ul>
   *   <li>{@code #methodName} - имя метода</li>
   *   <li>{@code #paramName} - параметры метода (по имени параметра)</li>
   *   <li>{@code #result} - результат выполнения метода (для AFTER_SUCCESS и AFTER_FINALLY)</li>
   *   <li>{@code #ex} - исключение (для AFTER_FAILURE и AFTER_FINALLY)</li>
   * </ul>
   *
   * @return текст сообщения с возможностью использования SpEL-выражений
   */
  String message();

  /**
   * Условие отправки уведомления в виде SpEL-выражения.
   *
   * <p>Уведомление будет отправлено только если данное условие вернет {@code true}.
   * Если условие не задано (пустая строка), уведомление отправляется всегда.
   *
   * <p>В выражении доступны те же переменные, что и в {@link #message()}.
   *
   * <p>Пример: {@code "#amount > 1000"} - отправить уведомление только если сумма больше 1000.
   *
   * @return SpEL-выражение для проверки условия или пустая строка (по умолчанию)
   */
  String condition() default "";

  /**
   * Момент отправки уведомления относительно выполнения метода.
   *
   * @return момент отправки уведомления, по умолчанию {@link NotifyWhen#AFTER_SUCCESS}
   * @see NotifyWhen
   */
  NotifyWhen when() default NotifyWhen.AFTER_SUCCESS;

  /**
   * Массив идентификаторов Telegram-чатов для отправки уведомления.
   *
   * <p>Если не задано (пустой массив), используются чаты из глобальной конфигурации.
   * Позволяет переопределить список получателей для конкретного метода.
   *
   * @return массив идентификаторов чатов или пустой массив (по умолчанию)
   */
  long[] chatIds() default {};

  /**
   * Режим форматирования текста сообщения.
   *
   * <p>Определяет, как Telegram будет обрабатывать разметку в тексте сообщения:
   * <ul>
   *   <li>{@link ParseMode#PLAIN} - обычный текст без форматирования</li>
   *   <li>{@link ParseMode#MARKDOWN} - разметка в формате Markdown</li>
   *   <li>{@link ParseMode#HTML} - разметка в формате HTML</li>
   * </ul>
   *
   * @return режим форматирования, по умолчанию {@link ParseMode#PLAIN}
   * @see ParseMode
   */
  ParseMode parseMode() default ParseMode.PLAIN;

  /**
   * Политика обработки ошибок при отправке уведомления.
   *
   * <p>Определяет, как система должна реагировать на ошибки при отправке уведомления:
   * <ul>
   *   <li>{@link ErrorPolicy#LOG_ONLY} - только логировать ошибку, не прерывать выполнение</li>
   *   <li>{@link ErrorPolicy#THROW} - пробросить исключение дальше</li>
   * </ul>
   *
   * @return политика обработки ошибок, по умолчанию {@link ErrorPolicy#LOG_ONLY}
   * @see ErrorPolicy
   */
  ErrorPolicy errorPolicy() default ErrorPolicy.LOG_ONLY;
}
