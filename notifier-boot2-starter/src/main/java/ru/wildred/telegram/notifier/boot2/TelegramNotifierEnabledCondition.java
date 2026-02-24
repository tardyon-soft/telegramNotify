package ru.wildred.telegram.notifier.boot2;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class TelegramNotifierEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Binder binder = Binder.get(context.getEnvironment());
        TelegramNotifierProperties props = binder.bind("telegram.notifier", Bindable.of(TelegramNotifierProperties.class))
                .orElseGet(TelegramNotifierProperties::new);

        boolean enabled = props.isEnabled();
        String token = props.getToken();
        String username = props.getUsername();
        List<Long> chatIds = resolveChatIds(binder, props);

        return enabled && hasText(token) && hasText(username) && !chatIds.isEmpty();
    }

    private List<Long> resolveChatIds(Binder binder, TelegramNotifierProperties props) {
        List<Long> fromList = props.getChatIds();
        if (fromList != null && !fromList.isEmpty()) {
            return fromList;
        }

        String fromString = binder.bind("telegram.notifier.targets.chat-ids", String.class).orElse("");
        if (!hasText(fromString)) {
            fromString = binder.bind("telegram.notifier.chat-ids", String.class).orElse("");
        }
        if (hasText(fromString)) {
            return parseCsv(fromString);
        }

        Long fromSingle = binder.bind("telegram.notifier.targets.chat-id", Long.class).orElse(null);
        if (fromSingle == null) {
            fromSingle = binder.bind("telegram.notifier.chat-id", Long.class).orElse(null);
        }
        if (fromSingle != null) {
            return Collections.singletonList(fromSingle);
        }

        String fromSingleString = binder.bind("telegram.notifier.targets.chat-id", String.class).orElse("");
        if (!hasText(fromSingleString)) {
            fromSingleString = binder.bind("telegram.notifier.chat-id", String.class).orElse("");
        }
        if (hasText(fromSingleString)) {
            return parseCsv(fromSingleString);
        }

        return Collections.emptyList();
    }

    private List<Long> parseCsv(String value) {
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(this::hasText)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
