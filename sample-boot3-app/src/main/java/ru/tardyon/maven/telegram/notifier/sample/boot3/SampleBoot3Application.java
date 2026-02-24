package ru.tardyon.maven.telegram.notifier.sample.boot3;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Главный класс Spring Boot приложения для демонстрации работы Telegram Notifier.
 *
 * <p>Это демонстрационное приложение показывает использование аннотации
 * {@link ru.tardyon.maven.telegram.notifier.core.annotation.TelegramNotify}
 * в контексте Spring Boot 3. Приложение запускает набор примеров через {@link CommandLineRunner},
 * демонстрирующих различные режимы отправки уведомлений в Telegram.
 *
 * @see DemoService
 */
@SpringBootApplication
public class SampleBoot3Application {

  /**
   * Публичный конструктор без аргументов.
   *
   * <p>Нужен, чтобы явно задокументировать конструктор (некоторые статические анализаторы
   * ругаются на неявный default-конструктор без комментария).
   */
  public SampleBoot3Application() {
    // no-op
  }

  /**
   * Точка входа в приложение.
   *
   * @param args аргументы командной строки
   */
  public static void main(String[] args) {
    SpringApplication.run(SampleBoot3Application.class, args);
  }

  /**
   * Создает CommandLineRunner для демонстрации работы Telegram уведомлений.
   *
   * <p>Выполняет последовательность вызовов методов {@link DemoService}, демонстрирующих:
   * <ul>
   *   <li>Уведомление BEFORE - перед выполнением метода</li>
   *   <li>Уведомление AFTER_SUCCESS - после успешного выполнения</li>
   *   <li>Уведомление AFTER_FAILURE - при возникновении исключения</li>
   *   <li>Уведомление AFTER_FINALLY - в любом случае после выполнения (с исключением и без)</li>
   * </ul>
   *
   * @param demoService сервис с демонстрационными методами
   * @return CommandLineRunner, выполняющий демонстрационные сценарии
   */
  @Bean
  CommandLineRunner demoRunner(DemoService demoService) {
    return args -> {
      System.out.println(demoService.beforeNotification("B3-200"));
      System.out.println(demoService.successNotification("B3-201"));
      try {
        demoService.failureNotification("B3-202");
      } catch (IllegalStateException ignored) {
        // Demo intentionally throws to show AFTER_FAILURE behavior.
      }
      try {
        System.out.println(demoService.finallyNotification("B3-203", true));
      } catch (IllegalArgumentException ignored) {
        // Demo intentionally throws to show AFTER_FINALLY behavior with exception context.
      }
      System.out.println(demoService.finallyNotification("B3-204", false));
    };
  }
}
