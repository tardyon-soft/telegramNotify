package ru.tardyon.maven.telegram.notifier.core.aop;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * Контекст вызова метода, содержащий информацию о целевом объекте, методе,
 * аргументах, результате выполнения и возникшем исключении.
 *
 * <p>Этот класс используется для передачи полной информации о вызове метода
 * в системе уведомлений. Является immutable-объектом.
 */

public final class MethodInvocationContext {
  private final Object target;
  private final Method method;
  private final Object[] arguments;
  private final Object result;
  private final Throwable throwable;

  /**
   * Создает новый контекст вызова метода.
   *
   * @param target    целевой объект, на котором был вызван метод
   * @param method    метод, который был вызван
   * @param arguments аргументы, переданные в метод (копируются для обеспечения неизменяемости)
   * @param result    результат выполнения метода (может быть {@code null})
   * @param throwable исключение, возникшее при выполнении метода (может быть {@code null})
   */
  public MethodInvocationContext(
      Object target,
      Method method,
      Object[] arguments,
      Object result,
      Throwable throwable
  ) {
    this.target = target;
    this.method = method;
    this.arguments = arguments == null ? new Object[0] : Arrays.copyOf(arguments, arguments.length);
    this.result = result;
    this.throwable = throwable;
  }

  /**
   * Возвращает целевой объект, на котором был вызван метод.
   *
   * @return целевой объект
   */
  public Object target() {
    return target;
  }

  /**
   * Возвращает метод, который был вызван.
   *
   * @return объект {@link Method}
   */
  public Method method() {
    return method;
  }

  /**
   * Возвращает копию массива аргументов, переданных в метод.
   *
   * <p>Возвращается копия для обеспечения неизменяемости контекста.
   *
   * @return массив аргументов метода
   */
  public Object[] arguments() {
    return Arrays.copyOf(arguments, arguments.length);
  }

  /**
   * Возвращает результат выполнения метода.
   *
   * @return результат выполнения метода или {@code null}, если метод завершился с ошибкой
   *       или возвращает {@code void}
   */
  public Object result() {
    return result;
  }

  /**
   * Возвращает исключение, возникшее при выполнении метода.
   *
   * @return исключение или {@code null}, если метод выполнился успешно
   */
  public Throwable throwable() {
    return throwable;
  }

  /**
   * Создает пустой контекст вызова метода со всеми {@code null}-значениями.
   *
   * @return пустой контекст вызова метода
   */
  public static MethodInvocationContext empty() {
    return new MethodInvocationContext(null, null, new Object[0], null, null);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MethodInvocationContext)) {
      return false;
    }
    MethodInvocationContext that = (MethodInvocationContext) o;
    return Objects.equals(target, that.target)
        && Objects.equals(method, that.method)
        && Arrays.equals(arguments, that.arguments)
        && Objects.equals(result, that.result)
        && Objects.equals(throwable, that.throwable);
  }

  @Override
  public int hashCode() {
    int result1 = Objects.hash(target, method, result, throwable);
    result1 = 31 * result1 + Arrays.hashCode(arguments);
    return result1;
  }
}
