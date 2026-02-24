package ru.tardyon.maven.telegram.notifier.sample.boot2;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Демонстрационное Spring Boot 2 приложение для примеров использования Telegram-уведомлений.
 *
 * <p>Приложение запускает набор тестовых сценариев, демонстрирующих различные режимы
 * отправки уведомлений через {@link DemoService}:
 * <ul>
 *   <li>Уведомления перед выполнением метода (BEFORE)</li>
 *   <li>Уведомления после успешного выполнения (AFTER_SUCCESS)</li>
 *   <li>Уведомления при возникновении ошибок (AFTER_FAILURE)</li>
 *   <li>Уведомления в любом случае после выполнения (AFTER_FINALLY)</li>
 * </ul>
 */
@SpringBootApplication
public class SampleBoot2Application {
  /**
   * Точка входа в приложение.
   *
   * @param args аргументы командной строки
   */
  public static void main(String[] args) {
    SpringApplication.run(SampleBoot2Application.class, args);
  }

  /**
   * Создает CommandLineRunner для выполнения демонстрационных сценариев.
   *
   * <p>Запускает последовательность тестовых вызовов методов {@link DemoService}:
   * <ol>
   *   <li>beforeNotification - демонстрация уведомления перед выполнением</li>
   *   <li>successNotification - демонстрация уведомления после успешного выполнения</li>
   *   <li>failureNotification - демонстрация уведомления при ошибке(исключение обрабатывается)</li>
   *   <li>finallyNotification с ошибкой - демонстрация уведомления AFTER_FINALLY с исключением</li>
   *   <li>finallyNotification без ошибки - демонстрация уведомления AFTER_FINALLY при успехе</li>
   * </ol>
   *
   * @param demoService сервис для демонстрации различных типов уведомлений
   * @return CommandLineRunner, выполняющий демонстрационные сценарии
   */
  @Bean
  CommandLineRunner demoRunner(DemoService demoService) {
    return args -> {
      System.out.println(demoService.beforeNotification("B2-100"));
      System.out.println(demoService.successNotification("B2-101"));
      try {
        demoService.failureNotification("B2-102");
      } catch (IllegalStateException ignored) {
        // Demo intentionally throws to show AFTER_FAILURE behavior.
      }
      try {
        System.out.println(demoService.finallyNotification("B2-103", true));
      } catch (IllegalArgumentException ignored) {
        // Demo intentionally throws to show AFTER_FINALLY behavior with exception context.
      }
      System.out.println(demoService.finallyNotification("B2-104", false));
    };
  }
}
