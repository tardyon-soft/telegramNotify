package ru.tardyon.maven.telegram.notifier.sample.boot2;

import org.springframework.stereotype.Service;
import ru.tardyon.maven.telegram.notifier.core.annotation.NotifyWhen;
import ru.tardyon.maven.telegram.notifier.core.annotation.TelegramNotify;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ParseMode;


/**
 * Демонстрационный сервис для примеров использования аннотации {@link TelegramNotify}.
 *
 * <p>Показывает различные режимы отправки уведомлений:
 * <ul>
 *   <li>{@link NotifyWhen#BEFORE} - перед выполнением метода</li>
 *   <li>{@link NotifyWhen#AFTER_SUCCESS} - после успешного выполнения</li>
 *   <li>{@link NotifyWhen#AFTER_FAILURE} - при возникновении исключения</li>
 *   <li>{@link NotifyWhen#AFTER_FINALLY} - в любом случае после выполнения</li>
 * </ul>
 */
@Service
public class DemoService {
  /**
   * Демонстрирует отправку уведомления ПЕРЕД выполнением метода.
   *
   * <p>Уведомление отправляется только если сумма больше нуля (condition).
   * Использует режим форматирования MARKDOWN.
   *
   * @param orderId идентификатор заказа
   * @return строка подтверждения с идентификатором заказа
   */
  @TelegramNotify(
      message = "'[BEFORE] method=' + #methodName + ', order=' + #orderId + ', amount=' + #amount",
      condition = "#amount > 0",
      when = NotifyWhen.BEFORE,
      parseMode = ParseMode.MARKDOWN
  )
  public String beforeNotification(String orderId) {
    return "accepted-" + orderId;
  }

  /**
   * Демонстрирует отправку уведомления ПОСЛЕ успешного выполнения метода.
   *
   * <p>Уведомление отправляется только если результат начинается с 'ok' (condition).
   * Использует режим форматирования HTML.
   *
   * @param orderId идентификатор заказа
   * @return результат обработки заказа
   */
  @TelegramNotify(
      message = "'[SUCCESS] order=' + #orderId + ', result=' + #result",
      condition = "#result.startsWith('ok')",
      when = NotifyWhen.AFTER_SUCCESS,
      parseMode = ParseMode.HTML
  )
  public String successNotification(String orderId) {
    return "ok-" + orderId;
  }

  /**
   * Демонстрирует отправку уведомления при ОШИБКЕ выполнения метода.
   *
   * <p>Метод всегда бросает исключение для демонстрации режима AFTER_FAILURE.
   * Уведомление содержит информацию об исключении.
   *
   * @param orderId
   *     идентификатор заказа
   * @throws IllegalStateException
   *     всегда для демонстрации обработки ошибок
   */
  @TelegramNotify(
      message = "'[FAILURE] order=' + #orderId + ', ex=' + #ex.message",
      when = NotifyWhen.AFTER_FAILURE
  )
  public void failureNotification(String orderId) {
    throw new IllegalStateException("payment failed for " + orderId);
  }

  /**
   * Демонстрирует отправку уведомления В ЛЮБОМ СЛУЧАЕ после выполнения метода.
   *
   * <p>Уведомление отправляется и при успехе, и при ошибке (режим AFTER_FINALLY).
   * Сообщение содержит информацию о результате и/или исключении.
   *
   * @param orderId идентификатор заказа
   * @param fail    флаг принудительной ошибки (true - бросить исключение)
   * @return результат обработки, если fail=false
   * @throws IllegalArgumentException если fail=true
   */
  @TelegramNotify(
      message = "'[FINALLY] order=' + #orderId + ', result=' "
          + "+ #result + ', ex=' + (#ex != null ? #ex.message : 'none')",
      when = NotifyWhen.AFTER_FINALLY
  )
  public String finallyNotification(String orderId, boolean fail) {
    if (fail) {
      throw new IllegalArgumentException("forced fail for " + orderId);
    }
    return "done-" + orderId;
  }
}
