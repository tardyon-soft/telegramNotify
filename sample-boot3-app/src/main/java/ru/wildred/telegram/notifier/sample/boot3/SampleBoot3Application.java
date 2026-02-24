package ru.wildred.telegram.notifier.sample.boot3;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SampleBoot3Application {
    public static void main(String[] args) {
        SpringApplication.run(SampleBoot3Application.class, args);
    }

    @Bean
    CommandLineRunner demoRunner(DemoService demoService) {
        return args -> {
            demoService.beforeNotification("B3-200", 1500);
            demoService.successNotification("B3-201");
            try {
                demoService.failureNotification("B3-202");
            } catch (IllegalStateException ignored) {
                // Demo intentionally throws to show AFTER_FAILURE behavior.
            }
            try {
                demoService.finallyNotification("B3-203", true);
            } catch (IllegalArgumentException ignored) {
                // Demo intentionally throws to show AFTER_FINALLY behavior with exception context.
            }
            demoService.finallyNotification("B3-204", false);
        };
    }
}
