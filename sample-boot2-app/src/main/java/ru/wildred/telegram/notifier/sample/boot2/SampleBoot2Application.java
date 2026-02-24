package ru.wildred.telegram.notifier.sample.boot2;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SampleBoot2Application {
    public static void main(String[] args) {
        SpringApplication.run(SampleBoot2Application.class, args);
    }

    @Bean
    CommandLineRunner demoRunner(DemoService demoService) {
        return args -> {
            demoService.beforeNotification("B2-100", 500);
            demoService.successNotification("B2-101");
            try {
                demoService.failureNotification("B2-102");
            } catch (IllegalStateException ignored) {
                // Demo intentionally throws to show AFTER_FAILURE behavior.
            }
            try {
                demoService.finallyNotification("B2-103", true);
            } catch (IllegalArgumentException ignored) {
                // Demo intentionally throws to show AFTER_FINALLY behavior with exception context.
            }
            demoService.finallyNotification("B2-104", false);
        };
    }
}
