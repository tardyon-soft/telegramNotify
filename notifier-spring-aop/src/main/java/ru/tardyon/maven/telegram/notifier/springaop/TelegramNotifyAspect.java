package ru.tardyon.maven.telegram.notifier.springaop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import ru.tardyon.maven.telegram.notifier.core.annotation.NotifyWhen;
import ru.tardyon.maven.telegram.notifier.core.annotation.TelegramNotify;
import ru.tardyon.maven.telegram.notifier.core.aop.MethodInvocationContext;
import ru.tardyon.maven.telegram.notifier.core.dispatch.NotificationRequest;
import ru.tardyon.maven.telegram.notifier.core.dispatch.TelegramNotificationDispatcher;


/**
 * Аспект для обработки аннотации {@link TelegramNotify}.
 *
 * <p>Перехватывает выполнение методов, помеченных {@code @TelegramNotify}, и отправляет
 * уведомления в Telegram в зависимости от настроек аннотации (до выполнения метода,
 * после успешного выполнения, после ошибки или в блоке finally).</p>
 *
 * @see TelegramNotify
 * @see NotifyWhen
 * @see TelegramNotificationDispatcher
 */
@Aspect
public class TelegramNotifyAspect {
  private final TelegramNotificationDispatcher dispatcher;

  /**
   * Создаёт экземпляр аспекта с указанным диспетчером уведомлений.
   *
   * @param dispatcher диспетчер для отправки уведомлений в Telegram (не может быть {@code null})
   * @throws NullPointerException если {@code dispatcher} равен {@code null}
   */
  public TelegramNotifyAspect(TelegramNotificationDispatcher dispatcher) {
    this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
  }

  /**
   * Перехватывает выполнение метода, аннотированного {@link TelegramNotify}.
   *
   * <p>В зависимости от значения {@link NotifyWhen} в аннотации, отправляет уведомление:
   * <ul>
   *   <li>{@link NotifyWhen#BEFORE} — до выполнения метода</li>
   *   <li>{@link NotifyWhen#AFTER_SUCCESS} — после успешного выполнения</li>
   *   <li>{@link NotifyWhen#AFTER_FAILURE} — после выброса исключения</li>
   *   <li>{@link NotifyWhen#AFTER_FINALLY} — всегда после выполнения (в блоке finally)</li>
   * </ul></p>
   *
   * @param joinPoint точка соединения AspectJ, представляющая перехваченный метод
   * @param ann       аннотация {@link TelegramNotify} с настройками уведомления
   * @return результат выполнения перехваченного метода
   * @throws Throwable любое исключение, выброшенное перехваченным методом
   */
  @Around("@annotation(ann)")
  public Object around(ProceedingJoinPoint joinPoint, TelegramNotify ann) throws Throwable {
    Method method = resolveMethod(joinPoint);
    Object target = joinPoint.getTarget();
    Object[] args = joinPoint.getArgs();

    if (ann.when() == NotifyWhen.BEFORE) {
      dispatch(ann, target, method, args, null, null);
    }

    Object result = null;
    Throwable error = null;
    try {
      result = joinPoint.proceed();
      if (ann.when() == NotifyWhen.AFTER_SUCCESS) {
        dispatch(ann, target, method, args, result, null);
      }
      return result;
    } catch (Throwable ex) {
      error = ex;
      if (ann.when() == NotifyWhen.AFTER_FAILURE) {
        dispatch(ann, target, method, args, null, ex);
      }
      throw ex;
    } finally {
      if (ann.when() == NotifyWhen.AFTER_FINALLY) {
        dispatch(ann, target, method, args, result, error);
      }
    }
  }

  /**
   * Формирует и отправляет запрос на уведомление через диспетчер.
   *
   * <p>Создаёт контекст вызова метода и запрос уведомления на основе параметров аннотации
   * и данных о выполнении метода, затем передаёт запрос в
   * {@link TelegramNotificationDispatcher}.</p>
   *
   * @param ann       аннотация {@link TelegramNotify} с настройками уведомления
   * @param target    объект, на котором был вызван метод (может быть {@code null})
   * @param method    перехваченный метод
   * @param args      аргументы метода
   * @param result    результат выполнения метода (может быть {@code null})
   * @param throwable исключение, выброшенное методом (может быть {@code null})
   */
  private void dispatch(
      TelegramNotify ann,
      Object target,
      Method method,
      Object[] args,
      Object result,
      Throwable throwable
  ) {
    MethodInvocationContext context = new MethodInvocationContext(
        target, method, args, result, throwable);
    NotificationRequest request = NotificationRequest.of(
        ann.message(),
        ann.condition(),
        toChatIds(ann.chatIds()),
        ann.parseMode(),
        ann.errorPolicy(),
        context
    );
    dispatcher.dispatch(request);
  }

  /**
   * Преобразует массив идентификаторов чатов из примитивного типа {@code long[]} в
   * {@code List<Long>}.
   *
   * @param chatIds массив идентификаторов чатов (может быть {@code null} или пустым)
   * @return список идентификаторов чатов или {@code null}, если входной массив
   *{@code null} или пуст
   */
  private List<Long> toChatIds(long[] chatIds) {
    if (chatIds == null || chatIds.length == 0) {
      return null;
    }
    List<Long> converted = new ArrayList<>(chatIds.length);
    for (long chatId : chatIds) {
      converted.add(chatId);
    }
    return converted;
  }

  /**
   * Разрешает фактический метод целевого объекта из точки соединения AspectJ.
   *
   * <p>Если метод вызван на прокси-объекте, пытается получить
   * метод из реального класса целевого объекта.
   * Сначала ищет публичный метод через {@link Class#getMethod}, затем объявленный метод через
   * {@link Class#getDeclaredMethod}. Если метод не найден, возвращает
   * метод из сигнатуры AspectJ.</p>
   *
   * @param joinPoint точка соединения AspectJ
   * @return разрешённый метод из целевого класса или метод из сигнатуры AspectJ
   */
  private Method resolveMethod(ProceedingJoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method sourceMethod = signature.getMethod();
    Object target = joinPoint.getTarget();
    if (target == null) {
      return sourceMethod;
    }

    Class<?> targetClass = target.getClass();
    try {
      return targetClass.getMethod(sourceMethod.getName(), sourceMethod.getParameterTypes());
    } catch (NoSuchMethodException ignored) {
      try {
        Method declared = targetClass.getDeclaredMethod(sourceMethod.getName(),
            sourceMethod.getParameterTypes());
        declared.setAccessible(true);
        return declared;
      } catch (NoSuchMethodException secondIgnored) {
        return sourceMethod;
      }
    }
  }
}
