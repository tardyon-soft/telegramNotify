package ru.tardyon.maven.telegram.notifier.sample.boot3;

import org.springframework.stereotype.Service;
import ru.tardyon.maven.telegram.notifier.core.annotation.NotifyWhen;
import ru.tardyon.maven.telegram.notifier.core.annotation.TelegramNotify;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ErrorPolicy;
import ru.tardyon.maven.telegram.notifier.core.dispatch.ParseMode;

/**
 * Демо-сервис для демонстрации работы аннотации {@link TelegramNotify}.
 *
 * <p>Содержит примеры использования различных режимов уведомлений:
 * <ul>
 *   <li>{@link NotifyWhen#BEFORE} - уведомление до выполнения метода</li>
 *   <li>{@link NotifyWhen#AFTER_SUCCESS} - уведомление после успешного выполнения</li>
 *   <li>{@link NotifyWhen#AFTER_FAILURE} - уведомление при возникновении исключения</li>
 *   <li>{@link NotifyWhen#AFTER_FINALLY} - уведомление в любом случае после выполнения</li>
 * </ul>
 */
@Service
public class DemoService {

  /**
   * Публичный конструктор без аргументов.
   *
   * <p>Spring создаёт бин через конструктор. Явно объявленный конструктор с Javadoc
   * устраняет предупреждение о неявном default-конструкторе без комментария.
   */
  public DemoService() {
    // no-op
  }
  /**
   * Демонстрирует отправку уведомления перед выполнением метода.
   *
   * <p>Уведомление отправляется только если {@code amount > 0}.
   * Сообщение содержит имя метода, идентификатор аккаунта и сумму операции.
   *
   * @param accountId идентификатор аккаунта
   * @return строка с результатом операции в формате "accepted-{accountId}"
   */
  @TelegramNotify(
      message =
          "'[BEFORE] method=' + #methodName + ', account=' + #accountId + ', amount=' + #amount",
      condition = "#amount > 0",
      when = NotifyWhen.BEFORE
  )
  public String beforeNotification(String accountId) {
    return "accepted-" + accountId;
  }

  /**
   * Демонстрирует отправку уведомления после успешного выполнения метода.
   *
   * <p>Уведомление отправляется только если результат начинается с "ok".
   * Сообщение форматируется с использованием HTML-разметки.
   *
   * @param accountId идентификатор аккаунта
   * @return строка с результатом операции в формате "ok-{accountId}"
   */
  @TelegramNotify(
      message = "'<b>[SUCCESS]</b> account=' + #accountId + ', result=' + #result",
      condition = "#result.startsWith('ok')",
      when = NotifyWhen.AFTER_SUCCESS,
      parseMode = ParseMode.HTML
  )
  public String successNotification(String accountId) {
    return "ok-" + accountId;
  }

  /**
   * Демонстрирует отправку уведомления при возникновении исключения.
   *
   * <p>Метод всегда выбрасывает {@link IllegalStateException}.
   * При ошибке отправки уведомления используется политика {@link ErrorPolicy#LOG_ONLY},
   * что означает только логирование без повторного выброса исключения.
   *
   * @param accountId
   *     идентификатор аккаунта
   * @throws IllegalStateException
   *     всегда выбрасывается с сообщением о неудачной операции
   */
  @TelegramNotify(
      message = "'[FAILURE] account=' + #accountId + ', ex=' + #ex.message",
      when = NotifyWhen.AFTER_FAILURE,
      errorPolicy = ErrorPolicy.LOG_ONLY
  )
  public void failureNotification(String accountId) {
    throw new IllegalStateException("transfer failed for " + accountId);
  }

  /**
   * Демонстрирует отправку уведомления после выполнения метода в любом случае.
   *
   * <p>Уведомление отправляется независимо от того, завершился метод успешно или с ошибкой.
   * Сообщение содержит информацию о результате и исключении (если было).
   *
   * @param accountId идентификатор аккаунта
   * @param fail      флаг принудительного выброса исключения
   * @return строка с результатом операции в формате "done-{accountId}", если {@code fail = false}
   * @throws IllegalArgumentException если {@code fail = true}
   */
  @TelegramNotify(
      message =
          "'[FINALLY] account=' + #accountId + ', result=' + #result + ', "
              + "ex=' + (#ex != null ? #ex.message : 'none')",
      when = NotifyWhen.AFTER_FINALLY
  )
  public String finallyNotification(String accountId, boolean fail) {
    if (fail) {
      throw new IllegalArgumentException("forced fail for " + accountId);
    }
    return "done-" + accountId;
  }
}
